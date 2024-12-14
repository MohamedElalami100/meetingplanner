package com.zenika.meetingplanner;

import com.zenika.meetingplanner.adapters.outbound.jpa.entities.JpaEquipment;
import com.zenika.meetingplanner.adapters.outbound.jpa.entities.JpaMeeting;
import com.zenika.meetingplanner.adapters.outbound.jpa.entities.JpaMeetingType;
import com.zenika.meetingplanner.adapters.outbound.jpa.entities.JpaRoom;
import com.zenika.meetingplanner.adapters.outbound.jpa.repositories.JpaEquipmentRepository;
import com.zenika.meetingplanner.adapters.outbound.jpa.repositories.JpaMeetingRepository;
import com.zenika.meetingplanner.adapters.outbound.jpa.repositories.JpaMeetingTypeRepository;
import com.zenika.meetingplanner.adapters.outbound.jpa.repositories.JpaRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private JpaRoomRepository roomRepository;

    @Autowired
    private JpaMeetingTypeRepository meetingTypeRepository;

    @Autowired
    private JpaMeetingRepository meetingRepository;

    @Autowired
    private JpaEquipmentRepository equipmentRepository;

    @Override
    public void run(String... args) throws Exception {

        // Create and save Equipment
        JpaEquipment projector = JpaEquipment.builder().name("Projector").build();
        JpaEquipment whiteboard = JpaEquipment.builder().name("Whiteboard").build();
        JpaEquipment tv = JpaEquipment.builder().name("TV").build();
        JpaEquipment laptop = JpaEquipment.builder().name("Laptop").build();

        equipmentRepository.saveAll(Arrays.asList(projector, whiteboard, tv, laptop));

        // Create and save Meeting Types
        JpaMeetingType type1 = JpaMeetingType.builder()
                .name("Conference")
                .minimumCapacity(10)
                .requiredEquipment(Arrays.asList(projector, whiteboard))
                .build();

        JpaMeetingType type2 = JpaMeetingType.builder()
                .name("Training")
                .minimumCapacity(15)
                .requiredEquipment(Arrays.asList(laptop, whiteboard))
                .build();

        meetingTypeRepository.saveAll(Arrays.asList(type1, type2));

        // Create and save Rooms
        JpaRoom room1 = JpaRoom.builder()
                .name("Room A")
                .capacity(20)
                .equipments(Arrays.asList(projector, whiteboard, tv))
                .build();

        JpaRoom room2 = JpaRoom.builder()
                .name("Room B")
                .capacity(10)
                .equipments(Arrays.asList(whiteboard))
                .build();

        roomRepository.saveAll(Arrays.asList(room1, room2));

        // Create and save Meetings
        JpaMeeting meeting1 = JpaMeeting.builder()
                .type(type1)
                .participantCount(12)
                .date(LocalDate.now())
                .hour(LocalTime.now().minusHours(1).getHour())
                .room(room1)
                .build();

        JpaMeeting meeting2 = JpaMeeting.builder()
                .type(type2)
                .participantCount(8)
                .date(LocalDate.now().plusDays(1))
                .hour(LocalTime.now().plusHours(2).getHour())
                .room(room2)
                .build();

        meetingRepository.saveAll(Arrays.asList(meeting1, meeting2));
    }
}
