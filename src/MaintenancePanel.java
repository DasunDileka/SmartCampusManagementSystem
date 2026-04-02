import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class MaintenancePanel extends JPanel {

    private final Session session;
    private final AppServices app;
    private final DefaultTableModel model;
    private final JTable table;

    public MaintenancePanel(Session session, AppServices app) {
        this.session = session;
        this.app = app;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        String[] cols = session.isAdmin()
                ? new String[]{"ID", "Room", "Description", "Urgency", "Status", "Reported by", "Assigned"}
                : new String[]{"ID", "Room", "Description", "Urgency", "Status", "Assigned"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(8, 8));

        if (!session.isAdmin()) {
            south.setBorder(BorderFactory.createTitledBorder("Report an issue"));
            JPanel form = new JPanel(new GridBagLayout());
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(4, 4, 4, 4);
            g.anchor = GridBagConstraints.LINE_END;

            JComboBox<String> roomCombo = new JComboBox<>();
            refreshRoomCombo(roomCombo);

            JComboBox<Urgency> urgCombo = new JComboBox<>(Urgency.values());
            JTextArea desc = new JTextArea(4, 36);

            g.gridx = 0;
            g.gridy = 0;
            form.add(new JLabel("Room:"), g);
            g.gridy = 1;
            form.add(new JLabel("Urgency:"), g);
            g.gridy = 2;
            form.add(new JLabel("Description:"), g);

            g.gridx = 1;
            g.gridy = 0;
            g.anchor = GridBagConstraints.LINE_START;
            g.fill = GridBagConstraints.HORIZONTAL;
            g.weightx = 1;
            form.add(roomCombo, g);
            g.gridy = 1;
            form.add(urgCombo, g);
            g.gridy = 2;
            g.fill = GridBagConstraints.BOTH;
            form.add(new JScrollPane(desc), g);

            JButton submit = new JButton("Submit request");
            submit.addActionListener(e -> {
                String room = (String) roomCombo.getSelectedItem();
                if (room == null) {
                    return;
                }
                String text = desc.getText().trim();
                if (text.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Enter a description.");
                    return;
                }
                app.maintenanceService.report(room, text, (Urgency) urgCombo.getSelectedItem(),
                        session.username());
                desc.setText("");
                refreshTable();
            });
            JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bp.add(submit);
            south.add(form, BorderLayout.CENTER);
            south.add(bp, BorderLayout.SOUTH);
        } else {
            south.setBorder(BorderFactory.createTitledBorder("Admin: assign and update status"));
            JPanel admin = new JPanel(new GridBagLayout());
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(4, 4, 4, 4);
            g.anchor = GridBagConstraints.LINE_END;
            JTextField assignField = new JTextField(16);
            JComboBox<RequestStatus> statusCombo = new JComboBox<>(RequestStatus.values());
            g.gridx = 0;
            g.gridy = 0;
            admin.add(new JLabel("Assign to:"), g);
            g.gridy = 1;
            admin.add(new JLabel("New status:"), g);
            g.gridx = 1;
            g.gridy = 0;
            g.anchor = GridBagConstraints.LINE_START;
            g.fill = GridBagConstraints.HORIZONTAL;
            admin.add(assignField, g);
            g.gridy = 1;
            admin.add(statusCombo, g);

            JButton apply = new JButton("Apply to selected request");
            apply.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    return;
                }
                String id = (String) model.getValueAt(row, 0);
                String assign = assignField.getText();
                RequestStatus st = (RequestStatus) statusCombo.getSelectedItem();
                app.maintenanceService.updateAdmin(id, st, assign).ifPresentOrElse(
                        msg -> JOptionPane.showMessageDialog(this, msg, "Update", JOptionPane.WARNING_MESSAGE),
                        () -> {
                            refreshTable();
                            assignField.setText("");
                        });
            });
            JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bp.add(apply);
            south.add(admin, BorderLayout.CENTER);
            south.add(bp, BorderLayout.SOUTH);
        }

        add(south, BorderLayout.SOUTH);
        refreshTable();
    }

    private void refreshRoomCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        for (Room r : app.roomService.activeRooms()) {
            combo.addItem(r.getId());
        }
    }

    private void refreshTable() {
        model.setRowCount(0);
        List<MaintenanceRequest> list = session.isAdmin()
                ? app.maintenanceService.all()
                : app.maintenanceService.forUser(session.username());
        for (MaintenanceRequest r : list) {
            if (session.isAdmin()) {
                model.addRow(new Object[]{
                        r.getId(), r.getRoomId(), r.getDescription(), r.getUrgency(), r.getStatus(),
                        r.getReportedBy(), nullToEmpty(r.getAssignedTo())});
            } else {
                model.addRow(new Object[]{
                        r.getId(), r.getRoomId(), r.getDescription(), r.getUrgency(), r.getStatus(),
                        nullToEmpty(r.getAssignedTo())});
            }
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
