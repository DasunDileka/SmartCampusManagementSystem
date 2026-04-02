import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // fall back to default LAF
        }

        AppServices app = AppServices.get();
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame(app);
            frame.setVisible(true);
        });
    }
}
