package com.github.guikeller.jtide.models;

import java.time.LocalDateTime;

public class TimedValue implements Comparable<TimedValue> {

    private LocalDateTime calendar;
    private double value;
    private TideType type;

    public TimedValue() {
        super();
    }

    public TimedValue(LocalDateTime calendar, double d) {
        this.calendar = calendar;
        this.value = d;
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

    public void setType(TideType type) {
        this.type = type;
    }

    public int compareTo(TimedValue tv) {
        return this.calendar.compareTo(tv.getCalendar());
    }

    public boolean equals(Object o) {
        return (o instanceof TimedValue && this.compareTo((TimedValue) o) == 0);
    }

}
