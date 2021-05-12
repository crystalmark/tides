package com.github.guikeller.jtide.api;

import com.github.guikeller.jtide.models.TideStation;
import com.github.guikeller.jtide.models.TimedValue;
import com.github.guikeller.jtide.services.TideCalculator;
import com.github.guikeller.jtide.services.XMLTideStationService;
import com.github.guikeller.jtide.util.StationTreeNode;
import com.github.guikeller.jtide.util.TideUtilities;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Tides API
 * @author https://github.com/crystalmark/tides
 * @author Gui Keller (Mainly tidying up the code, demo usage, and removing external deps)
 */
public class TideApi {

    private final XMLTideStationService tideService = new XMLTideStationService();
    private final TideCalculator tideCalculator = new TideCalculator(tideService);

    public TideApi() {
        super();
    }

    /**
     * A list of all stations available to use
     * @return List<String> stations
     */
    public List<String> getStations() {
        Stream<String> stream = tideService.getTideStations().stream().map(TideStation::getFullName).sorted();
        List<String> xmlLoadedStations = stream.collect(Collectors.toList());
        xmlLoadedStations.removeIf(station -> station.contains("expired") || station.contains("caution"));
        return xmlLoadedStations;
    }

    /**
     *
     * @return TreeMap<String, StationTreeNode>
     */
    public TreeMap<String, StationTreeNode> getStationsTree() {
        Set<TideStation> tideStations = tideService.getTideStations();
        tideStations.removeIf(station -> station.getFullName().contains("expired") || station.getFullName().contains("caution"));
        return TideUtilities.buildStationTree(tideStations);
    }

    /**
     * It returns the all tides movements that might happen for a location on the given date
     * @param location String
     * @param date LocalDate
     * @return List<TimedValue>
     */
    public List<TimedValue> getTideHeightAtTimeAndPlace(String location, LocalDate date) {
        try {
            return tideCalculator.getHighAndLowTides(location, date);
        } catch (Exception e) {
            throw new RuntimeException("Invalid params: "+location+" / "+date, e);
        }
    }

    /**
     * It returns the 4 tides (High/Low/High/Low) movements that might happen for a location on the given date
     * @param location String
     * @param date LocalDate
     * @return List<TimedValue>
     */
    public List<TimedValue> getHourlyTides(String location, LocalDate date) {
        try {
            return tideCalculator.getTides(location, date, 60);
        } catch (Exception e) {
            throw new RuntimeException("Invalid params: "+location+" / "+date, e);
        }
    }

}
