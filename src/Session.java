public final class Session {
    private final String username;
    private final UserRole role;

    public Session(String username, UserRole role) {
        this.username = username;
        this.role = role;
    }

    public String username() {
        return username;
    }

    public UserRole role() {
        return role;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public boolean isStaff() {
        return role == UserRole.STAFF;
    }

    public boolean isStudent() {
        return role == UserRole.STUDENT;
    }

    /** Full room CRUD and maintenance assignment. */
    public boolean canManageRooms() {
        return isAdmin();
    }

    /** Immediate room booking (no approval workflow). */
    public boolean canBookRoomDirectly() {
        return isStaff();
    }

    /** Submit a booking that requires administrator approval. */
    public boolean canRequestBooking() {
        return isStudent();
    }

    public boolean seesMaintenanceFeatures() {
        return isAdmin() || isStaff();
    }

    public boolean seesUserManagement() {
        return isAdmin();
    }

    public static String roleDisplayName(UserRole role) {
        return switch (role) {
            case ADMIN -> "Administrator";
            case STAFF -> "Staff member";
            case STUDENT -> "Student";
        };
    }
}
