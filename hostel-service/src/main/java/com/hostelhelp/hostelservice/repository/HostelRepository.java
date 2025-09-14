package com.hostelhelp.hostelservice.repository;

import com.hostelhelp.hostelservice.model.Hostel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HostelRepository extends JpaRepository<Hostel, UUID> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, UUID id);
}

