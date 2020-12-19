
public class Time {
    private final int day;
    private final int minute;
    private final int hour;

    public Time(int day, int hour, int minute) {
        this.day = day;
        this.hour = hour;
        this.minute = minute;
    }

    public boolean same(Time time) {
        return (time.day == this.day && time.hour == this.hour && time.minute == this.minute);
    }

    @Override
    public String toString() {
        return day + ":" + hour + ":" + minute;
    }
    public int getInMinutes() { return hour * 60 + minute; }
    public int getDay() {
        return day;
    }

}
