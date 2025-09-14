package com.hostelhelp.wardenservice.controller;


import com.hostelhelp.wardenservice.dto.WardenRequestDTO;
import com.hostelhelp.wardenservice.dto.WardenResponseDTO;
import com.hostelhelp.wardenservice.service.WardenService;
import com.hostelhelp.wardenservice.validation.CreateWardenValidationGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/wardens")
@Tag(name = "Warden", description = "API for managing Wardens")
public class WardenController {

    private final WardenService wardenService;

    public WardenController(WardenService wardenService) {
        this.wardenService = wardenService;
    }

    @GetMapping
    @Operation(summary = "Get all wardens")
    public ResponseEntity<List<WardenResponseDTO>> getWardens() {
        List<WardenResponseDTO> wardens = wardenService.getWardens();
        return ResponseEntity.ok().body(wardens);
    }

    @PostMapping
    @Operation(summary = "Create a new warden")
    public ResponseEntity<WardenResponseDTO> createWarden(
            @Validated({Default.class, CreateWardenValidationGroup.class})
            @RequestBody WardenRequestDTO wardenRequestDTO) {
        WardenResponseDTO wardenResponseDTO = wardenService.createWarden(wardenRequestDTO);
        return ResponseEntity.ok().body(wardenResponseDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a warden")
    public ResponseEntity<WardenResponseDTO> updateWarden(
            @PathVariable UUID id,
            @Validated(Default.class) @RequestBody WardenRequestDTO wardenRequestDTO) {
        WardenResponseDTO wardenResponseDTO = wardenService.updateWarden(id, wardenRequestDTO);
        return ResponseEntity.ok().body(wardenResponseDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a warden")
    public ResponseEntity<Void> deleteWarden(@PathVariable UUID id) {
        wardenService.deleteWarden(id);
        return ResponseEntity.noContent().build();
    }
}
