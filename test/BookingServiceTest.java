import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookingServiceTest {

    private static final String ROOM = "LAB1";
    private NotificationBus bus;
    private RecordingNotificationListener listener;
    private RoomService rooms;
    private BookingService bookings;

    @BeforeEach
    void setUp() {
        bus = new NotificationBus();
        listener = new RecordingNotificationListener();
        bus.subscribe(listener);
        rooms = new RoomService(bus);
        rooms.addRoom(new Room(ROOM, 10, "test", true));
        bookings = new BookingService(rooms, bus);
    }

    @Test
    void book_success_createsBookingAndNotification() {
        LocalDateTime start = LocalDateTime.of(2026, 5, 1, 10, 0);
        LocalDateTime end = start.plusHours(1);

        Optional<String> err = bookings.book(ROOM, "alice", start, end);

        assertTrue(err.isEmpty());
        TestEdt.flushEdt();
        List<Booking> list = bookings.bookingsForUser("alice");
        assertEquals(1, list.size());
        assertEquals(ROOM, list.get(0).getRoomId());

        List<Notification> notes = listener.notifications();
        assertEquals(1, notes.size());
        assertEquals("alice", notes.get(0).recipientKey());
        assertEquals("Booking confirmed", notes.get(0).title());
    }

    @Test
    void book_inactiveRoom_fails() {
        rooms.get(ROOM).get().setActive(false);
        LocalDateTime start = LocalDateTime.of(2026, 5, 1, 10, 0);

        Optional<String> err = bookings.book(ROOM, "bob", start, start.plusHours(1));

        assertTrue(err.isPresent());
        assertEquals("Room is not available.", err.get());
        assertTrue(bookings.allBookings().isEmpty());
    }

    @Test
    void book_unknownRoom_fails() {
        LocalDateTime start = LocalDateTime.of(2026, 5, 1, 10, 0);

        Optional<String> err = bookings.book("Nope", "bob", start, start.plusHours(1));

        assertTrue(err.isPresent());
        assertEquals("Room is not available.", err.get());
    }

    @Test
    void book_overlappingInterval_fails() {
        LocalDateTime start = LocalDateTime.of(2026, 6, 1, 9, 0);
        assertTrue(bookings.book(ROOM, "u1", start, start.plusHours(2)).isEmpty());

        Optional<String> err = bookings.book(ROOM, "u2", start.plusHours(1), start.plusHours(3));

        assertTrue(err.isPresent());
        assertEquals("That room is already booked for part of this time.", err.get());
    }

    @Test
    void book_invalidTimeRange_treatedAsOverlap() {
        LocalDateTime t = LocalDateTime.of(2026, 7, 1, 12, 0);
        Optional<String> err = bookings.book(ROOM, "u", t, t);

        assertTrue(err.isPresent());
        assertEquals("That room is already booked for part of this time.", err.get());
    }

    @Test
    void hasOverlap_adjacentBookings_doNotOverlap() {
        LocalDateTime a0 = LocalDateTime.of(2026, 8, 1, 10, 0);
        bookings.book(ROOM, "u", a0, a0.plusHours(1));

        boolean overlap = bookings.hasOverlap(ROOM, a0.plusHours(1), a0.plusHours(2), null);

        assertFalse(overlap);
    }

    @Test
    void cancelBooking_nonOwnerDenied() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 1, 14, 0);
        bookings.book(ROOM, "owner", start, start.plusHours(1));
        String bookingId = bookings.allBookings().get(0).getId();

        Optional<String> err = bookings.cancelBooking(bookingId, "intruder", false);

        assertTrue(err.isPresent());
        assertEquals("You can only cancel your own bookings.", err.get());
    }

    @Test
    void cancelBooking_success_notifiesBooker() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 1, 11, 0);
        bookings.book(ROOM, "owner", start, start.plusHours(1));
        TestEdt.flushEdt();
        listener.clear();

        String bookingId = bookings.allBookings().get(0).getId();

        Optional<String> err = bookings.cancelBooking(bookingId, "owner", false);

        assertTrue(err.isEmpty());
        TestEdt.flushEdt();
        assertEquals("Booking cancelled", listener.notifications().get(0).title());
        assertTrue(bookings.allBookings().get(0).isCancelled());
    }

    @Test
    void bookRecurring_weeksOutOfRange_fails() {
        LocalDateTime s = LocalDateTime.of(2026, 11, 1, 9, 0);
        Optional<String> low = bookings.bookRecurring(ROOM, "u", s, s.plusHours(1), 0);
        Optional<String> high = bookings.bookRecurring(ROOM, "u", s, s.plusHours(1), 53);

        assertEquals(Optional.of("Weeks must be between 1 and 52."), low);
        assertEquals(Optional.of("Weeks must be between 1 and 52."), high);
    }

    @Test
    void bookRecurring_invalidDuration_fails() {
        LocalDateTime s = LocalDateTime.of(2026, 12, 1, 9, 0);
        Optional<String> err = bookings.bookRecurring(ROOM, "u", s, s, 3);

        assertEquals(Optional.of("End must be after start."), err);
    }

    @Test
    void bookRecurring_conflictMidSeries_fails() {
        LocalDateTime first = LocalDateTime.of(2026, 1, 6, 10, 0);
        bookings.book(ROOM, "blocker", first.plusWeeks(2), first.plusWeeks(2).plusHours(1));

        Optional<String> err = bookings.bookRecurring(ROOM, "u", first, first.plusHours(1), 4);

        assertTrue(err.isPresent());
        assertTrue(err.get().startsWith("Recurring series conflicts at week "));
    }
}
