package com.hostelhelp.wardenservice.service;

import com.hostelhelp.wardenservice.dto.WardenRequestDTO;
import com.hostelhelp.wardenservice.dto.WardenResponseDTO;
import com.hostelhelp.wardenservice.exception.EmailAlreadyExistsException;
import com.hostelhelp.wardenservice.exception.WardenNotFoundException;
import com.hostelhelp.wardenservice.mapper.WardenMapper;
import com.hostelhelp.wardenservice.model.Warden;
import com.hostelhelp.wardenservice.repository.WardenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class WardenService {
    private final WardenRepository wardenRepository;

    public WardenService(WardenRepository wardenRepository) {
        this.wardenRepository = wardenRepository;
    }


    public List<WardenResponseDTO> getWardens() {
        List<Warden> wardens = wardenRepository.findAll();

        return wardens.stream().map(WardenMapper::toDTO).toList();

    }

    public WardenResponseDTO createWarden(WardenRequestDTO wardenRequestDTO) {
        if (wardenRepository.existsByEmail(wardenRequestDTO.email())) {
            throw new EmailAlreadyExistsException(
                    "A warden with this email " + "already exists"
                            + wardenRequestDTO.email());
        }

        Warden newWarden = wardenRepository.save(
                WardenMapper.toModel(wardenRequestDTO));


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

    public void deleteWarden(UUID id) {
        if (!wardenRepository.existsById(id)) {
            throw new WardenNotFoundException("Warden not found with id " + id);
        }
        wardenRepository.deleteById(id);
    }
}
