package com.airasia.loadplanner.service;

import com.airasia.loadplanner.model.OptimizationResult;
import com.airasia.loadplanner.model.Order;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LoadOptimizerService {

    public OptimizationResult optimize(int maxWeightLbs, int maxVolumeCuft, List<Order> orders) {
        if (orders.isEmpty()) {
            return new OptimizationResult(List.of(), 0L, 0, 0);
        }

        int n = orders.size();
        List<Order> compatibleOrders = filterCompatibleOrders(orders);
        
        if (compatibleOrders.isEmpty()) {
            return new OptimizationResult(List.of(), 0L, 0, 0);
        }

        // DP with bitmask: dp[mask] = {payout, weight, volume}
        int totalStates = 1 << compatibleOrders.size();
        long[] dpPayout = new long[totalStates];
        int[] dpWeight = new int[totalStates];
        int[] dpVolume = new int[totalStates];
        boolean[] valid = new boolean[totalStates];
        
        valid[0] = true;

        for (int mask = 0; mask < totalStates; mask++) {
            if (!valid[mask]) continue;

            for (int i = 0; i < compatibleOrders.size(); i++) {
                if ((mask & (1 << i)) != 0) continue;

                Order order = compatibleOrders.get(i);
                int newWeight = dpWeight[mask] + order.weightLbs();
                int newVolume = dpVolume[mask] + order.volumeCuft();

                if (newWeight <= maxWeightLbs && newVolume <= maxVolumeCuft) {
                    int newMask = mask | (1 << i);
                    long newPayout = dpPayout[mask] + order.payoutCents();

                    if (!valid[newMask] || newPayout > dpPayout[newMask]) {
                        valid[newMask] = true;
                        dpPayout[newMask] = newPayout;
                        dpWeight[newMask] = newWeight;
                        dpVolume[newMask] = newVolume;
                    }
                }
            }
        }

        // Find best mask
        int bestMask = 0;
        long bestPayout = 0;
        for (int mask = 0; mask < totalStates; mask++) {
            if (valid[mask] && dpPayout[mask] > bestPayout) {
                bestPayout = dpPayout[mask];
                bestMask = mask;
            }
        }

        // Reconstruct solution
        List<Order> selectedOrders = new ArrayList<>();
        for (int i = 0; i < compatibleOrders.size(); i++) {
            if ((bestMask & (1 << i)) != 0) {
                selectedOrders.add(compatibleOrders.get(i));
            }
        }

        return new OptimizationResult(
            selectedOrders,
            dpPayout[bestMask],
            dpWeight[bestMask],
            dpVolume[bestMask]
        );
    }

    private List<Order> filterCompatibleOrders(List<Order> orders) {
        if (orders.isEmpty()) return List.of();

        // Group by route
        Map<String, List<Order>> routeGroups = new HashMap<>();
        for (Order order : orders) {
            String route = order.origin() + "->" + order.destination();
            routeGroups.computeIfAbsent(route, k -> new ArrayList<>()).add(order);
        }

        // Find largest compatible group
        List<Order> largestGroup = List.of();
        for (List<Order> group : routeGroups.values()) {
            List<Order> compatible = filterHazmatCompatible(group);
            if (compatible.size() > largestGroup.size()) {
                largestGroup = compatible;
            }
        }

        return largestGroup;
    }

    private List<Order> filterHazmatCompatible(List<Order> orders) {
        long hazmatCount = orders.stream().filter(Order::isHazmat).count();
        
        // If multiple hazmat orders, cannot combine them - choose best strategy
        if (hazmatCount > 1) {
            List<Order> nonHazmat = orders.stream()
                .filter(o -> !o.isHazmat())
                .toList();
            
            // Return non-hazmat orders if available
            if (!nonHazmat.isEmpty()) {
                return nonHazmat;
            }
            
            // If only hazmat orders, return single best one
            return orders.stream()
                .filter(Order::isHazmat)
                .max(Comparator.comparingLong(Order::payoutCents))
                .map(List::of)
                .orElse(List.of());
        }
        
        return orders;
    }
}
