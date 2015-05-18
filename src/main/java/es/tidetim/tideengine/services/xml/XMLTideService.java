package es.tidetim.tideengine.services.xml;

import es.tidetim.tideengine.services.TideStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import es.tidetim.tideengine.models.Coefficient;
import es.tidetim.tideengine.models.Constituents;
import es.tidetim.tideengine.models.Harmonic;
import es.tidetim.tideengine.services.StationsService;
import es.tidetim.tideengine.models.TideStation;
import es.tidetim.tideengine.services.TideUtilities;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

@Component
public class XMLTideService implements TideStationService {
    private static Constituents constituents = null;
    private static Set<TideStation> stations = null;

    @Autowired
    StationsService stationsService;

    public Set<TideStation> getStations() {
        return stations;
    }

    public static Constituents getConstituents() {
        return constituents;
    }

    @PostConstruct
    private void connect() throws Exception {
        constituents = stationsService.buildConstituents();
        stations = stationsService.loadTideStations();
    }

    public List<Coefficient> getSiteConstSpeed() throws Exception {
        return getSiteConstSpeed(constituents);
    }

    public double getAmplitudeFix(int year, String name)
            throws Exception {
        double d = 0;
        d = getAmplitudeFix(constituents, year, name);
        return d;
    }

    public double getEpochFix(int year, String name) throws Exception {
        double d = 0;
        d = getEpochFix(constituents, year, name);
        return d;
    }

    public TideStation getTideStation(String stationName, int year)
            throws Exception {
        TideStation ts = null;
        ts = findTideStation(stationName, year, constituents,
                stations);
        return ts;
    }

    public Set<TideStation> getTideStations() {
        return stations;
    }

    public TreeMap<String, TideUtilities.StationTreeNode> getStationTree(Set<TideStation> stations) {
        TreeMap<String, TideUtilities.StationTreeNode> st = null;

        st = TideUtilities.buildStationTree(stations);
        return st;
    }

    public List<Coefficient> getSiteConstSpeed(Constituents doc)
            throws Exception {
        List<Coefficient> csal = new ArrayList<>();
        Map<String, Constituents.ConstSpeed> csm = doc.getConstSpeedMap();
        Set<String> keys = csm.keySet();
        for (String k : keys) {
            Constituents.ConstSpeed cs = csm.get(k);
            Coefficient coef = new Coefficient(cs.getCoeffName(),
                    cs.getCoeffValue() * TideUtilities.COEFF_FOR_EPOCH);
            csal.add(coef);
        }
        return csal;
    }

    public double getAmplitudeFix(Constituents doc, int year, String name)
            throws Exception {
        double d = 0;
        try {
            Constituents.ConstSpeed cs = doc.getConstSpeedMap().get(name);
            double f = cs.getFactors().get(year);
            d = f;
        } catch (Exception ex) {
            System.err.println("Error for [" + name + "] in [" + year + "]");
            throw ex;
        }
        return d;
    }

    public double getEpochFix(Constituents doc, int year, String name)
            throws Exception {
        double d = 0;
        try {
            Constituents.ConstSpeed cs = doc.getConstSpeedMap().get(name);
            double f = cs.getEquilibrium().get(year);
            d = f * TideUtilities.COEFF_FOR_EPOCH;
        } catch (Exception ex) {
            System.err.println("Error for [" + name + "] in [" + year + "]");
            throw ex;
        }
        return d;
    }

    public TideStation findTideStation(String stationName, int year,
                                       Constituents constituents, Set<TideStation> stations) throws Exception {

        Optional<TideStation> station = stations.stream().filter(s -> s.getFullName().equals(stationName)).findAny();

        if ( !station.isPresent()){
           station = stations.stream().filter(s -> s.getNameParts().contains(stationName)).findAny();
        }

        if (station.isPresent()) {
            return correctHarmonics(station.get(), year);
        } else {
            return null;
        }

    }

    public TideStation correctHarmonics(TideStation s, int year) throws Exception {
        TideStation station = s;
        if (station.yearHarmonicsFixed() != -1
                && station.yearHarmonicsFixed() != year) {
            TideStation newTs = getTideStation(station.getFullName());
            stations.add(newTs);
            station = newTs;
        }

        // Correction to the Harmonics
        if (station.yearHarmonicsFixed() == -1) {
            for (Harmonic harm : station.getHarmonics()) {
                String name = harm.getName();
                if (!"x".equals(name)) {
                    double amplitudeFix = getAmplitudeFix(constituents, year,
                            name);
                    double epochFix = getEpochFix(constituents, year, name);

                    harm.setAmplitude(harm.getAmplitude() * amplitudeFix);
                    harm.setEpoch(harm.getEpoch() - epochFix);
                }
            }
            station.setHarmonicsFixedForYear(year);
        }
        return station;
    }

    private TideStation getTideStation(String stationName)
            throws Exception {
        return stationsService.loadTideStation(stationName);
    }


}
