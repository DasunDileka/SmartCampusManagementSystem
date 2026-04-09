import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingService {
    private final RoomService roomService;
    private final NotificationBus notificationBus;
    private final IdGenerator ids = new IdGenerator("BK");
    private final List<Booking> bookings = new ArrayList<>();

    public BookingService(RoomService roomService, NotificationBus notificationBus) {
        this.roomService = roomService;
        this.notificationBus = notificationBus;
    }

    public List<Booking> allBookings() {
        return new ArrayList<>(bookings);
    }

    public List<Booking> bookingsForUser(String username) {
        List<Booking> list = new ArrayList<>();
        for (Booking b : bookings) {
            if (username.equals(b.getUsername()) && !b.isCancelled()) {
                list.add(b);
            }
        }
        return list;
    }

    /**
     * Half-open interval [start, end): overlaps if existing.start &lt; end && start &lt; existing.end.
     */
    public boolean hasOverlap(String roomId, LocalDateTime start, LocalDateTime end, Booking except) {
        if (!end.isAfter(start)) {
            return true;
        }
        for (Booking b : bookings) {
            if (b.isCancelled() || !roomId.equals(b.getRoomId())) {
                continue;
            }
            if (except != null && except.getId().equals(b.getId())) {
                continue;
            }
            if (b.getStart().isBefore(end) && start.isBefore(b.getEnd())) {
                return true;
            }
        }
        return false;
    }

    public Optional<String> book(String roomId, String username, LocalDateTime start, LocalDateTime end) {
        Optional<Room> room = roomService.get(roomId);
        if (room.isEmpty() || !room.get().isActive()) {
            return Optional.of("Room is not available.");
        }
        if (hasOverlap(roomId, start, end, null)) {
            return Optional.of("That room is already booked for part of this time.");
        }
        Booking b = new Booking(ids.next(), roomId, username, start, end);
        bookings.add(b);
        notificationBus.publish(Notification.builder()
                .recipientKey(username)
                .title("Booking confirmed")
                .body("Room " + roomId + " from " + start + " to " + end)
                .createdAt(LocalDateTime.now())
                .build());
        return Optional.empty();
    }

    /**
     * Creates one booking per week for {@code weeks} occurrences (including the first interval).
     */
    public Optional<String> bookRecurring(String roomId, String username,
                                          LocalDateTime firstStart, LocalDateTime firstEnd, int weeks) {
        if (weeks < 1 || weeks > 52) {
            return Optional.of("Weeks must be between 1 and 52.");
        }
        long minutes = ChronoUnit.MINUTES.between(firstStart, firstEnd);
        if (minutes <= 0) {
            return Optional.of("End must be after start.");
        }
        Optional<Room> room = roomService.get(roomId);
        if (room.isEmpty() || !room.get().isActive()) {
            return Optional.of("Room is not available.");
        }
        for (int w = 0; w < weeks; w++) {
            LocalDateTime s = firstStart.plusWeeks(w);
            LocalDateTime e = firstEnd.plusWeeks(w);
            if (hasOverlap(roomId, s, e, null)) {
                return Optional.of("Recurring series conflicts at week " + (w + 1) + ".");
            }
        }
        for (int w = 0; w < weeks; w++) {
            LocalDateTime s = firstStart.plusWeeks(w);
            LocalDateTime e = firstEnd.plusWeeks(w);
            Booking b = new Booking(ids.next(), roomId, username, s, e);
            bookings.add(b);
        }
        notificationBus.publish(Notification.builder()
                .recipientKey(username)
                .title("Recurring bookings confirmed")
                .body(weeks + " weekly slot(s) for room " + roomId + " starting " + firstStart)
                .createdAt(LocalDateTime.now())
                .build());
        return Optional.empty();
    }

    public Optional<String> cancelBooking(String bookingId, String username, boolean admin) {
        for (Booking b : bookings) {
            if (!b.getId().equals(bookingId) || b.isCancelled()) {
                continue;
            }
            if (!admin && !username.equals(b.getUsername())) {
                return Optional.of("You can only cancel your own bookings.");
            }
            b.setCancelled(true);
            notificationBus.publish(Notification.builder()
                    .recipientKey(b.getUsername())
                    .title("Booking cancelled")
                    .body("Room " + b.getRoomId() + " " + b.getStart() + " – " + b.getEnd())
                    .createdAt(LocalDateTime.now())
                    .build());
            return Optional.empty();
        }
        return Optional.of("Booking not found.");
    }
}
