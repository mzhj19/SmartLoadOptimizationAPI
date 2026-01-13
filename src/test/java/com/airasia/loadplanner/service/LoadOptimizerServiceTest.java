package com.airasia.loadplanner.service;

import com.airasia.loadplanner.model.OptimizationResult;
import com.airasia.loadplanner.model.Order;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadOptimizerServiceTest {

    private final LoadOptimizerService service = new LoadOptimizerService();

    @Test
    void testEmptyOrders() {
        OptimizationResult result = service.optimize(44000, 3000, List.of());
        
        assertEquals(0, result.totalPayoutCents());
        assertEquals(0, result.selectedOrders().size());
    }

    @Test
    void testSingleOrder() {
        Order order = new Order(
            "ord-001", 250000L, 18000, 1200,
            "Los Angeles, CA", "Dallas, TX",
            LocalDate.parse("2025-12-05"), LocalDate.parse("2025-12-09"),
            false
        );

        OptimizationResult result = service.optimize(44000, 3000, List.of(order));
        
        assertEquals(250000L, result.totalPayoutCents());
        assertEquals(1, result.selectedOrders().size());
        assertEquals("ord-001", result.selectedOrders().get(0).id());
    }

    @Test
    void testMultipleCompatibleOrders() {
        List<Order> orders = List.of(
            new Order("ord-001", 250000L, 18000, 1200,
                "Los Angeles, CA", "Dallas, TX",
                LocalDate.parse("2025-12-05"), LocalDate.parse("2025-12-09"), false),
            new Order("ord-002", 180000L, 12000, 900,
                "Los Angeles, CA", "Dallas, TX",
                LocalDate.parse("2025-12-04"), LocalDate.parse("2025-12-10"), false)
        );

        OptimizationResult result = service.optimize(44000, 3000, orders);
        
        assertEquals(430000L, result.totalPayoutCents());
        assertEquals(2, result.selectedOrders().size());
        assertEquals(30000, result.totalWeightLbs());
        assertEquals(2100, result.totalVolumeCuft());
    }

    @Test
    void testWeightConstraint() {
        List<Order> orders = List.of(
            new Order("ord-001", 250000L, 30000, 1200,
                "Los Angeles, CA", "Dallas, TX",
                LocalDate.parse("2025-12-05"), LocalDate.parse("2025-12-09"), false),
            new Order("ord-002", 180000L, 20000, 900,
                "Los Angeles, CA", "Dallas, TX",
                LocalDate.parse("2025-12-04"), LocalDate.parse("2025-12-10"), false)
        );

        OptimizationResult result = service.optimize(44000, 3000, orders);
        
        assertTrue(result.totalWeightLbs() <= 44000);
        assertEquals(1, result.selectedOrders().size());
        assertEquals("ord-001", result.selectedOrders().get(0).id());
    }

    @Test
    void testVolumeConstraint() {
        List<Order> orders = List.of(
            new Order("ord-001", 250000L, 10000, 2000,
                "Los Angeles, CA", "Dallas, TX",
                LocalDate.parse("2025-12-05"), LocalDate.parse("2025-12-09"), false),
            new Order("ord-002", 180000L, 10000, 1500,
                "Los Angeles, CA", "Dallas, TX",
                LocalDate.parse("2025-12-04"), LocalDate.parse("2025-12-10"), false)
        );

        OptimizationResult result = service.optimize(44000, 3000, orders);
        
        assertTrue(result.totalVolumeCuft() <= 3000);
        assertEquals(1, result.selectedOrders().size());
        assertEquals("ord-001", result.selectedOrders().get(0).id());
    }

    @Test
    void testHazmatIsolation() {
        List<Order> orders = List.of(
            new Order("ord-001", 250000L, 18000, 1200,
                "Los Angeles, CA", "Dallas, TX",
                LocalDate.parse("2025-12-05"), LocalDate.parse("2025-12-09"), false),
            new Order("ord-002", 180000L, 12000, 900,
                "Los Angeles, CA", "Dallas, TX",
                LocalDate.parse("2025-12-04"), LocalDate.parse("2025-12-10"), false),
            new Order("ord-003", 320000L, 30000, 1800,
                "Los Angeles, CA", "Dallas, TX",
                LocalDate.parse("2025-12-06"), LocalDate.parse("2025-12-08"), true)
        );

        OptimizationResult result = service.optimize(44000, 3000, orders);
        
        assertTrue(result.totalPayoutCents() > 0);
        assertTrue(result.selectedOrders().size() >= 1);
    }

    @Test
    void testDifferentRoutes() {
        List<Order> orders = List.of(
            new Order("ord-001", 250000L, 18000, 1200,
                "Los Angeles, CA", "Dallas, TX",
                LocalDate.parse("2025-12-05"), LocalDate.parse("2025-12-09"), false),
            new Order("ord-002", 180000L, 12000, 900,
                "New York, NY", "Boston, MA",
                LocalDate.parse("2025-12-04"), LocalDate.parse("2025-12-10"), false)
        );

        OptimizationResult result = service.optimize(44000, 3000, orders);
        
        assertEquals(1, result.selectedOrders().size());
        assertEquals(250000L, result.totalPayoutCents());
    }

    @Test
    void testMaximizesRevenue() {
        List<Order> orders = List.of(
            new Order("ord-001", 100000L, 10000, 1000,
                "Los Angeles, CA", "Dallas, TX",
                LocalDate.parse("2025-12-05"), LocalDate.parse("2025-12-09"), false),
            new Order("ord-002", 150000L, 10000, 1000,
                "Los Angeles, CA", "Dallas, TX",
                LocalDate.parse("2025-12-04"), LocalDate.parse("2025-12-10"), false),
            new Order("ord-003", 200000L, 10000, 1000,
                "Los Angeles, CA", "Dallas, TX",
                LocalDate.parse("2025-12-06"), LocalDate.parse("2025-12-08"), false)
        );

        OptimizationResult result = service.optimize(25000, 2500, orders);
        
        assertEquals(350000L, result.totalPayoutCents());
        assertEquals(2, result.selectedOrders().size());
    }
}
