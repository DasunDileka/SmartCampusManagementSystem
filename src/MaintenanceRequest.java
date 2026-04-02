public class MaintenanceRequest {
    private final String id;
    private final String roomId;
    private final String description;
    private final Urgency urgency;
    private final String reportedBy;
    private RequestStatus status;
    private String assignedTo;

    public MaintenanceRequest(String id, String roomId, String description, Urgency urgency,
                              String reportedBy, RequestStatus status, String assignedTo) {
        this.id = id;
        this.roomId = roomId;
        this.description = description;
        this.urgency = urgency;
        this.reportedBy = reportedBy;
        this.status = status;
        this.assignedTo = assignedTo;
    }

    public String getId() {
        return id;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getDescription() {
        return description;
    }

    public Urgency getUrgency() {
        return urgency;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
}
