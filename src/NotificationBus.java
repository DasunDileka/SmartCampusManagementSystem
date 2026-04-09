import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.SwingUtilities;

/**
 * Behavioural — Observer pattern — <em>Subject</em>:
 * <ul>
 *   <li>{@link #subscribe(NotificationListener)} / {@link #unsubscribe(NotificationListener)} register observers.</li>
 *   <li>{@link #publish(Notification)} notifies all observers (e.g. {@code NotificationsPanel}) of an event.</li>
 * </ul>
 * UI panels implement {@link NotificationListener} and update when notifications arrive, without the
 * publishers (booking, maintenance, etc.) depending on concrete Swing classes.
 */
public class NotificationBus {

    /** Recipients with this key are delivered to every admin session listener. */
    public static final String ADMINS_BROADCAST = "@admins";

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
