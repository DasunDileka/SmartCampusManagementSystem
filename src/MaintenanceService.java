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
        notificationBus.publish(new Notification(
                reportedBy,
                "Maintenance request submitted",
                "Request " + r.getId() + " for room " + roomId + ": " + description,
                LocalDateTime.now()));
        notificationBus.publish(new Notification(
                NotificationBus.ADMINS_BROADCAST,
                "New maintenance request",
                r.getId() + " — " + roomId + " (" + urgency + "): " + description,
                LocalDateTime.now()));
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
            notificationBus.publish(new Notification(
                    r.getReportedBy(),
                    "Maintenance update",
                    detail,
                    LocalDateTime.now()));
            notificationBus.publish(new Notification(
                    NotificationBus.ADMINS_BROADCAST,
                    "Maintenance update",
                    detail,
                    LocalDateTime.now()));
            return Optional.empty();
        }
        return Optional.of("Request not found.");
    }
}
