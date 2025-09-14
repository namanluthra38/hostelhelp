package com.hostelhelp.hostelservice.service;

import com.hostelhelp.hostelservice.dto.HostelRequestDTO;
import com.hostelhelp.hostelservice.dto.HostelResponseDTO;
import com.hostelhelp.hostelservice.exception.HostelAlreadyExistsException;
import com.hostelhelp.hostelservice.exception.HostelNotFoundException;
import com.hostelhelp.hostelservice.mapper.HostelMapper;
import com.hostelhelp.hostelservice.model.Hostel;
import com.hostelhelp.hostelservice.repository.HostelRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class HostelService {

    private final HostelRepository hostelRepository;

    public HostelService(HostelRepository hostelRepository) {
        this.hostelRepository = hostelRepository;
    }

    // Get all hostels
    public List<HostelResponseDTO> getHostels() {
        List<Hostel> hostels = hostelRepository.findAll();
        return hostels.stream().map(HostelMapper::toDTO).toList();
    }

    // Create hostel
    public HostelResponseDTO createHostel(HostelRequestDTO hostelRequestDTO) {
        if (hostelRepository.existsByName(hostelRequestDTO.getName())) {
            throw new HostelAlreadyExistsException(
                    "A hostel with this name already exists: " + hostelRequestDTO.getName()
            );
        }

        Hostel newHostel = hostelRepository.save(
                HostelMapper.toModel(hostelRequestDTO)
        );

        return HostelMapper.toDTO(newHostel);
    }

    // Update hostel
    public HostelResponseDTO updateHostel(UUID id, HostelRequestDTO hostelRequestDTO) {
        Hostel hostel = hostelRepository.findById(id).orElseThrow(() ->
                new HostelNotFoundException("Hostel not found with id " + id));

        if (hostelRepository.existsByNameAndIdNot(hostelRequestDTO.getName(), id)) {
            throw new HostelAlreadyExistsException(
                    "A hostel with this name already exists: " + hostelRequestDTO.getName()
            );
        }

        hostel.setName(hostelRequestDTO.getName());
        hostel.setHasAC(hostelRequestDTO.getHasAC());
        hostel.setNumberOfRooms(hostelRequestDTO.getNumberOfRooms());
        hostel.setNumberOfSeatsPerRoom(hostelRequestDTO.getNumberOfSeatsPerRoom());
        hostel.setBoysHostel(hostelRequestDTO.getIsBoysHostel());

        Hostel updatedHostel = hostelRepository.save(hostel);

        return HostelMapper.toDTO(updatedHostel);
    }

    // Delete hostel
    public void deleteHostel(UUID id) {
        if (!hostelRepository.existsById(id)) {
            throw new HostelNotFoundException("Hostel not found with id " + id);
        }
        hostelRepository.deleteById(id);
    }
}
