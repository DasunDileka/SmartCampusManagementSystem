/**
 * Observer interface in the Observer pattern: receives notifications from {@link NotificationBus}.
 */
@FunctionalInterface
public interface NotificationListener {
    void notificationReceived(Notification notification);
}
