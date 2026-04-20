package com.smartload.controller;

import com.smartload.dto.OptimizeRequest;
import com.smartload.dto.OptimizeResponse;
import com.smartload.service.OptimizationPipelineService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoint for the load optimizer.
 */
@RestController
@RequestMapping("/api/v1/load-optimizer")
public class LoadOptimizerController {

    private final OptimizationPipelineService pipeline;

    public LoadOptimizerController(OptimizationPipelineService pipeline) {
        this.pipeline = pipeline;
    }

    @PostMapping("/optimize")
    public ResponseEntity<OptimizeResponse> optimize(
            @Valid @RequestBody OptimizeRequest request) {
        OptimizeResponse response = pipeline.execute(request);
        return ResponseEntity.ok(response);
    }
}
