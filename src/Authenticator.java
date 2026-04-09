import java.util.Optional;

/**
 * Narrow port for sign-in. {@link UserDirectoryAuthenticator} adapts {@link UserDirectory} to this
 * interface (Adapter pattern): {@code LoginFrame} depends on {@code Authenticator}, not on the
 * concrete user store.
 */
public interface Authenticator {
    Optional<Session> authenticate(String username, String password);
}
