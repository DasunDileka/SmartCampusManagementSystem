import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Booking requests, admin approval path, and related notification titles.
 */
class RoomServiceBookingWorkflowTest {

    private static final String ROOM = "W101";

    private NotificationBus bus;
    private RecordingNotificationListener recorder;
    private RoomService rooms;
    private BookingService bookings;

    @BeforeEach
    void setUp() {
        bus = new NotificationBus();
        recorder = new RecordingNotificationListener();
        bus.subscribe(recorder);
        rooms = new RoomService(bus);
        rooms.addRoom(new Room(ROOM, 20, "chairs", true));
        bookings = new BookingService(rooms, bus);
        rooms.attachBookingService(bookings);
    }

    @Test
    void submitBookingRequest_pending_thenApprove_createsBookingAndExtraNotifications() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 10, 13, 0);
        LocalDateTime end = start.plusHours(2);

        Optional<String> submitErr = rooms.submitBookingRequest(ROOM, "stu", start, end);
        assertTrue(submitErr.isEmpty());
        TestEdt.flushEdt();

        List<RoomService.RoomBookingRequest> pending = rooms.pendingBookingRequests();
        assertEquals(1, pending.size());
        assertEquals(RequestStatus.BOOKING_PENDING, pending.get(0).getStatus());

        List<Notification> afterSubmit = recorder.notifications();
        assertEquals(2, afterSubmit.size());
        assertEquals("Booking request submitted", afterSubmit.get(0).title());
        assertEquals("New booking request", afterSubmit.get(1).title());

        recorder.clear();
        String reqId = pending.get(0).getId();

        Optional<String> approveErr = rooms.approveBookingRequest(reqId);

        assertTrue(approveErr.isEmpty());
        TestEdt.flushEdt();
        assertTrue(rooms.pendingBookingRequests().isEmpty());
        List<RoomService.RoomBookingRequest> after = rooms.bookingRequestsForUser("stu");
        assertEquals(1, after.size());
        assertEquals(RequestStatus.BOOKING_APPROVED, after.get(0).getStatus());
        assertEquals(1, bookings.bookingsForUser("stu").size());

        List<Notification> afterApprove = recorder.notifications();
        assertTrue(afterApprove.stream().anyMatch(n -> "Booking request approved".equals(n.title())));
        assertTrue(afterApprove.stream().anyMatch(n -> "Booking confirmed".equals(n.title())));
    }

    @Test
    void submitBookingRequest_invalidInterval_rejected() {
        LocalDateTime t = LocalDateTime.of(2026, 5, 5, 10, 0);
        Optional<String> err = rooms.submitBookingRequest(ROOM, "u", t, t);

        assertEquals(Optional.of("End must be after start."), err);
    }

    @Test
    void approveBookingRequest_unknownId_rejected() {
        Optional<String> err = rooms.approveBookingRequest("BR999");

        assertEquals(Optional.of("Request not found."), err);
    }

    @Test
    void rejectBookingRequest_pending_only() {
        LocalDateTime start = LocalDateTime.of(2026, 6, 1, 9, 0);
        rooms.submitBookingRequest(ROOM, "stu", start, start.plusHours(1));
        String id = rooms.pendingBookingRequests().get(0).getId();

        Optional<String> err = rooms.rejectBookingRequest(id);

        assertTrue(err.isEmpty());
        TestEdt.flushEdt();
        assertTrue(recorder.notifications().stream().anyMatch(n -> "Booking request rejected".equals(n.title())));
    }
}
