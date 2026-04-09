import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

public class DashboardFrame extends JFrame {

    private final NotificationsPanel notificationsPanel;

    public DashboardFrame(Session session, AppServices app) {
        super("Campus dashboard");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(960, 640);

        JLabel header = new JLabel("Signed in as " + session.username()
                + " (" + (session.isAdmin() ? "Administrator" : "User") + ")");
        header.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 12));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Room management", new RoomsPanel(session, app));
        tabs.addTab("Maintenance", new MaintenancePanel(session, app));
        notificationsPanel = new NotificationsPanel(session, app);
        tabs.addTab("Notifications & messaging", notificationsPanel);
        app.notificationBus.subscribe(notificationsPanel);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                app.notificationBus.unsubscribe(notificationsPanel);
            }
        });

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        setLocationRelativeTo(null);
    }
}
