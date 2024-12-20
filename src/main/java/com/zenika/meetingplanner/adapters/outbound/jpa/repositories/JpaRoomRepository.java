package com.zenika.meetingplanner.adapters.outbound.jpa.repositories;

import com.zenika.meetingplanner.adapters.outbound.jpa.entities.JpaRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRoomRepository extends JpaRepository<JpaRoom, Long> {
}
