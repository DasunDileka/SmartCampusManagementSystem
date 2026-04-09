import javax.swing.SwingUtilities;

public final class SwingOnEdt {
    private SwingOnEdt() {
    }

    static void openDashboard(Session session, CampusManagementFacade campus) {
        SwingUtilities.invokeLater(() -> {
            DashboardFrame dash = new DashboardFrame(session, campus);
            dash.setVisible(true);
        });
    }
}
