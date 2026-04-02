import java.time.LocalDateTime;

public class Booking {
    private final String id;
    private final String roomId;
    private final String username;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private boolean cancelled;

    public Booking(String id, String roomId, String username, LocalDateTime start, LocalDateTime end) {
        this.id = id;
        this.roomId = roomId;
        this.username = username;
        this.start = start;
        this.end = end;
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

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
