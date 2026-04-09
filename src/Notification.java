import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable in-app message. Prefer constructing via {@link #builder()} (Builder pattern).
 */
public final class Notification {
    private final String recipientKey;
    private final String title;
    private final String body;
    private final LocalDateTime createdAt;

    private Notification(String recipientKey, String title, String body, LocalDateTime createdAt) {
        this.recipientKey = recipientKey;
        this.title = title;
        this.body = body;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Recipient username or {@link NotificationBus#ADMINS_BROADCAST} / {@link NotificationBus#STUDENTS_BROADCAST}. */
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

    /**
     * Builder (creational): step-by-step construction for the four fields of {@link Notification}.
     */
    public static final class Builder {
        private String recipientKey;
        private String title;
        private String body;
        private LocalDateTime createdAt = LocalDateTime.now();

        public Builder recipientKey(String recipientKey) {
            this.recipientKey = recipientKey;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Notification build() {
            return new Notification(
                    Objects.requireNonNull(recipientKey, "recipientKey"),
                    Objects.requireNonNull(title, "title"),
                    Objects.requireNonNull(body, "body"),
                    Objects.requireNonNull(createdAt, "createdAt"));
        }
    }
}
