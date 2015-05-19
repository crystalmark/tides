package es.tidetim.tideengine.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import es.tidetim.tideengine.util.JsonDateDeserializer;
import es.tidetim.tideengine.util.JsonDateSerializer;

import java.time.LocalDateTime;

public class TimedValue implements Comparable<TimedValue> {

    @JsonDeserialize(using = JsonDateDeserializer.class)
    @JsonSerialize(using = JsonDateSerializer.class)
    private LocalDateTime calendar;

    private double value;

    private TideType type;

    public TimedValue(LocalDateTime calendar, double d) {
        this.calendar = calendar;
        this.value = d;
    }

    public TimedValue() {
    }

    public int compareTo(TimedValue tv) {
        return this.calendar.compareTo(tv.getCalendar());
    }

    public LocalDateTime getCalendar() {
        return calendar;
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
