package com.hostelhelp.wardenservice.controller;


import com.hostelhelp.wardenservice.dto.WardenRequestDTO;
import com.hostelhelp.wardenservice.dto.WardenResponseDTO;
import com.hostelhelp.wardenservice.exception.WardenNotFoundException;
import com.hostelhelp.wardenservice.service.WardenService;
import com.hostelhelp.wardenservice.validation.CreateWardenValidationGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @PreAuthorize("hasAnyRole('WARDEN','ADMIN')")
    public ResponseEntity<List<WardenResponseDTO>> getWardens() {
        List<WardenResponseDTO> wardens = wardenService.getWardens();
        return ResponseEntity.ok().body(wardens);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a warden by ID")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<WardenResponseDTO> getStudent(@PathVariable UUID id) {
        try {
            WardenResponseDTO student = wardenService.getWarden(id);
            return ResponseEntity.ok().body(student);
        } catch (WardenNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }



    @PostMapping
    @Operation(summary = "Create a new warden")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WardenResponseDTO> createWarden(
            @Validated({Default.class, CreateWardenValidationGroup.class})
            @RequestBody WardenRequestDTO wardenRequestDTO) {
        WardenResponseDTO wardenResponseDTO = wardenService.createWarden(wardenRequestDTO);
        return ResponseEntity.ok().body(wardenResponseDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a warden")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WardenResponseDTO> updateWarden(
            @PathVariable UUID id,
            @Validated(Default.class) @RequestBody WardenRequestDTO wardenRequestDTO) {
        WardenResponseDTO wardenResponseDTO = wardenService.updateWarden(id, wardenRequestDTO);
        return ResponseEntity.ok().body(wardenResponseDTO);
    }
    @GetMapping("/me")
    @Operation(summary = "Get current student's profile")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<WardenResponseDTO> getCurrentStudentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        WardenResponseDTO student = wardenService.getWardenByEmail(email);
        return ResponseEntity.ok().body(student);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a warden")
    public ResponseEntity<Void> deleteWarden(@PathVariable UUID id) {
        wardenService.deleteWarden(id);
        return ResponseEntity.noContent().build();
    }
}
