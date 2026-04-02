import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.SwingUtilities;

/**
 * Subject in the Observer pattern: publishes {@link Notification} events to registered listeners.
 */
public class NotificationBus {

    /** Recipients with this key are delivered to every admin session listener. */
    public static final String ADMINS_BROADCAST = "@admins";

    /** Campus-wide announcements to all student sessions. */
    public static final String STUDENTS_BROADCAST = "@students";

    private final List<NotificationListener> listeners = new CopyOnWriteArrayList<>();

    public void subscribe(NotificationListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(NotificationListener listener) {
        listeners.remove(listener);
    }

    public void publish(Notification notification) {
        for (NotificationListener listener : listeners) {
            SwingUtilities.invokeLater(() -> listener.notificationReceived(notification));
        }
    }
}
