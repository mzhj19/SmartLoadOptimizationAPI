package com.airasia.loadplanner.controller;

import com.airasia.loadplanner.dto.OptimizationRequest;
import com.airasia.loadplanner.dto.OptimizationResponse;
import com.airasia.loadplanner.dto.OrderDto;
import com.airasia.loadplanner.exception.InvalidRequestException;
import com.airasia.loadplanner.model.OptimizationResult;
import com.airasia.loadplanner.model.Order;
import com.airasia.loadplanner.service.LoadOptimizerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/load-optimizer")
public class LoadOptimizerController {

    private final LoadOptimizerService optimizerService;

    public LoadOptimizerController(LoadOptimizerService optimizerService) {
        this.optimizerService = optimizerService;
    }


    @PostMapping("/optimize")
    public ResponseEntity<OptimizationResponse> optimize(@Valid @RequestBody OptimizationRequest request) {
        validateRequest(request);

        List<Order> orders = request.orders().stream()
            .map(this::toOrder)
            .toList();

        OptimizationResult result = optimizerService.optimize(
            request.truck().maxWeightLbs(),
            request.truck().maxVolumeCuft(),
            orders
        );

        OptimizationResponse response = buildResponse(request.truck().id(), result, request.truck());

        return ResponseEntity.ok(response);
    }

    private void validateRequest(OptimizationRequest request) {
        for (OrderDto order : request.orders()) {
            try {
                LocalDate pickup = LocalDate.parse(order.pickupDate());
                LocalDate delivery = LocalDate.parse(order.deliveryDate());
                
                if (delivery.isBefore(pickup)) {
                    throw new InvalidRequestException(
                        "Delivery date must be on or after pickup date for order: " + order.id()
                    );
                }
            } catch (DateTimeParseException e) {
                throw new InvalidRequestException(
                    "Invalid date format for order: " + order.id() + ". Expected format: YYYY-MM-DD"
                );
            }
        }
    }

    private Order toOrder(OrderDto dto) {
        return new Order(
            dto.id(),
            dto.payoutCents(),
            dto.weightLbs(),
            dto.volumeCuft(),
            dto.origin(),
            dto.destination(),
            LocalDate.parse(dto.pickupDate()),
            LocalDate.parse(dto.deliveryDate()),
            dto.isHazmat()
        );
    }

    private OptimizationResponse buildResponse(String truckId, OptimizationResult result, 
                                               com.airasia.loadplanner.dto.TruckDto truck) {
        List<String> selectedOrderIds = result.selectedOrders().stream()
            .map(Order::id)
            .toList();

        double weightUtilization = truck.maxWeightLbs() > 0 
            ? (result.totalWeightLbs() * 100.0) / truck.maxWeightLbs() 
            : 0.0;
        
        double volumeUtilization = truck.maxVolumeCuft() > 0 
            ? (result.totalVolumeCuft() * 100.0) / truck.maxVolumeCuft() 
            : 0.0;

        return new OptimizationResponse(
            truckId,
            selectedOrderIds,
            result.totalPayoutCents(),
            result.totalWeightLbs(),
            result.totalVolumeCuft(),
            Math.round(weightUtilization * 100.0) / 100.0,
            Math.round(volumeUtilization * 100.0) / 100.0
        );
    }
}
