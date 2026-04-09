import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RoomService {
    private final Map<String, Room> rooms = new LinkedHashMap<>();
    private final NotificationBus notificationBus;
    private final IdGenerator bookingRequestIds = new IdGenerator("BR");
    private final List<RoomBookingRequest> bookingRequests = new ArrayList<>();
    private BookingService bookingService;

    public RoomService(NotificationBus notificationBus) {
        this.notificationBus = notificationBus;
    }

    /**
     * Resolves circular construction with {@link BookingService}; call once from {@link AppServices}.
     */
    public void attachBookingService(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    public void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public Optional<Room> get(String id) {
        return Optional.ofNullable(rooms.get(id));
    }

    public List<Room> allRooms() {
        return new ArrayList<>(rooms.values());
    }

    public List<Room> activeRooms() {
        List<Room> list = new ArrayList<>();
        for (Room r : rooms.values()) {
            if (r.isActive()) {
                list.add(r);
            }
        }
        return list;
    }

    public List<RoomBookingRequest> pendingBookingRequests() {
        List<RoomBookingRequest> list = new ArrayList<>();
        for (RoomBookingRequest r : bookingRequests) {
            if (r.getStatus() == RequestStatus.BOOKING_PENDING) {
                list.add(r);
            }
        }
        return list;
    }

    public List<RoomBookingRequest> bookingRequestsForUser(String username) {
        List<RoomBookingRequest> list = new ArrayList<>();
        for (RoomBookingRequest r : bookingRequests) {
            if (username.equals(r.getUsername())) {
                list.add(r);
            }
        }
        return list;
    }

    private static boolean intervalsOverlap(String roomId, LocalDateTime start, LocalDateTime end,
                                            String otherRoomId, LocalDateTime otherStart, LocalDateTime otherEnd) {
        return roomId.equals(otherRoomId)
                && otherStart.isBefore(end)
                && start.isBefore(otherEnd);
    }

    private boolean overlapsPendingBooking(String roomId, LocalDateTime start, LocalDateTime end,
                                             RoomBookingRequest except) {
        for (RoomBookingRequest r : bookingRequests) {
            if (r.getStatus() != RequestStatus.BOOKING_PENDING) {
                continue;
            }
            if (except != null && except.getId().equals(r.getId())) {
                continue;
            }
            if (intervalsOverlap(roomId, start, end, r.getRoomId(), r.getStart(), r.getEnd())) {
                return true;
            }
        }
        return false;
    }

    public Optional<String> submitBookingRequest(String roomId, String username,
                                                   LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            return Optional.of("End must be after start.");
        }
        Optional<Room> room = get(roomId);
        if (room.isEmpty() || !room.get().isActive()) {
            return Optional.of("Room is not available.");
        }
        if (bookingService == null) {
            return Optional.of("Booking service is not ready.");
        }
        if (bookingService.hasOverlap(roomId, start, end, null)) {
            return Optional.of("That room is already booked for part of this time.");
        }
        if (overlapsPendingBooking(roomId, start, end, null)) {
            return Optional.of("Another pending request overlaps this time for that room.");
        }
        RoomBookingRequest r = new RoomBookingRequest(
                bookingRequestIds.next(), roomId, username, start, end, RequestStatus.BOOKING_PENDING);
        bookingRequests.add(r);
        notificationBus.publish(Notification.builder()
                .recipientKey(username)
                .title("Booking request submitted")
                .body("Request " + r.getId() + " for room " + roomId + " from " + start + " to " + end)
                .createdAt(LocalDateTime.now())
                .build());
        notificationBus.publish(Notification.builder()
                .recipientKey(NotificationBus.ADMINS_BROADCAST)
                .title("New booking request")
                .body(r.getId() + " — " + username + ", room " + roomId + ": " + start + " – " + end)
                .createdAt(LocalDateTime.now())
                .build());
        return Optional.empty();
    }

    public Optional<String> approveBookingRequest(String id) {
        if (bookingService == null) {
            return Optional.of("Booking service is not ready.");
        }
        for (RoomBookingRequest r : bookingRequests) {
            if (!r.getId().equals(id)) {
                continue;
            }
            if (r.getStatus() != RequestStatus.BOOKING_PENDING) {
                return Optional.of("Only pending requests can be approved.");
            }
            Optional<String> bookErr = bookingService.book(r.getRoomId(), r.getUsername(), r.getStart(), r.getEnd());
            if (bookErr.isPresent()) {
                return bookErr;
            }
            r.setStatus(RequestStatus.BOOKING_APPROVED);
            notificationBus.publish(Notification.builder()
                    .recipientKey(r.getUsername())
                    .title("Booking request approved")
                    .body("Room " + r.getRoomId() + " from " + r.getStart() + " to " + r.getEnd())
                    .createdAt(LocalDateTime.now())
                    .build());
            return Optional.empty();
        }
        return Optional.of("Request not found.");
    }

    public Optional<String> rejectBookingRequest(String id) {
        for (RoomBookingRequest r : bookingRequests) {
            if (!r.getId().equals(id)) {
                continue;
            }
            if (r.getStatus() != RequestStatus.BOOKING_PENDING) {
                return Optional.of("Only pending requests can be rejected.");
            }
            r.setStatus(RequestStatus.BOOKING_REJECTED);
            notificationBus.publish(Notification.builder()
                    .recipientKey(r.getUsername())
                    .title("Booking request rejected")
                    .body("Room " + r.getRoomId() + " from " + r.getStart() + " to " + r.getEnd())
                    .createdAt(LocalDateTime.now())
                    .build());
            return Optional.empty();
        }
        return Optional.of("Request not found.");
    }

    public static final class RoomBookingRequest {
        private final String id;
        private final String roomId;
        private final String username;
        private final LocalDateTime start;
        private final LocalDateTime end;
        private RequestStatus status;

        RoomBookingRequest(String id, String roomId, String username,
                           LocalDateTime start, LocalDateTime end, RequestStatus status) {
            this.id = id;
            this.roomId = roomId;
            this.username = username;
            this.start = start;
            this.end = end;
            this.status = status;
        }

        public String getId() {
            return id;
        }

        public String getRoomId() {
            return roomId;
        }

        public String getUsername() {
            return username;
        }

        public LocalDateTime getStart() {
            return start;
        }

        public LocalDateTime getEnd() {
            return end;
        }

        public RequestStatus getStatus() {
            return status;
        }

        void setStatus(RequestStatus status) {
            this.status = status;
        }
    }
}
