import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class DashboardFrame extends JFrame {

    private final CampusManagementFacade campus;
    private final NotificationsPanel notificationsPanel;
    private boolean notificationsDetached;

    public DashboardFrame(Session session, CampusManagementFacade campus) {
        super("Campus dashboard");
        this.campus = campus;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(960, 640);

        JLabel header = new JLabel("Signed in as " + session.username()
                + " (" + Session.roleDisplayName(session.role()) + ")");
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));

        JButton backToLogin = new JButton("Back to login");
        backToLogin.addActionListener(e -> returnToLogin());

        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 12));
        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        east.add(backToLogin);
        headerBar.add(header, BorderLayout.WEST);
        headerBar.add(east, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Rooms", new RoomsPanel(session, campus));

        if (session.seesMaintenanceFeatures()) {
            tabs.addTab("Maintenance", new MaintenancePanel(session, campus));
        }

        if (session.seesUserManagement()) {
            tabs.addTab("User management", new UsersPanel(session, campus));
        }

        notificationsPanel = new NotificationsPanel(session, campus);
        tabs.addTab(session.isStudent() ? "Announcements & messages" : "Notifications & messaging",
                notificationsPanel);
        campus.notificationBus.subscribe(notificationsPanel);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                detachNotifications();
            }
        });

        add(headerBar, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        setLocationRelativeTo(null);
    }

    private void detachNotifications() {
        if (!notificationsDetached) {
            notificationsDetached = true;
            campus.notificationBus.unsubscribe(notificationsPanel);
        }
    }

    private void returnToLogin() {
        detachNotifications();
        dispose();
        SwingUtilities.invokeLater(() -> {
            Authenticator auth = new UserDirectoryAuthenticator(campus.userDirectory);
            LoginFrame login = new LoginFrame(auth, campus);
            login.setVisible(true);
        });
    }
}
