package com.hostelhelp.wardenservice.repository;

import com.hostelhelp.wardenservice.model.Warden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WardenRepository extends JpaRepository<Warden, UUID> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);
    Optional<Warden> findByEmail(String email);

    // Fetch wardens that belong to a hostel
    List<Warden> findByHostelId(String hostelId);
}
