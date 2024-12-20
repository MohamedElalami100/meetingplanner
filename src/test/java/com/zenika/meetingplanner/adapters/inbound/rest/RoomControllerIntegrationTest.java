package com.zenika.meetingplanner.adapters.inbound.rest;

import com.zenika.meetingplanner.adapters.outbound.jpa.entities.JpaEquipment;
import com.zenika.meetingplanner.adapters.outbound.jpa.entities.JpaMeeting;
import com.zenika.meetingplanner.adapters.outbound.jpa.entities.JpaMeetingType;
import com.zenika.meetingplanner.adapters.outbound.jpa.entities.JpaRoom;
import com.zenika.meetingplanner.adapters.outbound.jpa.repositories.JpaEquipmentRepository;
import com.zenika.meetingplanner.adapters.outbound.jpa.repositories.JpaMeetingTypeRepository;
import com.zenika.meetingplanner.adapters.outbound.jpa.repositories.JpaRoomRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RoomControllerIntegrationTest {

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

        // Create and save the equipment first
        JpaEquipment equipment1 = JpaEquipment.builder().name("Projector").build();
        JpaEquipment equipment2 = JpaEquipment.builder().name("Whiteboard").build();

        // Save the equipment objects
        JpaEquipment savedEquipment1 = equipmentRepository.save(equipment1);
        JpaEquipment savedEquipment2 = equipmentRepository.save(equipment2);

        // Create meeting types with all required fields
        JpaMeetingType meetingType = JpaMeetingType.builder()
                .name("SPEC")
                .minimumCapacity(10)
                .requiredEquipment(List.of(savedEquipment1)) // Ensure required equipment objects
                .build();
        meetingTypeRepository.save(meetingType);

        // Create a room with all necessary fields populated
        JpaRoom room = JpaRoom.builder()
                .name("Conference Room A")
                .capacity(20)
                .equipments(new ArrayList<>(List.of(savedEquipment1, savedEquipment2))) // Use mutable list
                .reservations(new ArrayList<>()) // Start with an empty mutable reservations list
                .build();
        JpaRoom savedRoom = roomRepository.save(room);

        // Create a meeting associated with the saved room
        JpaMeeting meeting = JpaMeeting.builder()
                .date(LocalDate.of(2024, 12, 1))
                .hour(10)
                .room(savedRoom) // Assign the room to the meeting
                .type(meetingType) // Assign the meeting type
                .build();

        // Add the meeting to the room's reservations
        savedRoom.getReservations().add(meeting);
        roomRepository.save(savedRoom); // Save room with meeting added
    }


    @Test
    void testGetBestRoomWithAvailableHours_ShouldReturnRoomDetails() {
        try {
            mockMvc.perform(get("/api/rooms/best-room-with-available-hours")
                            .param("meetingType", "SPEC")
                            .param("requiredCapacity", "10")
                            .param("meetingDate", "2024-12-01")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Conference Room A"))
                    .andExpect(jsonPath("$.capacity").value(20))
                    .andExpect(jsonPath("$.roomEquipments[0]").value("Projector"))
                    .andExpect(jsonPath("$.availableHours[0]").value("8h00-9h00"));
        } catch (Exception e) {
        }
    }

    @Test
    void testGetBestRoomWithAvailableHours_ShouldReturnNotFound_WhenNoRoomAvailable() {
        try {
            mockMvc.perform(get("/api/rooms/best-room-with-available-hours")
                            .param("meetingType", "SPEC")
                            .param("requiredCapacity", "25") // Exceeds capacity of all rooms
                            .param("meetingDate", "2024-12-01")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string("No suitable room found for the given meeting type and capacity."));
        } catch (Exception e) {
        }
    }

    @Test
    void testGetBestRoomWithAvailableHours_ShouldReturnBadRequest_WhenInvalidDateFormat() {
        try {
            mockMvc.perform(get("/api/rooms/best-room-with-available-hours")
                            .param("meetingType", "SPEC")
                            .param("requiredCapacity", "10")
                            .param("meetingDate", "invalid-date") // Invalid date format
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Invalid date format: invalid-date"));
        } catch (Exception e) {
        }
    }

    @AfterAll
    void tearDownDatabase() {
        // Clean up database after tests
        roomRepository.deleteAll();
        meetingTypeRepository.deleteAll();
    }
}

