import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MaintenanceServiceTest {

    private NotificationBus bus;
    private RecordingNotificationListener recorder;
    private MaintenanceService maintenance;

    @BeforeEach
    void setUp() {
        bus = new NotificationBus();
        recorder = new RecordingNotificationListener();
        bus.subscribe(recorder);
        maintenance = new MaintenanceService(bus);
    }

    @Test
    void report_publishesToReporterAndAdmins() {
        MaintenanceRequest r = maintenance.report("R1", "Leak", Urgency.HIGH, "student1");

        assertEquals(RequestStatus.PENDING, r.getStatus());
        TestEdt.flushEdt();
        List<Notification> notes = recorder.notifications();
        assertEquals(2, notes.size());
        assertEquals("student1", notes.get(0).recipientKey());
        assertEquals("New maintenance request", notes.get(1).title());
        assertEquals(NotificationBus.ADMINS_BROADCAST, notes.get(1).recipientKey());
    }

    @Test
    void updateAdmin_assignRequiresAssignee() {
        MaintenanceRequest r = maintenance.report("R2", "Door", Urgency.LOW, "rep");

        Optional<String> err = maintenance.updateAdmin(r.getId(), RequestStatus.ASSIGNED, "  ");

        assertEquals(Optional.of("Set an assignee when status is ASSIGNED."), err);
    }

    @Test
    void updateAdmin_invalidStatus_returnsError() {
        MaintenanceRequest r = maintenance.report("R3", "Paint", Urgency.MEDIUM, "rep");

        Optional<String> err = maintenance.updateAdmin(r.getId(), RequestStatus.BOOKING_PENDING, null);

        assertEquals(Optional.of("Use only PENDING, ASSIGNED, or COMPLETED for maintenance."), err);
    }

    @Test
    void updateAdmin_notFound_returnsError() {
        Optional<String> err = maintenance.updateAdmin("MR999", RequestStatus.COMPLETED, null);

        assertEquals(Optional.of("Request not found."), err);
    }

    @Test
    void updateAdmin_transitions_notifyReporterAndAdmins() {
        MaintenanceRequest r = maintenance.report("R4", "Lights", Urgency.MEDIUM, "sam");
        TestEdt.flushEdt();
        recorder.clear();

        Optional<String> err = maintenance.updateAdmin(r.getId(), RequestStatus.ASSIGNED, "crew");

        assertTrue(err.isEmpty());
        TestEdt.flushEdt();
        List<Notification> notes = recorder.notifications();
        assertEquals(2, notes.size());
        assertEquals("Maintenance update", notes.get(0).title());
        assertEquals("sam", notes.get(0).recipientKey());
        assertEquals(NotificationBus.ADMINS_BROADCAST, notes.get(1).recipientKey());
        assertEquals(RequestStatus.ASSIGNED, maintenance.all().get(0).getStatus());
        assertEquals("crew", maintenance.all().get(0).getAssignedTo());
    }

    @Test
    void forUser_listsOnlyTheirRequests() {
        maintenance.report("A", "x", Urgency.LOW, "u1");
        maintenance.report("B", "y", Urgency.LOW, "u2");

        assertEquals(1, maintenance.forUser("u1").size());
        assertEquals("A", maintenance.forUser("u1").get(0).getRoomId());
    }
}
