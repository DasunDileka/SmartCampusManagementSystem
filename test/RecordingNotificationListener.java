import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

final class RecordingNotificationListener implements NotificationListener {

    private final List<Notification> received = new CopyOnWriteArrayList<>();

    @Override
    public void notificationReceived(Notification notification) {
        received.add(notification);
    }

    List<Notification> notifications() {
        return List.copyOf(received);
    }

    void clear() {
        received.clear();
    }
}
