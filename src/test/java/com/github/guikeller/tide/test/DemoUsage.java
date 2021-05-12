package com.github.guikeller.tide.test;

import com.github.guikeller.jtide.api.TideApi;
import com.github.guikeller.jtide.models.TimedValue;
import com.github.guikeller.jtide.util.StationTreeNode;

import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;

/**
 * Run this class, see the output generated, compare with what it is out there (google)
 * times and tides might be slightly off, if so adjust on the 'consumer' / 'client'
 * LICENSE: MIT
 * @author Gui Keller
 */
public class DemoUsage {

    private boolean previousIsLeaf = false;
    private final TideApi api = new TideApi();

    public DemoUsage() {
        super();
    }

    public void spaceOutput(String demoName) {
        System.out.println(" ");
        System.out.println("- - -");
        System.out.println(demoName);
    }

    public void printAllStations() {
        this.spaceOutput("#printAllStations");
        List<String> stations = api.getStations();
        stations.forEach(station -> System.out.println(station));
    }

    public void getStationsTree() {
        this.spaceOutput("#getStationsTree");
        TreeMap<String, StationTreeNode> stationsTree = api.getStationsTree();
        printTreeMapRecursively(stationsTree);
    }

    protected void printTreeMapRecursively(TreeMap<String, StationTreeNode> stationsTree) {
        stationsTree.keySet().forEach(key -> {
            StationTreeNode treeNode = stationsTree.get(key);
            if (treeNode.getSubTree().isEmpty()) {
                System.out.println("leaf: " + treeNode);
                this.previousIsLeaf = true;
            } else {
                if (previousIsLeaf) {
                    System.out.println(" * * * ");
                    this.previousIsLeaf = false;
                }
                System.out.println("node: " + key);
                this.printTreeMapRecursively(treeNode.getSubTree());
            }
        });
    }

    // Google: "tide Aba, Nagasaki, Japan" (and compare the results) =]
    public void getHourlyTides() {
        this.spaceOutput("#getHourlyTides");
        String firstStationName = "Aba, Nagasaki, Japan";
        List<TimedValue> hourlyTides = api.getHourlyTides(firstStationName, LocalDate.now());
        hourlyTides.forEach(timedValue -> {
            System.out.println("value: "+ timedValue.getValue() + " / type: " + timedValue.getType() + " / time: "+timedValue.getCalendar());
        });
    }

    // Google: "tide Aba, Nagasaki, Japan" (and compare the results) =]
    public void getTideHeightAtTimeAndPlace() {
        this.spaceOutput("#getTideHeightAtTimeAndPlace");

        String firstStationName = "Aba, Nagasaki, Japan";
        List<TimedValue> hourlyTides = api.getTideHeightAtTimeAndPlace(firstStationName, LocalDate.now());
        hourlyTides.forEach(timedValue -> {
            System.out.println("value: "+ timedValue.getValue() + " / type: " + timedValue.getType() + " / time: "+timedValue.getCalendar());
        });
    }

    public void printEndMsg() {
        System.out.println();
        System.out.println("* * * # * * *");
        System.out.println("It works, so if you want something better, clone this repo and do it yourself.");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Demo usage..");
        DemoUsage tests = new DemoUsage();
        tests.printAllStations();
        tests.getStationsTree();
        tests.getHourlyTides();
        tests.getTideHeightAtTimeAndPlace();
        tests.printEndMsg();
        System.exit(0);
    }

}
