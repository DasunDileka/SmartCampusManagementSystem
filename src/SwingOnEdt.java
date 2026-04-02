import javax.swing.SwingUtilities;

public final class SwingOnEdt {
    private SwingOnEdt() {
    }

    static void openDashboard(Session session, AppServices app) {
        SwingUtilities.invokeLater(() -> {
            DashboardFrame dash = new DashboardFrame(session, app);
            dash.setVisible(true);
        });
    }
}
