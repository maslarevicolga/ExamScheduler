import java.util.List;

public class RoomsAndTime {

    private final Time time;
    private final List<ExamRoom> rooms;
    private String strRooms = "";
    private double quality;
    private int capacity;

    public RoomsAndTime(Time time, List<ExamRoom> rooms) {
        StringBuilder sb = new StringBuilder();
        this.time = time;
        this.rooms = rooms;
        this.quality = this.capacity = 0;
        for(ExamRoom room : rooms) {
            sb.append(room.getName()).append(" ");
            quality += (room.getOnDuty() + 1.2 * (room.getETF() ? 0 : 1));
            capacity += room.getCapacity();
        }
        strRooms = sb.toString();
    }

    public Time getTime() { return time; }
    public List<ExamRoom> getRooms() { return rooms; }
    public double getQuality() {
        return quality;
    }
    public int getCapacity() {
        return capacity;
    }


    // Equals if day, hour and minute are same and set contains any examHall that is contained in the second set
    public boolean areSubSet(RoomsAndTime check) {
        if(!check.time.same(this.time)) return false;
        for(ExamRoom examRoom : this.rooms)
            if(check.rooms.contains(examRoom)) return true;
        return false;
    }

    @Override
    public String toString() {
        return "[ " + strRooms + "|" + getQuality() + "| |" + time + "| ]";
    }

}
