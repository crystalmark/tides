package com.github.guikeller.jtide.models;

public enum TideType {

    HW("High Water"),
    LW("Low Water"),
    FALLING("Falling"),
    RISING("Rising");

    private String description;

    TideType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
