import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
    private final CampusManagementFacade campus;
    private final DefaultTableModel roomsModel;
    private final DefaultTableModel bookingsModel;
    private DefaultTableModel requestsModel;
    private DefaultTableModel pendingAdminModel;
    private final JTable roomsTable;
    private final JTable bookingsTable;
    private JTable requestsTable;
    private JTable pendingAdminTable;

    public RoomsPanel(Session session, CampusManagementFacade campus) {
        this.session = session;
        this.campus = campus;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        String[] roomCols = session.canManageRooms()
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
        top.setBorder(BorderFactory.createTitledBorder(session.canManageRooms()
                ? "All rooms"
                : "Available rooms"));
        top.add(new JScrollPane(roomsTable), BorderLayout.CENTER);

        JPanel roomButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        if (session.canManageRooms()) {
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
        } else if (session.canBookRoomDirectly()) {
            JButton book = new JButton("Book selected room…");
            book.addActionListener(e -> {
                Room r = selectedRoom();
                if (r == null || !r.isActive()) {
                    JOptionPane.showMessageDialog(this, "Select an active room.");
                    return;
                }
                showBookingDialog(r.getId(), true);
            });
            roomButtons.add(book);
        } else if (session.canRequestBooking()) {
            JButton req = new JButton("Request booking…");
            req.addActionListener(e -> {
                Room r = selectedRoom();
                if (r == null || !r.isActive()) {
                    JOptionPane.showMessageDialog(this, "Select an active room.");
                    return;
                }
                showBookingDialog(r.getId(), false);
            });
            roomButtons.add(req);
        }
        top.add(roomButtons, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(4, 4));

        if (session.canManageRooms()) {
            pendingAdminModel = new DefaultTableModel(
                    new String[]{"ID", "User", "Room", "Start", "End", "Status"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            pendingAdminTable = new JTable(pendingAdminModel);
            JPanel pendingWrap = new JPanel(new BorderLayout(4, 4));
            pendingWrap.setBorder(BorderFactory.createTitledBorder("Pending booking requests"));
            pendingWrap.add(new JScrollPane(pendingAdminTable), BorderLayout.CENTER);
            JPanel pbtn = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton approve = new JButton("Approve selected");
            JButton reject = new JButton("Reject selected");
            approve.addActionListener(e -> actOnPending(true));
            reject.addActionListener(e -> actOnPending(false));
            pbtn.add(approve);
            pbtn.add(reject);
            pendingWrap.add(pbtn, BorderLayout.SOUTH);
            center.add(pendingWrap, BorderLayout.NORTH);
        }

        if (session.canRequestBooking()) {
            requestsModel = new DefaultTableModel(
                    new String[]{"ID", "Room", "Start", "End", "Status"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            requestsTable = new JTable(requestsModel);
            JPanel reqWrap = new JPanel(new BorderLayout(4, 4));
            reqWrap.setBorder(BorderFactory.createTitledBorder("My booking requests"));
            reqWrap.add(new JScrollPane(requestsTable), BorderLayout.CENTER);
            center.add(reqWrap, BorderLayout.NORTH);
        }

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
                session.canManageRooms() ? "All bookings" : "My confirmed bookings"));
        bottom.add(new JScrollPane(bookingsTable), BorderLayout.CENTER);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        if (!session.canManageRooms()) {
            JButton cancel = new JButton("Cancel selected booking");
            cancel.addActionListener(e -> cancelSelectedBooking(false));
            bp.add(cancel);
        } else {
            JButton cancel = new JButton("Cancel booking (admin)");
            cancel.addActionListener(e -> cancelSelectedBooking(true));
            bp.add(cancel);
        }
        bottom.add(bp, BorderLayout.SOUTH);

        if (session.canRequestBooking() || session.canManageRooms()) {
            center.add(bottom, BorderLayout.CENTER);
            add(center, BorderLayout.CENTER);
        } else {
            add(bottom, BorderLayout.CENTER);
        }

        refreshRooms();
        refreshBookings();
        refreshBookingRequests();
    }

    private void actOnPending(boolean approve) {
        int row = pendingAdminTable.getSelectedRow();
        if (row < 0) {
            return;
        }
        String id = (String) pendingAdminModel.getValueAt(row, 0);
        Optional<String> err = approve
                ? campus.approvePendingRoomBooking(id)
                : campus.rejectPendingRoomBooking(id);
        err.ifPresentOrElse(
                msg -> JOptionPane.showMessageDialog(this, msg, "Request", JOptionPane.WARNING_MESSAGE),
                () -> {
                    refreshBookingRequests();
                    refreshBookings();
                });
    }

    private Room selectedRoom() {
        int row = roomsTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        String id = (String) roomsModel.getValueAt(row, 0);
        return campus.roomService.get(id).orElse(null);
    }

    private void refreshRooms() {
        roomsModel.setRowCount(0);
        List<Room> list = session.canManageRooms()
                ? campus.roomService.allRooms()
                : campus.roomService.activeRooms();
        for (Room r : list) {
            if (session.canManageRooms()) {
                roomsModel.addRow(new Object[]{
                        r.getId(), r.getCapacity(), r.getEquipment(), r.isActive()});
            } else {
                roomsModel.addRow(new Object[]{r.getId(), r.getCapacity(), r.getEquipment()});
            }
        }
    }

    private void refreshBookings() {
        bookingsModel.setRowCount(0);
        List<Booking> list = session.canManageRooms()
                ? campus.bookingService.allBookings()
                : campus.bookingService.bookingsForUser(session.username());
        for (Booking b : list) {
            bookingsModel.addRow(new Object[]{
                    b.getId(), b.getRoomId(), b.getStart().toString(), b.getEnd().toString(), b.isCancelled()});
        }
    }

    private void refreshBookingRequests() {
        if (session.canManageRooms() && pendingAdminModel != null) {
            pendingAdminModel.setRowCount(0);
            for (RoomService.RoomBookingRequest r : campus.roomService.pendingBookingRequests()) {
                pendingAdminModel.addRow(new Object[]{
                        r.getId(), r.getUsername(), r.getRoomId(),
                        r.getStart().toString(), r.getEnd().toString(), r.getStatus()});
            }
        }
        if (session.canRequestBooking() && requestsModel != null) {
            requestsModel.setRowCount(0);
            for (RoomService.RoomBookingRequest r : campus.roomService.bookingRequestsForUser(session.username())) {
                requestsModel.addRow(new Object[]{
                        r.getId(), r.getRoomId(),
                        r.getStart().toString(), r.getEnd().toString(), r.getStatus()});
            }
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
                if (campus.roomService.get(id).isPresent()) {
                    JOptionPane.showMessageDialog(d, "Room ID already exists.");
                    return;
                }
                campus.roomService.addRoom(new Room(id, cap, eq, true));
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

    /**
     * @param directBooking true for staff (immediate booking); false for student (request for approval).
     */
    private void showBookingDialog(String roomId, boolean directBooking) {
        JDialog d = new JDialog(JOptionPane.getFrameForComponent(this), true);
        d.setTitle(directBooking ? "Book room " + roomId : "Request booking — " + roomId);
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
        if (!directBooking) {
            recurring.setEnabled(false);
            recurring.setSelected(false);
            weeksSp.setEnabled(false);
        }

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
        JButton ok = new JButton(directBooking ? "Book" : "Submit request");
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
            if (directBooking) {
                if (recurring.isSelected()) {
                    int w = ((Number) weeksSp.getValue()).intValue();
                    err = campus.bookingService.bookRecurring(roomId, session.username(), s, en, w);
                } else {
                    err = campus.bookingService.book(roomId, session.username(), s, en);
                }
            } else {
                err = campus.roomService.submitBookingRequest(roomId, session.username(), s, en);
            }
            if (err.isPresent()) {
                JOptionPane.showMessageDialog(d, err.get(), "Booking failed", JOptionPane.WARNING_MESSAGE);
            } else {
                refreshBookings();
                refreshBookingRequests();
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
        campus.bookingService.cancelBooking(id, session.username(), admin).ifPresentOrElse(
                msg -> JOptionPane.showMessageDialog(this, msg, "Cancel", JOptionPane.WARNING_MESSAGE),
                this::refreshBookings);
    }
}
