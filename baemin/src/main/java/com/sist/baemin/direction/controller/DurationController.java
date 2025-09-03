package com.sist.baemin.direction.controller;

import com.sist.baemin.direction.dto.DurationRequest;
import com.sist.baemin.direction.service.DirectionsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DurationController {

    private final DirectionsService directionsService;

    public DurationController(DirectionsService directionsService) {
        this.directionsService = directionsService;
    }

    @PostMapping("/duration")
    public ResponseEntity<String> getDuration(@RequestBody DurationRequest request) {
        String duration = directionsService.getDuration(request.getStart(), request.getGoal());
        return ResponseEntity.ok(duration);
    }
}