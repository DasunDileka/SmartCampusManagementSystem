import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Standard bookable grid: 30-minute slots from 08:00 to 18:00.
 */
public final class TimeSlots {

    private static final List<TimeSlot> STANDARD = buildGrid(
            LocalTime.of(8, 0), LocalTime.of(18, 0), 30);

    private TimeSlots() {
    }

    public static List<TimeSlot> standardDay() {
        return STANDARD;
    }

    private static List<TimeSlot> buildGrid(LocalTime open, LocalTime close, int stepMinutes) {
        List<TimeSlot> out = new ArrayList<>();
        LocalTime t = open;
        while (true) {
            LocalTime e = t.plusMinutes(stepMinutes);
            if (e.isAfter(close)) {
                break;
            }
            out.add(new TimeSlot(t, e));
            t = e;
        }
        return Collections.unmodifiableList(out);
    }
}
