import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // fall back to default LAF
        }

        AppServices app = AppServices.get();
        CampusManagementFacade campus = new CampusManagementFacade(app);
        Authenticator auth = new UserDirectoryAuthenticator(app.userDirectory);
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame(auth, campus);
            frame.setVisible(true);
        });
    }
}
