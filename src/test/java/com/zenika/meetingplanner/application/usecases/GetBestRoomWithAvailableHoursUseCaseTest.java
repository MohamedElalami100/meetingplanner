package com.zenika.meetingplanner.application.usecases;

import com.zenika.meetingplanner.application.ports.MeetingTypeRepositoryPort;
import com.zenika.meetingplanner.application.ports.RoomRepositoryPort;
import com.zenika.meetingplanner.common.dtos.RoomWithAvailableHoursDto;
import com.zenika.meetingplanner.common.exceptions.MeetingTypeNotFoundException;
import com.zenika.meetingplanner.common.exceptions.NoSuitableRoomException;
import com.zenika.meetingplanner.domain.Equipment;
import com.zenika.meetingplanner.domain.Meeting;
import com.zenika.meetingplanner.domain.MeetingType;
import com.zenika.meetingplanner.domain.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetBestRoomWithAvailableHoursUseCaseTest {

    @Mock
    private RoomRepositoryPort roomRepository;

    @Mock
    private MeetingTypeRepositoryPort meetingTypeRepositoryPort;

    @InjectMocks
    private GetBestRoomWithAvailableHoursUseCase getBestRoomWithAvailableHoursUseCase;

    private MeetingType meetingType;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize meeting type
        meetingType = MeetingType.builder()
                .name("SPEC")
                .minimumCapacity(5)
                .requiredEquipment(List.of(new Equipment("Whiteboard"), new Equipment("Projector")))
                .build();
    }

    @Test
    void testExecute_SuccessfulRoomSelection() {
        // Arrange
        LocalDate meetingDate = LocalDate.of(2024, 12, 1);

        Room bestRoom = Room.builder()
                .name("Conference Room A")
                .capacity(15)
                .equipments(List.of(new Equipment("Whiteboard"), new Equipment("Projector")))
                .reservations(List.of())
                .build();

        when(meetingTypeRepositoryPort.findByName("SPEC")).thenReturn(Optional.of(meetingType));
        when(roomRepository.findAllRooms()).thenReturn(List.of(bestRoom));

        // Act
        RoomWithAvailableHoursDto responseDto = getBestRoomWithAvailableHoursUseCase.execute("SPEC", 10, meetingDate);

        // Assert
        assertNotNull(responseDto);
        assertEquals("Conference Room A", responseDto.getName());
        assertEquals(15, responseDto.getCapacity());
        assertTrue(responseDto.getRoomEquipments().contains("Whiteboard"));
        assertTrue(responseDto.getAvailableHours().contains("8h00-9h00"));
        assertTrue(responseDto.getAvailableHours().contains("9h00-10h00"));

        // Verify interactions
        verify(meetingTypeRepositoryPort, times(1)).findByName("SPEC");
        verify(roomRepository, times(1)).findAllRooms();
    }

    @Test
    void testExecute_MeetingTypeNotFound_ShouldThrowException() {
        // Arrange
        when(meetingTypeRepositoryPort.findByName("SPEC")).thenReturn(Optional.empty());

        // Act & Assert
        MeetingTypeNotFoundException exception = assertThrows(MeetingTypeNotFoundException.class,
                () -> getBestRoomWithAvailableHoursUseCase.execute("SPEC", 10, LocalDate.of(2024, 12, 1)));

        assertEquals("Meeting type 'SPEC' not found.", exception.getMessage());

        // Verify no further interactions
        verify(meetingTypeRepositoryPort, times(1)).findByName("SPEC");
        verifyNoInteractions(roomRepository);
    }

    @Test
    void testExecute_NoSuitableRoom_ShouldThrowException() {
        // Arrange
        LocalDate meetingDate = LocalDate.of(2024, 12, 1);

        when(meetingTypeRepositoryPort.findByName("SPEC")).thenReturn(Optional.of(meetingType));
        when(roomRepository.findAllRooms()).thenReturn(List.of()); // No rooms available

        // Act & Assert
        NoSuitableRoomException exception = assertThrows(NoSuitableRoomException.class,
                () -> getBestRoomWithAvailableHoursUseCase.execute("SPEC", 10, meetingDate));

        assertEquals("No suitable room found for the given meeting type and capacity.", exception.getMessage());

        // Verify interactions
        verify(meetingTypeRepositoryPort, times(1)).findByName("SPEC");
        verify(roomRepository, times(1)).findAllRooms();
    }

    @Test
    void testExecute_RoomWithoutSufficientCapacity_ShouldThrowException() {
        // Arrange
        LocalDate meetingDate = LocalDate.of(2024, 12, 1);

        Room unsuitableRoom = Room.builder()
                .name("Small Room")
                .capacity(5) // Insufficient capacity
                .equipments(List.of(new Equipment("Whiteboard")))
                .reservations(List.of())
                .build();

        when(meetingTypeRepositoryPort.findByName("SPEC")).thenReturn(Optional.of(meetingType));
        when(roomRepository.findAllRooms()).thenReturn(List.of(unsuitableRoom));

        // Act & Assert
        NoSuitableRoomException exception = assertThrows(NoSuitableRoomException.class,
                () -> getBestRoomWithAvailableHoursUseCase.execute("SPEC", 10, meetingDate));

        assertEquals("No suitable room found for the given meeting type and capacity.", exception.getMessage());

        // Verify interactions
        verify(meetingTypeRepositoryPort, times(1)).findByName("SPEC");
        verify(roomRepository, times(1)).findAllRooms();
    }

    @Test
    void testExecute_RoomWithConflictingReservations_ShouldThrowException() {
        // Arrange
        LocalDate meetingDate = LocalDate.of(2024, 12, 1);

        Room conflictingRoom = Room.builder()
                .name("Conference Room A")
                .capacity(15)
                .equipments(List.of(new Equipment("Whiteboard"), new Equipment("Projector")))
                .reservations(List.of(
                        Meeting.builder().date(LocalDate.of(2024, 12, 1)).hour(8).build(),
                        Meeting.builder().date(LocalDate.of(2024, 12, 1)).hour(10).build(),
                        Meeting.builder().date(LocalDate.of(2024, 12, 1)).hour(12).build(),
                        Meeting.builder().date(LocalDate.of(2024, 12, 1)).hour(14).build(),
                        Meeting.builder().date(LocalDate.of(2024, 12, 1)).hour(16).build(),
                        Meeting.builder().date(LocalDate.of(2024, 12, 1)).hour(18).build()
                ))
                .build();

        when(meetingTypeRepositoryPort.findByName("SPEC")).thenReturn(Optional.of(meetingType));
        when(roomRepository.findAllRooms()).thenReturn(List.of(conflictingRoom));

        // Act & Assert
        NoSuitableRoomException exception = assertThrows(NoSuitableRoomException.class,
                () -> getBestRoomWithAvailableHoursUseCase.execute("SPEC", 10, meetingDate));

        assertEquals("No suitable room found for the given meeting type and capacity.", exception.getMessage());

        // Verify interactions
        verify(meetingTypeRepositoryPort, times(1)).findByName("SPEC");
        verify(roomRepository, times(1)).findAllRooms();
    }
}
