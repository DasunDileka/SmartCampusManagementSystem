public final class AppServices {
    private static AppServices instance;

    public final NotificationBus notificationBus = new NotificationBus();
    public final RoomService roomService = new RoomService();
    public final BookingService bookingService;
    public final MaintenanceService maintenanceService;

    private AppServices() {
        bookingService = new BookingService(roomService, notificationBus);
        maintenanceService = new MaintenanceService(notificationBus);
        seedRooms();
    }

    public static synchronized AppServices get() {
        if (instance == null) {
            instance = new AppServices();
        }
        return instance;
    }

    private void seedRooms() {
        roomService.addRoom(new Room("A101", 30, "Projector, Whiteboard", true));
        roomService.addRoom(new Room("B204", 12, "TV, HDMI", true));
        roomService.addRoom(new Room("C-LAB", 24, "Computers (24)", true));
    }
}
