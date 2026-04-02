import java.time.LocalDateTime;

public final class Notification {
    private final String recipientKey;
    private final String title;
    private final String body;
    private final LocalDateTime createdAt;

    public Notification(String recipientKey, String title, String body, LocalDateTime createdAt) {
        this.recipientKey = recipientKey;
        this.title = title;
        this.body = body;
        this.createdAt = createdAt;
    }

    /** Username or {@link NotificationBus#ADMINS_BROADCAST}. */
    public String recipientKey() {
        return recipientKey;
    }

    public String title() {
        return title;
    }

    public String body() {
        return body;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }
}
