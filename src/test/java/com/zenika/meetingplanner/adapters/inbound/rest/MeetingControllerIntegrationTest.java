package com.zenika.meetingplanner.adapters.inbound.rest;

import com.zenika.meetingplanner.adapters.outbound.jpa.entities.JpaEquipment;
import com.zenika.meetingplanner.adapters.outbound.jpa.entities.JpaMeeting;
import com.zenika.meetingplanner.adapters.outbound.jpa.entities.JpaMeetingType;
import com.zenika.meetingplanner.adapters.outbound.jpa.entities.JpaRoom;
import com.zenika.meetingplanner.adapters.outbound.jpa.repositories.JpaEquipmentRepository;
import com.zenika.meetingplanner.adapters.outbound.jpa.repositories.JpaMeetingTypeRepository;
import com.zenika.meetingplanner.adapters.outbound.jpa.repositories.JpaRoomRepository;
import com.zenika.meetingplanner.common.dtos.MeetingRequestDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MeetingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaRoomRepository roomRepository;

    @Autowired
    private JpaMeetingTypeRepository meetingTypeRepository;

    @Autowired
    private JpaEquipmentRepository equipmentRepository;

    @BeforeAll
    @Transactional
    void setUpDatabase() {
        // Clear and populate the test database
        roomRepository.deleteAll();
        meetingTypeRepository.deleteAll();
        equipmentRepository.deleteAll();

        // Create and save equipment
        JpaEquipment equipment = JpaEquipment.builder().name("Projector").build();
        JpaEquipment savedEquipment = equipmentRepository.save(equipment);

        // Create a meeting type
        JpaMeetingType meetingType = JpaMeetingType.builder()
                .name("SPEC")
                .minimumCapacity(10)
                .requiredEquipment(List.of(savedEquipment))
                .build();
        meetingTypeRepository.save(meetingType);

        // Create and save a room
        JpaRoom room = JpaRoom.builder()
                .name("Conference Room A")
                .capacity(20)
                .equipments(new ArrayList<>(List.of(savedEquipment)))
                .reservations(new ArrayList<>())
                .build();
        JpaRoom savedRoom = roomRepository.save(room); // Save the room first

        // Create a meeting associated with the saved room
        JpaMeeting meeting = JpaMeeting.builder()
                .date(LocalDate.of(2024, 12, 1))
                .hour(10)
                .room(savedRoom) // Assign the saved room
                .type(meetingType) // Assign the meeting type
                .build();

        // Save the meeting
        savedRoom.getReservations().add(meeting); // Add meeting to room's reservations
        roomRepository.save(savedRoom); // Save the room with the updated reservations
    }


    @Test
    void testAssignMeetingToBestRoom_ShouldReturnMeetingDetails() {
        try {
            mockMvc.perform(post("/api/meetings/assign-to-best-room")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "meetingType": "SPEC",
                                        "requiredCapacity": 10,
                                        "meetingDate": "2024-12-01",
                                        "meetingHour": 15
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.assignedRoomName").value("Conference Room A"))
                    .andExpect(jsonPath("$.meetingType").value("SPEC"))
                    .andExpect(jsonPath("$.meetingHour").value(15));
        } catch (Exception e) {
        }
    }

    @Test
    void testAssignMeetingToBestRoom_ShouldReturnNotFound_WhenNoRoomAvailable() {
        try {
            mockMvc.perform(post("/api/meetings/assign-to-best-room")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "meetingType": "SPEC",
                                        "requiredCapacity": 25,
                                        "meetingDate": "2024-12-01",
                                        "meetingHour": 10
                                    }
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string("No suitable room found for the given meeting type, capacity, and time."));
        } catch (Exception e) {
        }
    }

    @Test
    void testAssignMeetingToBestRoom_ShouldReturnBadRequest_WhenInvalidRequest(){
        try {
            mockMvc.perform(post("/api/meetings/assign-to-best-room")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "meetingType": "",
                                        "requiredCapacity": -1,
                                        "meetingDate": "invalid-date",
                                        "meetingHour": -5
                                    }
                                    """))
                    .andExpect(status().isBadRequest());
        } catch (Exception e) {
        }
    }

    @AfterAll
    void tearDownDatabase() {
        // Clean up database after tests
        roomRepository.deleteAll();
        meetingTypeRepository.deleteAll();
        equipmentRepository.deleteAll();
    }
}
