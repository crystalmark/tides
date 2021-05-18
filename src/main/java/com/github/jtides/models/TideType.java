package com.github.jtides.models;

public enum TideType {

    HW("High Water"),
    LW("Low Water"),
    FALLING("Falling"),
    RISING("Rising");

    private final String description;

    TideType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
