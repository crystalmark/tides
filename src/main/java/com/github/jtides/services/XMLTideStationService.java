package com.github.jtides.services;

import com.github.jtides.models.*;
import com.github.jtides.util.StationTreeNode;
import com.github.jtides.util.TideUtilities;

import java.util.*;

public class XMLTideStationService {

    private final XMLDataLoader stationsService = new XMLDataLoader();
    private Constituents constituents = null;
    private Set<TideStation> stations = null;
    private static final String RESERVED_KEY = "x";

    public XMLTideStationService() {
        try {
            constituents = stationsService.loadConstituentsFromXml();
            stations = stationsService.loadTideStationsFromXml();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public Set<TideStation> getStations() {
        return stations;
    }

    public Constituents getConstituents() {
        return constituents;
    }

    public List<Coefficient> getSiteConstSpeed() throws Exception {
        return getSiteConstSpeed(constituents);
    }

    public double getAmplitudeFix(int year, String name) {
        return getAmplitudeFix(constituents, year, name);
    }

    public double getEpochFix(int year, String name) {
        return getEpochFix(constituents, year, name);
    }

    public TideStation getTideStation(String stationName, int year) {
        return findTideStation(stationName, year, stations);
    }

    public Set<TideStation> getTideStations() {
        return stations;
    }

    public TreeMap<String, StationTreeNode> getStationTree(Set<TideStation> stations) {
        TreeMap<String, StationTreeNode> st = null;
        st = TideUtilities.buildStationTree(stations);
        return st;
    }

    public List<Coefficient> getSiteConstSpeed(Constituents doc) {
        List<Coefficient> coefficients = new ArrayList<>();
        Map<String, ConstSpeed> constSpeedMap = doc.getConstSpeedMap();
        Set<String> keys = constSpeedMap.keySet();
        for (String key : keys) {
            ConstSpeed constSpeed = constSpeedMap.get(key);
            Coefficient coefficient = new Coefficient(constSpeed.getCoeffName(),(constSpeed.getCoeffValue() * TideUtilities.COEFF_FOR_EPOCH));
            coefficients.add(coefficient);
        }
        return coefficients;
    }

    public double getAmplitudeFix(Constituents doc, int year, String name) {
        ConstSpeed cs = doc.getConstSpeedMap().get(name);
        if (cs != null) {
            return cs.getFactors().get(year);
        }
        return 0.0D;
    }

    public double getEpochFix(Constituents doc, int year, String name) {
        ConstSpeed cs = doc.getConstSpeedMap().get(name);
        if (cs != null) {
            double f = cs.getEquilibrium().get(year);
            return f * TideUtilities.COEFF_FOR_EPOCH;
        }
        return 0.0D;
    }

    public TideStation findTideStation(String stationName, int year, Set<TideStation> stations) {
        Optional<TideStation> station = stations.stream().filter(s -> s.getFullName().equals(stationName)).findAny();
        if ( !station.isPresent()){
           station = stations.stream().filter(s -> s.getNameParts().contains(stationName)).findAny();
        }
        return station.map(tideStation -> correctHarmonics(tideStation, year)).orElse(null);
    }

    public TideStation correctHarmonics(TideStation tideStation, int year) {
        TideStation station = tideStation;
        if (station.yearHarmonicsFixed() != -1 && station.yearHarmonicsFixed() != year) {
            TideStation newTideStation = getTideStation(station.getFullName());
            stations.add(newTideStation);
            station = newTideStation;
        }

        // Correction to the Harmonics
        if (station.yearHarmonicsFixed() == -1) {
            for (Harmonic harm : station.getHarmonics()) {
                String name = harm.getName();
                if (!RESERVED_KEY.equals(name)) {
                    double amplitudeFix = getAmplitudeFix(constituents, year, name);
                    double epochFix = getEpochFix(constituents, year, name);
                    harm.setAmplitude(harm.getAmplitude() * amplitudeFix);
                    harm.setEpoch(harm.getEpoch() - epochFix);
                }
            }
            station.setHarmonicsFixedForYear(year);
        }
        return station;
    }

    private TideStation getTideStation(String stationName) {
        return stationsService.loadTideStation(stationName);
    }

}
