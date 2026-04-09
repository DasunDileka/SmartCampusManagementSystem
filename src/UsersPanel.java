import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class UsersPanel extends JPanel {

    private final Session session;
    private final CampusManagementFacade campus;
    private final DefaultTableModel model;
    private final JTable table;

    public UsersPanel(Session session, CampusManagementFacade campus) {
        this.session = session;
        this.campus = campus;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        model = new DefaultTableModel(new String[]{"Username", "Role"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(8, 8));
        south.setBorder(BorderFactory.createTitledBorder("Add user"));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.LINE_END;

        JTextField newUserField = new JTextField(16);
        JPasswordField newPassField = new JPasswordField(16);
        JComboBox<UserRole> roleCombo = new JComboBox<>(UserRole.values());

        g.gridx = 0;
        g.gridy = 0;
        form.add(new JLabel("Username:"), g);
        g.gridy = 1;
        form.add(new JLabel("Password:"), g);
        g.gridy = 2;
        form.add(new JLabel("Role:"), g);

        g.gridx = 1;
        g.gridy = 0;
        g.anchor = GridBagConstraints.LINE_START;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        form.add(newUserField, g);
        g.gridy = 1;
        form.add(newPassField, g);
        g.gridy = 2;
        form.add(roleCombo, g);

        JButton addBtn = new JButton("Add user");
        addBtn.addActionListener(e -> {
            String u = newUserField.getText().trim();
            String p = new String(newPassField.getPassword());
            UserRole role = (UserRole) roleCombo.getSelectedItem();
            campus.userDirectory.addUser(u, p, role).ifPresentOrElse(
                    msg -> JOptionPane.showMessageDialog(this, msg, "User management", JOptionPane.WARNING_MESSAGE),
                    () -> {
                        newUserField.setText("");
                        newPassField.setText("");
                        refreshTable();
                    });
        });

        JPanel adminRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JPasswordField passUpdate = new JPasswordField(12);
        JComboBox<UserRole> roleUpdate = new JComboBox<>(UserRole.values());
        JButton updatePass = new JButton("Set password (selected)");
        JButton updateRole = new JButton("Set role (selected)");
        JButton removeBtn = new JButton("Remove user (selected)");

        updatePass.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a user.", "User management", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String username = (String) model.getValueAt(row, 0);
            String np = new String(passUpdate.getPassword());
            campus.userDirectory.setPassword(username, np).ifPresentOrElse(
                    msg -> JOptionPane.showMessageDialog(this, msg, "User management", JOptionPane.WARNING_MESSAGE),
                    () -> {
                        passUpdate.setText("");
                        JOptionPane.showMessageDialog(this, "Password updated.");
                    });
        });

        updateRole.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a user.", "User management", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String username = (String) model.getValueAt(row, 0);
            UserRole role = (UserRole) roleUpdate.getSelectedItem();
            campus.userDirectory.setRole(username, role).ifPresentOrElse(
                    msg -> JOptionPane.showMessageDialog(this, msg, "User management", JOptionPane.WARNING_MESSAGE),
                    () -> refreshTable());
        });

        removeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a user.", "User management", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String username = (String) model.getValueAt(row, 0);
            int ok = JOptionPane.showConfirmDialog(this,
                    "Remove user \"" + username + "\"?",
                    "Confirm removal",
                    JOptionPane.OK_CANCEL_OPTION);
            if (ok != JOptionPane.OK_OPTION) {
                return;
            }
            campus.userDirectory.removeUser(session.username(), username).ifPresentOrElse(
                    msg -> JOptionPane.showMessageDialog(this, msg, "User management", JOptionPane.WARNING_MESSAGE),
                    this::refreshTable);
        });

        adminRow.add(new JLabel("New password:"));
        adminRow.add(passUpdate);
        adminRow.add(updatePass);
        adminRow.add(new JLabel("Role:"));
        adminRow.add(roleUpdate);
        adminRow.add(updateRole);
        adminRow.add(removeBtn);

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bp.add(addBtn);

        south.add(form, BorderLayout.NORTH);
        south.add(adminRow, BorderLayout.CENTER);
        south.add(bp, BorderLayout.SOUTH);

        add(south, BorderLayout.SOUTH);
        refreshTable();
    }

    private void refreshTable() {
        model.setRowCount(0);
        for (UserDirectory.UserRow r : campus.userDirectory.allUsers()) {
            model.addRow(new Object[]{r.username, r.role});
        }
    }
}
