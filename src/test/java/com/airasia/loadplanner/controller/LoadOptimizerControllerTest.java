package com.airasia.loadplanner.controller;

import com.airasia.loadplanner.dto.OptimizationRequest;
import com.airasia.loadplanner.dto.OptimizationResponse;
import com.airasia.loadplanner.dto.OrderDto;
import com.airasia.loadplanner.dto.TruckDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoadOptimizerControllerTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void testOptimizeSuccess() {
        TruckDto truck = new TruckDto("truck-123", 44000, 3000);
        List<OrderDto> orders = List.of(
            new OrderDto("ord-001", 250000L, 18000, 1200, "Los Angeles, CA", "Dallas, TX",
                "2025-12-05", "2025-12-09", false),
            new OrderDto("ord-002", 180000L, 12000, 900, "Los Angeles, CA", "Dallas, TX",
                "2025-12-04", "2025-12-10", false)
        );
        OptimizationRequest request = new OptimizationRequest(truck, orders);

        ResponseEntity<OptimizationResponse> response = restTemplate.postForEntity(
            getBaseUrl() + "/api/v1/load-optimizer/optimize", request, OptimizationResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("truck-123", response.getBody().truckId());
        assertEquals(430000L, response.getBody().totalPayoutCents());
        assertEquals(30000, response.getBody().totalWeightLbs());
        assertEquals(2100, response.getBody().totalVolumeCuft());
    }

    @Test
    void testValidationError() {
        try {
            TruckDto truck = new TruckDto("truck-123", -1, 3000);
            OptimizationRequest request = new OptimizationRequest(truck, List.of());

            restTemplate.postForEntity(
                getBaseUrl() + "/api/v1/load-optimizer/optimize", request, String.class);
            fail("Expected exception");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("400"));
        }
    }

    @Test
    void testInvalidDateFormat() {
        try {
            TruckDto truck = new TruckDto("truck-123", 44000, 3000);
            List<OrderDto> orders = List.of(
                new OrderDto("ord-001", 250000L, 18000, 1200, "Los Angeles, CA", "Dallas, TX",
                    "invalid-date", "2025-12-09", false)
            );
            OptimizationRequest request = new OptimizationRequest(truck, orders);

            restTemplate.postForEntity(
                getBaseUrl() + "/api/v1/load-optimizer/optimize", request, String.class);
            fail("Expected exception");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("400"));
        }
    }

    @Test
    void testEmptyOrders() {
        TruckDto truck = new TruckDto("truck-123", 44000, 3000);
        OptimizationRequest request = new OptimizationRequest(truck, List.of());

        ResponseEntity<OptimizationResponse> response = restTemplate.postForEntity(
            getBaseUrl() + "/api/v1/load-optimizer/optimize", request, OptimizationResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0L, response.getBody().totalPayoutCents());
        assertTrue(response.getBody().selectedOrderIds().isEmpty());
    }
}
