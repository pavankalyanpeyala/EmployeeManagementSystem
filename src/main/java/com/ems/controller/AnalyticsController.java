package com.ems.controller;

import com.ems.dto.AnalyticsResponse;
import com.ems.dto.ApiResponse;
import com.ems.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getDashboard() {
        return ResponseEntity.ok(
            ApiResponse.success("Analytics fetched", analyticsService.getDashboardAnalytics())
        );
    }
}
