import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaintenanceService {
    private final NotificationBus notificationBus;
    private final IdGenerator ids = new IdGenerator("MR");
    private final List<MaintenanceRequest> requests = new ArrayList<>();

    public MaintenanceService(NotificationBus notificationBus) {
        this.notificationBus = notificationBus;
    }

    public MaintenanceRequest report(String roomId, String description, Urgency urgency, String reportedBy) {
        MaintenanceRequest r = new MaintenanceRequest(
                ids.next(), roomId, description, urgency, reportedBy,
                RequestStatus.PENDING, "");
        requests.add(r);
        notificationBus.publish(Notification.builder()
                .recipientKey(reportedBy)
                .title("Maintenance request submitted")
                .body("Request " + r.getId() + " for room " + roomId + ": " + description)
                .createdAt(LocalDateTime.now())
                .build());
        notificationBus.publish(Notification.builder()
                .recipientKey(NotificationBus.ADMINS_BROADCAST)
                .title("New maintenance request")
                .body(r.getId() + " — " + roomId + " (" + urgency + "): " + description)
                .createdAt(LocalDateTime.now())
                .build());
        return r;
    }

    public List<MaintenanceRequest> all() {
        return new ArrayList<>(requests);
    }

    public List<MaintenanceRequest> forUser(String username) {
        List<MaintenanceRequest> list = new ArrayList<>();
        for (MaintenanceRequest r : requests) {
            if (username.equals(r.getReportedBy())) {
                list.add(r);
            }
        }
        return list;
    }

    public Optional<String> updateAdmin(String id, RequestStatus newStatus, String assignedTo) {
        for (MaintenanceRequest r : requests) {
            if (!r.getId().equals(id)) {
                continue;
            }
            RequestStatus old = r.getStatus();
            if (assignedTo != null && !assignedTo.isBlank()) {
                r.setAssignedTo(assignedTo.trim());
            }
            if (newStatus != null) {
                if (!newStatus.isMaintenanceStatus()) {
                    return Optional.of("Use only PENDING, ASSIGNED, or COMPLETED for maintenance.");
                }
                r.setStatus(newStatus);
            }
            if (r.getStatus() == RequestStatus.ASSIGNED
                    && (r.getAssignedTo() == null || r.getAssignedTo().isBlank())) {
                return Optional.of("Set an assignee when status is ASSIGNED.");
            }
            String detail = "Request " + r.getId() + " room " + r.getRoomId()
                    + " — status: " + old + " → " + r.getStatus();
            if (r.getAssignedTo() != null && !r.getAssignedTo().isBlank()) {
                detail += ", assigned: " + r.getAssignedTo();
            }
            notificationBus.publish(Notification.builder()
                    .recipientKey(r.getReportedBy())
                    .title("Maintenance update")
                    .body(detail)
                    .createdAt(LocalDateTime.now())
                    .build());
            notificationBus.publish(Notification.builder()
                    .recipientKey(NotificationBus.ADMINS_BROADCAST)
                    .title("Maintenance update")
                    .body(detail)
                    .createdAt(LocalDateTime.now())
                    .build());
            return Optional.empty();
        }
        return Optional.of("Request not found.");
    }
}
