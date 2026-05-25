package com.hostelhelp.wardenservice.service;

import com.hostelhelp.wardenservice.dto.UserDTO;
import com.hostelhelp.wardenservice.dto.WardenRequestDTO;
import com.hostelhelp.wardenservice.dto.WardenResponseDTO;
import com.hostelhelp.wardenservice.exception.EmailAlreadyExistsException;
import com.hostelhelp.wardenservice.exception.WardenNotFoundException;
import com.hostelhelp.wardenservice.mapper.WardenMapper;
import com.hostelhelp.wardenservice.model.Warden;
import com.hostelhelp.wardenservice.repository.WardenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Service
public class WardenService {
    private static final Logger logger = LoggerFactory.getLogger(WardenService.class);

    private final WardenRepository wardenRepository;
    private final RestTemplate restTemplate;

    public WardenService(WardenRepository wardenRepository, RestTemplate restTemplate) {
        this.wardenRepository = wardenRepository;
        this.restTemplate = restTemplate;
    }


    public List<WardenResponseDTO> getWardens() {
        List<Warden> wardens = wardenRepository.findAll();

        return wardens.stream().map(WardenMapper::toDTO).toList();

    }

    public List<WardenResponseDTO> getWardensByHostelId(String hostelId) {
        List<Warden> wardens = wardenRepository.findByHostelId(hostelId);
        return wardens.stream().map(WardenMapper::toDTO).toList();
    }

    public WardenResponseDTO getWarden(UUID id) {
        Warden warden = wardenRepository.findById(id).orElseThrow(() ->
                new WardenNotFoundException("Warden not found with id " + id));

        return WardenMapper.toDTO(warden);
    }

    public WardenResponseDTO createWarden(WardenRequestDTO wardenRequestDTO) {
        if (wardenRepository.existsByEmail(wardenRequestDTO.email())) {
            throw new EmailAlreadyExistsException(
                    "A warden with this email " + "already exists"
                            + wardenRequestDTO.email());
        }

        Warden newWarden = wardenRepository.save(
                WardenMapper.toModel(wardenRequestDTO));

        UserDTO userDTO = new UserDTO(newWarden.getEmail(), newWarden.getPassword(), "WARDEN");
        // Use api-gateway for inter-service auth calls
        restTemplate.postForObject("http://api-gateway:4004/auth/register", userDTO, Void.class);


        return WardenMapper.toDTO(newWarden);
    }

    public WardenResponseDTO updateWarden(UUID id,
                                            WardenRequestDTO wardenRequestDTO) {
        Warden warden = wardenRepository.findById(id).orElseThrow(() ->
                new WardenNotFoundException("Warden not found with id " + id));

        if(wardenRepository.existsByEmailAndIdNot(wardenRequestDTO.email(), id)){
            throw new EmailAlreadyExistsException("A warden with the email "
                    + wardenRequestDTO.email() + " already exists");
        }

        warden.setName(wardenRequestDTO.name());
        warden.setEmail(wardenRequestDTO.email());
        Warden updatedWarden = wardenRepository.save(warden);
        return WardenMapper.toDTO(updatedWarden);
    }

    public WardenResponseDTO getWardenByEmail(String email) {
        Warden warden = wardenRepository.findByEmail(email)
                .orElseThrow(() -> new WardenNotFoundException("Warden not found with email " + email));
        return WardenMapper.toDTO(warden);
    }

    public void deleteWarden(UUID id) {
        Warden warden = wardenRepository.findById(id)
                .orElseThrow(() -> new WardenNotFoundException("Warden not found with id " + id));
        String email = warden.getEmail();
        wardenRepository.deleteById(id);
        // Delete user in auth-service
        try {
            String deleteUrl = "http://api-gateway:4004/auth/user/" + email;
            restTemplate.delete(deleteUrl);
        } catch (Exception e) {
            logger.error("Failed to delete user in auth-service for email: {}", email, e);
        }
    }
}
