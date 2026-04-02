import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

/**
 * Observer: subscribes to {@link NotificationBus} and shows items addressed to this session.
 */
public class NotificationsPanel extends JPanel implements NotificationListener {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Session session;
    private final AppServices app;
    private final DefaultListModel<String> model = new DefaultListModel<>();

    public NotificationsPanel(Session session, AppServices app) {
        this.session = session;
        this.app = app;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JList<String> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(8, 8));
        south.setBorder(BorderFactory.createTitledBorder("Messaging"));

        JPanel msgRow = new JPanel(new BorderLayout(4, 4));
        if (session.isAdmin()) {
            msgRow.add(new JLabel("To username:"), BorderLayout.WEST);
            JTextField toField = new JTextField(12);
            msgRow.add(toField, BorderLayout.CENTER);
            JTextArea body = new JTextArea(3, 40);
            south.add(msgRow, BorderLayout.NORTH);
            south.add(new JScrollPane(body), BorderLayout.CENTER);
            JButton send = new JButton("Send to user");
            send.addActionListener(e -> {
                String u = toField.getText().trim();
                String t = body.getText().trim();
                if (u.isEmpty() || t.isEmpty()) {
                    return;
                }
                app.notificationBus.publish(new Notification(
                        u,
                        "Message from " + session.username(),
                        t,
                        java.time.LocalDateTime.now()));
                body.setText("");
            });
            JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bp.add(send);
            south.add(bp, BorderLayout.SOUTH);
        } else {
            JTextArea body = new JTextArea(3, 40);
            south.add(new JLabel("Send a note to administrators:"), BorderLayout.NORTH);
            south.add(new JScrollPane(body), BorderLayout.CENTER);
            JButton send = new JButton("Send to admins");
            send.addActionListener(e -> {
                String t = body.getText().trim();
                if (t.isEmpty()) {
                    return;
                }
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                app.notificationBus.publish(new Notification(
                        NotificationBus.ADMINS_BROADCAST,
                        "User note from " + session.username(),
                        t,
                        now));
                app.notificationBus.publish(new Notification(
                        session.username(),
                        "Sent to administrators",
                        t,
                        now));
                body.setText("");
            });
            JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bp.add(send);
            south.add(bp, BorderLayout.SOUTH);
        }

        add(south, BorderLayout.SOUTH);
    }

    private boolean isForMe(Notification n) {
        if (session.username().equals(n.recipientKey())) {
            return true;
        }
        return NotificationBus.ADMINS_BROADCAST.equals(n.recipientKey()) && session.isAdmin();
    }

    @Override
    public void notificationReceived(Notification n) {
        if (!isForMe(n)) {
            return;
        }
        Runnable add = () -> model.addElement(
                "[" + FMT.format(n.createdAt()) + "] " + n.title() + " — " + n.body());
        if (SwingUtilities.isEventDispatchThread()) {
            add.run();
        } else {
            SwingUtilities.invokeLater(add);
        }
    }
}
