import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

public class RoomsPanel extends JPanel {

    private final Session session;
    private final AppServices app;
    private final DefaultTableModel roomsModel;
    private final DefaultTableModel bookingsModel;
    private final JTable roomsTable;
    private final JTable bookingsTable;

    public RoomsPanel(Session session, AppServices app) {
        this.session = session;
        this.app = app;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        String[] roomCols = session.isAdmin()
                ? new String[]{"ID", "Capacity", "Equipment", "Active"}
                : new String[]{"ID", "Capacity", "Equipment"};
        roomsModel = new DefaultTableModel(roomCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        roomsTable = new JTable(roomsModel);
        JPanel top = new JPanel(new BorderLayout(4, 4));
        top.setBorder(BorderFactory.createTitledBorder(session.isAdmin() ? "All rooms" : "Available rooms"));
        top.add(new JScrollPane(roomsTable), BorderLayout.CENTER);

        JPanel roomButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        if (session.isAdmin()) {
            JButton add = new JButton("Add room");
            JButton edit = new JButton("Edit");
            JButton deactivate = new JButton("Deactivate");
            add.addActionListener(e -> showRoomDialog(null));
            edit.addActionListener(e -> {
                Room r = selectedRoom();
                if (r == null) {
                    return;
                }
                showRoomDialog(r);
            });
            deactivate.addActionListener(e -> {
                Room r = selectedRoom();
                if (r == null) {
                    return;
                }
                r.setActive(false);
                refreshRooms();
                JOptionPane.showMessageDialog(this, "Room " + r.getId() + " deactivated.");
            });
            roomButtons.add(add);
            roomButtons.add(edit);
            roomButtons.add(deactivate);
        } else {
            JButton book = new JButton("Book selected room…");
            book.addActionListener(e -> {
                Room r = selectedRoom();
                if (r == null || !r.isActive()) {
                    JOptionPane.showMessageDialog(this, "Select an active room.");
                    return;
                }
                showBookingDialog(r.getId());
            });
            roomButtons.add(book);
        }
        top.add(roomButtons, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        String[] bookCols = new String[]{"ID", "Room", "Start", "End", "Cancelled"};
        bookingsModel = new DefaultTableModel(bookCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookingsTable = new JTable(bookingsModel);
        JPanel bottom = new JPanel(new BorderLayout(4, 4));
        bottom.setBorder(BorderFactory.createTitledBorder(
                session.isAdmin() ? "All bookings" : "My bookings"));
        bottom.add(new JScrollPane(bookingsTable), BorderLayout.CENTER);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        if (!session.isAdmin()) {
            JButton cancel = new JButton("Cancel selected booking");
            cancel.addActionListener(e -> cancelSelectedBooking(false));
            bp.add(cancel);
        } else {
            JButton cancel = new JButton("Cancel booking (admin)");
            cancel.addActionListener(e -> cancelSelectedBooking(true));
            bp.add(cancel);
        }
        bottom.add(bp, BorderLayout.SOUTH);
        add(bottom, BorderLayout.CENTER);

        refreshRooms();
        refreshBookings();
    }

    private Room selectedRoom() {
        int row = roomsTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        String id = (String) roomsModel.getValueAt(row, 0);
        return app.roomService.get(id).orElse(null);
    }

    private void refreshRooms() {
        roomsModel.setRowCount(0);
        List<Room> list = session.isAdmin() ? app.roomService.allRooms() : app.roomService.activeRooms();
        for (Room r : list) {
            if (session.isAdmin()) {
                roomsModel.addRow(new Object[]{
                        r.getId(), r.getCapacity(), r.getEquipment(), r.isActive()});
            } else {
                roomsModel.addRow(new Object[]{r.getId(), r.getCapacity(), r.getEquipment()});
            }
        }
    }

    private void refreshBookings() {
        bookingsModel.setRowCount(0);
        List<Booking> list = session.isAdmin()
                ? app.bookingService.allBookings()
                : app.bookingService.bookingsForUser(session.username());
        for (Booking b : list) {
            bookingsModel.addRow(new Object[]{
                    b.getId(), b.getRoomId(), b.getStart().toString(), b.getEnd().toString(), b.isCancelled()});
        }
    }

    private void showRoomDialog(Room existing) {
        JDialog d = new JDialog(JOptionPane.getFrameForComponent(this), true);
        d.setTitle(existing == null ? "Add room" : "Edit room " + existing.getId());
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.LINE_END;

        JTextField idField = new JTextField(12);
        JTextField capField = new JTextField(6);
        JTextField eqField = new JTextField(20);
        if (existing != null) {
            idField.setText(existing.getId());
            idField.setEditable(false);
            capField.setText(String.valueOf(existing.getCapacity()));
            eqField.setText(existing.getEquipment());
        }

        g.gridx = 0;
        g.gridy = 0;
        p.add(new JLabel("ID:"), g);
        g.gridy = 1;
        p.add(new JLabel("Capacity:"), g);
        g.gridy = 2;
        p.add(new JLabel("Equipment:"), g);

        g.gridx = 1;
        g.anchor = GridBagConstraints.LINE_START;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.gridy = 0;
        p.add(idField, g);
        g.gridy = 1;
        p.add(capField, g);
        g.gridy = 2;
        p.add(eqField, g);

        JPanel bt = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("Save");
        ok.addActionListener(e -> {
            String id = idField.getText().trim();
            if (id.isEmpty()) {
                return;
            }
            int cap;
            try {
                cap = Integer.parseInt(capField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d, "Invalid capacity.");
                return;
            }
            String eq = eqField.getText().trim();
            if (existing == null) {
                if (app.roomService.get(id).isPresent()) {
                    JOptionPane.showMessageDialog(d, "Room ID already exists.");
                    return;
                }
                app.roomService.addRoom(new Room(id, cap, eq, true));
            } else {
                existing.setCapacity(cap);
                existing.setEquipment(eq);
            }
            refreshRooms();
            d.dispose();
        });
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> d.dispose());
        bt.add(ok);
        bt.add(cancel);

        d.setContentPane(new JPanel(new BorderLayout()));
        d.add(p, BorderLayout.CENTER);
        d.add(bt, BorderLayout.SOUTH);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void showBookingDialog(String roomId) {
        JDialog d = new JDialog(JOptionPane.getFrameForComponent(this), true);
        d.setTitle("Book room " + roomId + " — time slots");
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.LINE_START;

        List<TimeSlot> slots = TimeSlots.standardDay();
        TimeSlot[] slotArray = slots.toArray(new TimeSlot[0]);

        Date today = DateTimeUtil.toDate(LocalDateTime.now().withHour(12).withMinute(0).withSecond(0).withNano(0));
        JSpinner dateSp = new JSpinner(new SpinnerDateModel(today, null, null, java.util.Calendar.DAY_OF_MONTH));
        dateSp.setEditor(new JSpinner.DateEditor(dateSp, "yyyy-MM-dd"));

        JComboBox<TimeSlot> startCombo = new JComboBox<>(slotArray);
        JComboBox<TimeSlot> endCombo = new JComboBox<>();

        Runnable refreshEndSlots = () -> {
            int si = startCombo.getSelectedIndex();
            endCombo.removeAllItems();
            if (si < 0) {
                return;
            }
            for (int i = si; i < slots.size(); i++) {
                endCombo.addItem(slots.get(i));
            }
            if (endCombo.getItemCount() > 0) {
                endCombo.setSelectedIndex(0);
            }
        };
        startCombo.addActionListener(e -> refreshEndSlots.run());
        refreshEndSlots.run();

        JCheckBox recurring = new JCheckBox("Weekly recurring (same slots)");
        JSpinner weeksSp = new JSpinner(new SpinnerNumberModel(4, 1, 52, 1));
        weeksSp.setEnabled(false);
        recurring.addActionListener(e -> weeksSp.setEnabled(recurring.isSelected()));

        g.gridx = 0;
        g.gridy = 0;
        p.add(new JLabel("Date:"), g);
        g.gridy = 1;
        p.add(new JLabel("First slot:"), g);
        g.gridy = 2;
        p.add(new JLabel("Last slot:"), g);
        g.gridy = 3;
        p.add(recurring, g);
        g.gridy = 4;
        p.add(new JLabel("Weeks (if recurring):"), g);

        g.gridx = 1;
        g.gridy = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        p.add(dateSp, g);
        g.gridy = 1;
        p.add(startCombo, g);
        g.gridy = 2;
        p.add(endCombo, g);
        g.gridy = 4;
        p.add(weeksSp, g);

        JLabel hint = new JLabel("<html><i>30-minute slots, 08:00–18:00. Last slot ends where the booking ends.</i></html>");
        g.gridx = 0;
        g.gridy = 5;
        g.gridwidth = 2;
        g.weightx = 1;
        p.add(hint, g);

        JPanel bt = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("Book");
        ok.addActionListener(e -> {
            LocalDate day = DateTimeUtil.toLocalDate((Date) dateSp.getValue());
            TimeSlot first = (TimeSlot) startCombo.getSelectedItem();
            TimeSlot last = (TimeSlot) endCombo.getSelectedItem();
            if (first == null || last == null) {
                JOptionPane.showMessageDialog(d, "Select time slots.", "Booking", JOptionPane.WARNING_MESSAGE);
                return;
            }
            LocalDateTime s = first.startOn(day);
            LocalDateTime en = last.endOn(day);
            if (!en.isAfter(s)) {
                JOptionPane.showMessageDialog(d, "End must be after start.", "Booking", JOptionPane.WARNING_MESSAGE);
                return;
            }
            java.util.Optional<String> err;
            if (recurring.isSelected()) {
                int w = ((Number) weeksSp.getValue()).intValue();
                err = app.bookingService.bookRecurring(roomId, session.username(), s, en, w);
            } else {
                err = app.bookingService.book(roomId, session.username(), s, en);
            }
            if (err.isPresent()) {
                JOptionPane.showMessageDialog(d, err.get(), "Booking failed", JOptionPane.WARNING_MESSAGE);
            } else {
                refreshBookings();
                d.dispose();
            }
        });
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> d.dispose());
        bt.add(ok);
        bt.add(cancel);

        d.setContentPane(new JPanel(new BorderLayout()));
        d.add(p, BorderLayout.CENTER);
        d.add(bt, BorderLayout.SOUTH);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void cancelSelectedBooking(boolean admin) {
        int row = bookingsTable.getSelectedRow();
        if (row < 0) {
            return;
        }
        String id = (String) bookingsModel.getValueAt(row, 0);
        app.bookingService.cancelBooking(id, session.username(), admin).ifPresentOrElse(
                msg -> JOptionPane.showMessageDialog(this, msg, "Cancel", JOptionPane.WARNING_MESSAGE),
                this::refreshBookings);
    }
}
