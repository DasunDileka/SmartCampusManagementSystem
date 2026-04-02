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
}
