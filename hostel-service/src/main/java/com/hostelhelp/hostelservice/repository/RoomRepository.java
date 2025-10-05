package com.hostelhelp.hostelservice.repository;

import com.hostelhelp.hostelservice.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findByHostelId(UUID hostelId);
}
