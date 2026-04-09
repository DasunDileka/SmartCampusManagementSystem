import java.util.Optional;

/**
 * Facade (structural): one object for the presentation layer, hiding how room booking, users,
 * maintenance, and notifications are split across services. Subsystems remain {@link AppServices}.
 */
public final class CampusManagementFacade {

    public final NotificationBus notificationBus;
    public final UserDirectory userDirectory;
    public final RoomService roomService;
    public final BookingService bookingService;
    public final MaintenanceService maintenanceService;

    public CampusManagementFacade(AppServices app) {
        this.notificationBus = app.notificationBus;
        this.userDirectory = app.userDirectory;
        this.roomService = app.roomService;
        this.bookingService = app.bookingService;
        this.maintenanceService = app.maintenanceService;
    }

    /** Hides the room-service booking-request subsystem behind one operation for admins. */
    public Optional<String> approvePendingRoomBooking(String requestId) {
        return roomService.approveBookingRequest(requestId);
    }

    public Optional<String> rejectPendingRoomBooking(String requestId) {
        return roomService.rejectBookingRequest(requestId);
    }
}
