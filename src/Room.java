public class Room {
    private final String id;
    private int capacity;
    private String equipment;
    private boolean active;

    public Room(String id, int capacity, String equipment, boolean active) {
        this.id = id;
        this.capacity = capacity;
        this.equipment = equipment;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
