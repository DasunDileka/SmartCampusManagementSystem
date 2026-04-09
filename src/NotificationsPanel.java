import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JLabel;
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
    private final CampusManagementFacade campus;
    private final DefaultListModel<String> model = new DefaultListModel<>();

    public NotificationsPanel(Session session, CampusManagementFacade campus) {
        this.session = session;
        this.campus = campus;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JList<String> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(list), BorderLayout.CENTER);

        if (session.isStudent()) {
            JLabel hint = new JLabel("<html><i>Announcements from administrators and your booking updates appear here.</i></html>");
            hint.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
            add(hint, BorderLayout.SOUTH);
            return;
        }

        JPanel south = new JPanel(new BorderLayout(8, 8));
        south.setBorder(BorderFactory.createTitledBorder("Messaging"));

        if (session.isAdmin()) {
            JPanel direct = new JPanel(new BorderLayout(4, 4));
            direct.setBorder(BorderFactory.createTitledBorder("Message one user"));
            JPanel msgRow = new JPanel(new BorderLayout(4, 4));
            msgRow.add(new JLabel("To username:"), BorderLayout.WEST);
            JTextField toField = new JTextField(12);
            msgRow.add(toField, BorderLayout.CENTER);
            JTextArea body = new JTextArea(3, 40);
            direct.add(msgRow, BorderLayout.NORTH);
            direct.add(new JScrollPane(body), BorderLayout.CENTER);
            JButton send = new JButton("Send to user");
            send.addActionListener(e -> {
                String u = toField.getText().trim();
                String t = body.getText().trim();
                if (u.isEmpty() || t.isEmpty()) {
                    return;
                }
                campus.notificationBus.publish(Notification.builder()
                        .recipientKey(u)
                        .title("Message from " + session.username())
                        .body(t)
                        .createdAt(LocalDateTime.now())
                        .build());
                body.setText("");
            });
            JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bp.add(send);
            direct.add(bp, BorderLayout.SOUTH);

            JPanel announce = new JPanel(new BorderLayout(4, 4));
            announce.setBorder(BorderFactory.createTitledBorder("Announcement (all students)"));
            JTextArea annBody = new JTextArea(3, 40);
            announce.add(new JScrollPane(annBody), BorderLayout.CENTER);
            JButton broadcast = new JButton("Send to all students");
            broadcast.addActionListener(e -> {
                String t = annBody.getText().trim();
                if (t.isEmpty()) {
                    return;
                }
                LocalDateTime now = LocalDateTime.now();
                campus.notificationBus.publish(Notification.builder()
                        .recipientKey(NotificationBus.STUDENTS_BROADCAST)
                        .title("Campus announcement")
                        .body(t)
                        .createdAt(now)
                        .build());
                annBody.setText("");
            });
            JPanel bp2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bp2.add(broadcast);
            announce.add(bp2, BorderLayout.SOUTH);

            south.add(direct, BorderLayout.NORTH);
            south.add(announce, BorderLayout.CENTER);
        } else if (session.isStaff()) {
            JTextArea body = new JTextArea(3, 40);
            south.add(new JLabel("Send a note to administrators:"), BorderLayout.NORTH);
            south.add(new JScrollPane(body), BorderLayout.CENTER);
            JButton send = new JButton("Send to admins");
            send.addActionListener(e -> {
                String t = body.getText().trim();
                if (t.isEmpty()) {
                    return;
                }
                LocalDateTime now = LocalDateTime.now();
                campus.notificationBus.publish(Notification.builder()
                        .recipientKey(NotificationBus.ADMINS_BROADCAST)
                        .title("Staff note from " + session.username())
                        .body(t)
                        .createdAt(now)
                        .build());
                campus.notificationBus.publish(Notification.builder()
                        .recipientKey(session.username())
                        .title("Sent to administrators")
                        .body(t)
                        .createdAt(now)
                        .build());
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
        if (NotificationBus.ADMINS_BROADCAST.equals(n.recipientKey()) && session.isAdmin()) {
            return true;
        }
        return NotificationBus.STUDENTS_BROADCAST.equals(n.recipientKey()) && session.isStudent();
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
