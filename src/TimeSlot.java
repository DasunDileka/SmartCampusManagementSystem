import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * A fixed window on a single calendar day (e.g. 09:00–09:30).
 */
public final class TimeSlot {

    private static final DateTimeFormatter T = DateTimeFormatter.ofPattern("HH:mm");

    private final LocalTime start;
    private final LocalTime end;

    public TimeSlot(LocalTime start, LocalTime end) {
        this.start = start;
        this.end = end;
    }

    public LocalTime start() {
        return start;
    }

    public LocalTime end() {
        return end;
    }

    public LocalDateTime startOn(LocalDate date) {
        return LocalDateTime.of(date, start);
    }

    public LocalDateTime endOn(LocalDate date) {
        return LocalDateTime.of(date, end);
    }

    public String label() {
        return T.format(start) + " – " + T.format(end);
    }

    @Override
    public String toString() {
        return label();
    }
}
