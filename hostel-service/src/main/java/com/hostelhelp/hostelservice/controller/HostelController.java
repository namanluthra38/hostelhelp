package com.hostelhelp.hostelservice.controller;



import com.hostelhelp.hostelservice.dto.HostelRequestDTO;
import com.hostelhelp.hostelservice.dto.HostelResponseDTO;
import com.hostelhelp.hostelservice.exception.HostelNotFoundException;
import com.hostelhelp.hostelservice.service.HostelService;
import com.hostelhelp.hostelservice.validation.CreateHostelValidationGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/hostels")
@Tag(name = "Hostel", description = "API for managing Hostels")
public class HostelController {

    private final HostelService hostelService;

    public HostelController(HostelService hostelService) {
        this.hostelService = hostelService;
    }

    @GetMapping
    @Operation(summary = "Get all hostels")
    public ResponseEntity<List<HostelResponseDTO>> getHostels() {
        List<HostelResponseDTO> hostels = hostelService.getHostels();
        return ResponseEntity.ok().body(hostels);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a Hostel by ID")
    public ResponseEntity<HostelResponseDTO> getStudent(@PathVariable UUID id) {
        try {
            HostelResponseDTO student = hostelService.getHostel(id);
            return ResponseEntity.ok().body(student);
        } catch (HostelNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/is-boys")
    @Operation(summary = "Return whether the hostel is a boys hostel")
    public ResponseEntity<Boolean> isBoysHostel(@PathVariable UUID id) {
        try {
            HostelResponseDTO dto = hostelService.getHostel(id);
            return ResponseEntity.ok(dto.isBoysHostel());
        } catch (HostelNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Create a new hostel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HostelResponseDTO> createHostel(
            @Validated({Default.class, CreateHostelValidationGroup.class})
            @RequestBody HostelRequestDTO hostelRequestDTO) {
        HostelResponseDTO hostelResponseDTO = hostelService.createHostel(hostelRequestDTO);
        return ResponseEntity.ok().body(hostelResponseDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a hostel")
    @PreAuthorize("hasAnyRole('ADMIN', 'WARDEN')")
    public ResponseEntity<HostelResponseDTO> updateHostel(
            @PathVariable UUID id,
            @Validated(Default.class) @RequestBody HostelRequestDTO hostelRequestDTO) {
        HostelResponseDTO hostelResponseDTO = hostelService.updateHostel(id, hostelRequestDTO);
        return ResponseEntity.ok().body(hostelResponseDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a hostel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHostel(@PathVariable UUID id) {
        hostelService.deleteHostel(id);
        return ResponseEntity.noContent().build();
    }
}
