public enum RequestStatus {
    PENDING,
    ASSIGNED,
    COMPLETED,
    /** Student booking request awaiting admin approval */
    BOOKING_PENDING,
    BOOKING_APPROVED,
    BOOKING_REJECTED;

    public boolean isMaintenanceStatus() {
        return this == PENDING || this == ASSIGNED || this == COMPLETED;
    }
}