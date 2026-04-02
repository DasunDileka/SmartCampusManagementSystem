import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RoomService {
    private final Map<String, Room> rooms = new LinkedHashMap<>();

    public void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public Optional<Room> get(String id) {
        return Optional.ofNullable(rooms.get(id));
    }

    public List<Room> allRooms() {
        return new ArrayList<>(rooms.values());
    }

    public List<Room> activeRooms() {
        List<Room> list = new ArrayList<>();
        for (Room r : rooms.values()) {
            if (r.isActive()) {
                list.add(r);
            }
        }
        return list;
    }
}
