package com.example.jobfinder.controller;

import com.example.jobfinder.dto.report.ReportRequest;
import com.example.jobfinder.dto.report.ReportResponse;
import com.example.jobfinder.dto.report.ReportTypeResponse;
import com.example.jobfinder.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ReportResponse> submitReport(@Valid @RequestBody ReportRequest request) {
        ReportResponse response = reportService.submitReport(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public Page<ReportResponse> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return reportService.getAllReports(page, size);
    }

    @GetMapping("/report-type")
    public ResponseEntity<List<ReportTypeResponse>> getReportTypes() {
        List<ReportTypeResponse> response = reportService.getAllReportTypes();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports/search")
    public ResponseEntity<Page<ReportResponse>> searchReportsByTypeName(
            @RequestParam(required = false) String typeName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reportService.searchReportsByType(typeName, page, size));
    }
}
