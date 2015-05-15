package tideengine;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class TimedValue implements Comparable<TimedValue> {

    private LocalDateTime cal;
    private double value;
    private TideType type;

    public TimedValue(LocalDateTime cal, double d) {
        this.cal = cal;
        this.value = d;
    }

    public int compareTo(TimedValue tv) {
        return this.cal.compareTo(tv.getCalendar());
    }

    public LocalDateTime getCalendar() {
        return cal;
    }

    public double getValue() {
        return value;
    }

    public TideType getType() {
        return type;
    }

    public boolean equals(Object o) {
        return (o instanceof TimedValue && this.compareTo((TimedValue) o) == 0);
    }

    public void setType(TideType type) {
        this.type = type;
    }
}
