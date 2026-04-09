import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory users for demo / prototype. Passwords are stored in plain text (not for production).
 */
public class UserDirectory {
    private final Map<String, UserRecord> users = new LinkedHashMap<>();

    public UserDirectory() {
        putUser("admin", "password123", UserRole.ADMIN);
        putUser("staff", "secret", UserRole.STAFF);
        putUser("student", "student123", UserRole.STUDENT);
    }

    private void putUser(String username, String password, UserRole role) {
        users.put(username.toLowerCase(), new UserRecord(username, password, role));
    }

    public Optional<Session> authenticate(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        UserRecord u = users.get(username.trim().toLowerCase());
        if (u == null || !u.password.equals(password)) {
            return Optional.empty();
        }
        return Optional.of(new Session(u.canonicalUsername, u.role));
    }

    public List<UserRow> allUsers() {
        List<UserRow> list = new ArrayList<>();
        for (UserRecord u : users.values()) {
            list.add(new UserRow(u.canonicalUsername, u.role));
        }
        list.sort(Comparator.comparing(r -> r.username, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    public Optional<String> addUser(String username, String password, UserRole role) {
        if (username == null || username.isBlank()) {
            return Optional.of("Username is required.");
        }
        if (password == null || password.isEmpty()) {
            return Optional.of("Password is required.");
        }
        if (role == null) {
            return Optional.of("Role is required.");
        }
        String key = username.trim().toLowerCase();
        if (users.containsKey(key)) {
            return Optional.of("That username already exists.");
        }
        putUser(username.trim(), password, role);
        return Optional.empty();
    }

    public Optional<String> removeUser(String actingUsername, String targetUsername) {
        if (targetUsername == null || targetUsername.isBlank()) {
            return Optional.of("Select a user.");
        }
        if (actingUsername.equalsIgnoreCase(targetUsername)) {
            return Optional.of("You cannot remove your own account.");
        }
        UserRecord u = users.remove(targetUsername.trim().toLowerCase());
        if (u == null) {
            return Optional.of("User not found.");
        }
        return Optional.empty();
    }

    public Optional<String> setPassword(String username, String newPassword) {
        if (newPassword == null || newPassword.isEmpty()) {
            return Optional.of("Password cannot be empty.");
        }
        UserRecord u = users.get(username.trim().toLowerCase());
        if (u == null) {
            return Optional.of("User not found.");
        }
        u.password = newPassword;
        return Optional.empty();
    }

    public Optional<String> setRole(String username, UserRole newRole) {
        if (newRole == null) {
            return Optional.of("Role is required.");
        }
        UserRecord u = users.get(username.trim().toLowerCase());
        if (u == null) {
            return Optional.of("User not found.");
        }
        if (u.role == UserRole.ADMIN && newRole != UserRole.ADMIN && adminCount() <= 1) {
            return Optional.of("Cannot change role: at least one administrator is required.");
        }
        u.role = newRole;
        return Optional.empty();
    }

    private int adminCount() {
        int n = 0;
        for (UserRecord u : users.values()) {
            if (u.role == UserRole.ADMIN) {
                n++;
            }
        }
        return n;
    }

    public static final class UserRow {
        public final String username;
        public final UserRole role;

        public UserRow(String username, UserRole role) {
            this.username = username;
            this.role = role;
        }
    }

    private static final class UserRecord {
        final String canonicalUsername;
        String password;
        UserRole role;

        UserRecord(String canonicalUsername, String password, UserRole role) {
            this.canonicalUsername = canonicalUsername;
            this.password = password;
            this.role = role;
        }
    }
}
