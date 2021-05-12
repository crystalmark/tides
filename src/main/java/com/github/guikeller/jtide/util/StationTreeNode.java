package com.github.guikeller.jtide.util;

import java.util.TreeMap;

public class StationTreeNode implements Comparable {

    public final static int TIDE_STATION = 1;
    public final static int CURRENT_STATION = 2;

    private String label = "";
    private String fullStationName = null;
    private int stationType = 0;
    private TreeMap<String, StationTreeNode> subTree = new TreeMap<String, StationTreeNode>();

    public StationTreeNode(String label) {
        this.label = label;
    }

    public TreeMap<String, StationTreeNode> getSubTree() {
        return subTree;
    }

    public void setFullStationName(String fullStationName) {
        this.fullStationName = fullStationName;
    }

    public String getFullStationName() {
        return fullStationName;
    }

    public void setStationType(int stationType) {
        this.stationType = stationType;
    }

    public int getStationType() {
        return stationType;
    }

    @Override
    public String toString() {
        return this.label + " / " + this.fullStationName + " / " + this.stationType;
    }

    public int compareTo(Object o) {
        return this.label.compareTo(o.toString());
    }

    public boolean equals(Object o) {
        return (o instanceof StationTreeNode && this.compareTo(o) == 0);
    }

}
