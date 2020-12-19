

public class ExamRoom {

    private final String name;
    private final int capacity;
    private final boolean pc;
    private final int onDuty;
    private final boolean etf;

    public ExamRoom(String name, int capacity, boolean pc, int onDuty, boolean etf) {
        this.name = name;
        this.capacity = capacity;
        this.pc = pc;
        this.onDuty = onDuty;
        this.etf = etf;
    }

    public String getName() { return name; }
    public boolean getPC() {
        return pc;
    }
    public int getCapacity() {
        return capacity;
    }
    public int getOnDuty() {
        return onDuty;
    }
    public boolean getETF() {
        return etf;
    }

}
