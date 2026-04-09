
public final class AppServicesFactory {

    private AppServicesFactory() {
    }

    /** @return the application-wide, fully wired {@link AppServices} instance */
    public static AppServices getServices() {
        return AppServices.get();
    }
}
