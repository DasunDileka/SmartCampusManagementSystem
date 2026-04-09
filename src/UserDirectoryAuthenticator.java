import java.util.Optional;

/** Adapter (structural): wraps {@link UserDirectory} so callers use {@link Authenticator}. */
public final class UserDirectoryAuthenticator implements Authenticator {
    private final UserDirectory directory;

    public UserDirectoryAuthenticator(UserDirectory directory) {
        this.directory = directory;
    }

    @Override
    public Optional<Session> authenticate(String username, String password) {
        return directory.authenticate(username, password);
    }
}
