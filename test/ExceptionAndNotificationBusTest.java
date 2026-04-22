import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
/**
 * Invalid inputs that surface as {@link Optional} errors or validation exceptions, plus
 * {@link NotificationBus} delivery behaviour.
 */
class ExceptionAndNotificationBusTest {
    @Test
    void notificationBuilder_nullRecipientKey_throws() {
        assertThrows(NullPointerException.class,
                () -> Notification.builder().recipientKey(null).title("t").body("b").build());
    }
    @Test
    void notificationBuilder_nullTitle_throws() {
        assertThrows(NullPointerException.class,
                () -> Notification.builder().recipientKey("u").title(null).body("b").build());
    }
    @Test
    void bookingService_cancel_unknownId_returnsError() {
        NotificationBus bus = new NotificationBus();
        RoomService rooms = new RoomService(bus);
        rooms.addRoom(new Room("X", 5, "", true));
        BookingService bookings = new BookingService(rooms, bus);
        Optional<String> err = bookings.cancelBooking("BK404", "anyone", true);
        assertEquals(Optional.of("Booking not found."), err);
    }
    @Test
    void maintenance_updateCompleted_withoutAssignee_allowedIfAlreadyAssigned() {
        NotificationBus bus = new NotificationBus();
        MaintenanceService ms = new MaintenanceService(bus);
        MaintenanceRequest r = ms.report("Z", "Fix", Urgency.LOW, "rep");
        ms.updateAdmin(r.getId(), RequestStatus.ASSIGNED, "tech");
        Optional<String> err = ms.updateAdmin(r.getId(), RequestStatus.COMPLETED, null);
        assertTrue(err.isEmpty());
        assertEquals(RequestStatus.COMPLETED, ms.all().get(0).getStatus());
    }
    @Test
    void notificationBus_unsubscribe_stopsDelivery() {
        NotificationBus bus = new NotificationBus();
        List<Notification> seen = new ArrayList<>();
        NotificationListener listener = seen::add;
        bus.subscribe(listener);
        bus.publish(sample("first"));
        TestEdt.flushEdt();
        assertEquals(1, seen.size());
        bus.unsubscribe(listener);
        bus.publish(sample("second"));
        TestEdt.flushEdt();
        assertEquals(1, seen.size());
    }
    private static Notification sample(String tag) {
        return Notification.builder()
                .recipientKey("u")
                .title(tag)
                .body("body")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
