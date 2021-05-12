package com.github.guikeller.jtide.util;

import com.github.guikeller.jtide.models.Coefficient;
import com.github.guikeller.jtide.models.Harmonic;
import com.github.guikeller.jtide.models.TideStation;
import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.*;

/**
 * Lots of little goods, use at your own risk, good luck =]
 * @author https://github.com/crystalmark/tides
 * @author Gui Keller
 */
public class TideUtilities {

    public final static double FEET_2_METERS = 0.30480061d;
    public final static double COEFF_FOR_EPOCH = 0.017453292519943289D;
    public final static DecimalFormat DF2PLUS = new DecimalFormat("#0");
    public final static DecimalFormat DF22PLUS = new DecimalFormat("#0.00");
    public final static DecimalFormat DF31 = new DecimalFormat("##0.0");
    public final static DecimalFormat DF13 = new DecimalFormat("##0.000");
    public final static DecimalFormat DF36 = new DecimalFormat("##0.000000");
    public final static Map<String, String> COEFF_DEFINITION = new HashMap<>();

    public final static int MIN_POS = 0;
    public final static int MAX_POS = 1;

    static {
        DF2PLUS.setPositivePrefix("+");
        DF22PLUS.setPositivePrefix("+");

        COEFF_DEFINITION.put("M2", "Principal lunar semidiurnal constituent");
        COEFF_DEFINITION.put("S2", "Principal solar semidiurnal constituent");
        COEFF_DEFINITION.put("N2", "Larger lunar elliptic semidiurnal constituent");
        COEFF_DEFINITION.put("K1", "Lunar diurnal constituent");
        COEFF_DEFINITION.put("M4", "Shallow water overtides of principal lunar constituent");
        COEFF_DEFINITION.put("O1", "Lunar diurnal constituent");
        COEFF_DEFINITION.put("M6", "Shallow water overtides of principal lunar constituent");
        COEFF_DEFINITION.put("MK3", "Shallow water terdiurnal");
        COEFF_DEFINITION.put("S4", "Shallow water overtides of principal solar constituent");
        COEFF_DEFINITION.put("MN4", "Shallow water quarter diurnal constituent");
        COEFF_DEFINITION.put("NU2", "Larger lunar evectional constituent");
        COEFF_DEFINITION.put("S6", "Shallow water overtides of principal solar constituent");
        COEFF_DEFINITION.put("MU2", "Variational constituent");
        COEFF_DEFINITION.put("2N2", "Lunar elliptical semidiurnal second");
        COEFF_DEFINITION.put("OO1", "Lunar diurnal");
        COEFF_DEFINITION.put("LAM2", "Smaller lunar evectional constituent");
        COEFF_DEFINITION.put("S1", "Solar diurnal constituent");
        COEFF_DEFINITION.put("M1", "Smaller lunar elliptic diurnal constituent");
        COEFF_DEFINITION.put("J1", "Smaller lunar elliptic diurnal constituent");
        COEFF_DEFINITION.put("MM", "Lunar monthly constituent");
        COEFF_DEFINITION.put("SSA", "Solar semiannual constituent");
        COEFF_DEFINITION.put("SA", "Solar annual constituent");
        COEFF_DEFINITION.put("MSF", "Lunisolar synodic fortnightly constituent");
        COEFF_DEFINITION.put("MF", "Lunisolar fortnightly constituent");
        COEFF_DEFINITION.put("RHO", "Larger lunar evectional diurnal constituent");
        COEFF_DEFINITION.put("Q1", "Larger lunar elliptic diurnal constituent");
        COEFF_DEFINITION.put("T2", "Larger solar elliptic constituent");
        COEFF_DEFINITION.put("R2", "Smaller solar elliptic constituent");
        COEFF_DEFINITION.put("2Q1", "Larger elliptic diurnal");
        COEFF_DEFINITION.put("P1", "Solar diurnal constituent");
        COEFF_DEFINITION.put("2SM2", "Shallow water semidiurnal constituent");
        COEFF_DEFINITION.put("M3", "Lunar terdiurnal constituent");
        COEFF_DEFINITION.put("L2", "Smaller lunar elliptic semidiurnal constituent");
        COEFF_DEFINITION.put("2MK3", "Shallow water terdiurnal constituent");
        COEFF_DEFINITION.put("K2", "Lunisolar semidiurnal constituent");
        COEFF_DEFINITION.put("M8", "Shallow water eighth diurnal constituent");
        COEFF_DEFINITION.put("MS4", "Shallow water quarter diurnal constituent");
    }

    private final static String[] ORDERED_COEFF = {
            "M2", "S2", "N2", "K1", "M4", "O1", "M6", "MK3", "S4",
            "MN4", "NU2", "S6", "MU2", "2N2", "OO1", "LAM2", "S1", "M1",
            "J1", "MM", "SSA", "SA", "MSF", "MF", "RHO", "Q1", "T2",
            "R2", "2Q1", "P1", "2SM2", "M3", "L2", "2MK3", "K2", "M8", "MS4"};

    public TideUtilities() {
        super();
    }

    public static TreeMap<String, StationTreeNode> buildStationTree(Set<TideStation> stations) {
        final TreeMap<String, StationTreeNode> set = new TreeMap<>();
        stations.stream().forEach( station -> addStationToTree(station, set));
        return set;
    }

    public static TreeMap<String, StationTreeNode> buildStationTree(String stationFileName) {
        InputSource source = null;
        try {
            source = new InputSource(new FileInputStream(new File(stationFileName)));
            source.setEncoding("ISO-8859-1");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return buildStationTree(source);
    }

    public static TreeMap<String, StationTreeNode> buildStationTree(InputSource stationFileInputSource) {
        TreeMap<String, StationTreeNode> set = new TreeMap<>();
        StationObserver stationObserver = new StationObserver();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            stationObserver.setTreeToPopulate(set);
            saxParser.parse(stationFileInputSource, stationObserver);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return set;
    }

    public static String[] getOrderedCoeff() {
        return ORDERED_COEFF.clone();
    }

    public static double feetToMeters(double d) {
        return d * FEET_2_METERS;
    }

    public static double metersToFeet(double d) {
        return d / FEET_2_METERS;
    }

    public static double getWaterHeight(TideStation station, List<Coefficient> constSpeed, LocalDateTime when) {
        return getWaterHeight(when, station, constSpeed, true);
    }

    public static double getWaterHeight(LocalDateTime when, TideStation station, List<Coefficient> constSpeed) {
        return getWaterHeight(when, station, constSpeed, false);
    }

    protected static double getWaterHeight(LocalDateTime date, TideStation station, List<Coefficient> constSpeed, boolean b) {
        double value = 0d;

        LocalDateTime jan1st = LocalDate.of(date.getYear(), Month.JANUARY, 1).atStartOfDay();

        long d1 = date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long j1 = jan1st.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        double stationBaseHeight = station.getBaseHeight();
        long nbSecSinceJan1st = (d1 - j1) / 1000L;
        double timeOffset = nbSecSinceJan1st * 0.00027777777777777778D;
        value = stationBaseHeight;
        for (int i = 0; i < constSpeed.size(); i++) {
            if (station.getHarmonics().get(i).getName().equals(constSpeed.get(i).getName())) {
                value += (station.getHarmonics().get(i).getAmplitude() * Math.cos(constSpeed.get(i).getValue() * timeOffset - station.getHarmonics().get(i).getEpoch()));
            }
        }
        if (station.getUnit().contains("^2")) {
            value = (value >= 0.0D ? Math.sqrt(value) : -Math.sqrt(-value));
        }
        return value;
    }

    public static String getHarmonicCoefficientName(TideStation station, List<Coefficient> constSpeed, int constSpeedIdx) {
        String name = "";
        if (station != null) {
            name = station.getHarmonics().get(constSpeedIdx).getName();
        } else {
            name = constSpeed.get(constSpeedIdx).getName();
        }
        return name;
    }

    public static String getHarmonicCoefficientDefinition(String name) {
        return COEFF_DEFINITION.get(name);
    }

    public static double getHarmonicValue(Date when, Date yearJan1st, TideStation ts, List<Coefficient> constSpeed, int constSpeedIdx) {
        double value = 0d;
        double stationBaseHeight = ts.getBaseHeight();
        long nbSecSinceJan1st = (when.getTime() - yearJan1st.getTime()) / 1000L;
        double timeOffset = nbSecSinceJan1st * 0.00027777777777777778D;

        value = stationBaseHeight;
        value += (ts.getHarmonics().get(constSpeedIdx).getAmplitude() * Math.cos(constSpeed.get(constSpeedIdx).getValue() * timeOffset - ts.getHarmonics().get(constSpeedIdx).getEpoch()));
        if (ts.getUnit().contains("^2")) {
            value = (value >= 0.0D ? Math.sqrt(value) : -Math.sqrt(-value));
        }
        return value;
    }

    public static double getHarmonicValue(Date when, Date yearJan1st, TideStation ts, List<Coefficient> constSpeed, String coeffName) {
        double value = 0d;
        double stationBaseHeight = ts.getBaseHeight();
        long nbSecSinceJan1st = (when.getTime() - yearJan1st.getTime()) / 1000L;
        double timeOffset = nbSecSinceJan1st * 0.00027777777777777778D;
        value = stationBaseHeight;

        int constSpeedIdx = getHarmonicIndex(ts.getHarmonics(), coeffName);
        value += (ts.getHarmonics().get(constSpeedIdx).getAmplitude() * Math.cos(constSpeed.get(constSpeedIdx).getValue() * timeOffset - ts.getHarmonics().get(constSpeedIdx).getEpoch()));
        if (ts.getUnit().contains("^2")) {
            value = (value >= 0.0D ? Math.sqrt(value) : -Math.sqrt(-value));
        }
        return value;
    }

    public static int getHarmonicIndex(List<Harmonic> harmonics, String name) {
        int idx = 0;
        boolean found = false;
        for (Harmonic harmonic : harmonics) {
            if (harmonic.getName().equals(name)) {
                found = true;
                break;
            } else {
                idx++;
            }
        }
        return (found ? idx : -1);
    }

    public static double[] getMinMaxWH(TideStation station, List<Coefficient> constSpeed, LocalDateTime when) {
        double[] minMax = {0d, 0d};
        if (station != null) {
            // Calculate min/max, for the graph
            int year = when.getYear();
            // Calc Jan 1st of the current year
            LocalDate jan1st = LocalDate.of(year, Month.JANUARY, 1);
            LocalDateTime dec31st = jan1st.atStartOfDay().withMonth(12).withDayOfMonth(31).withHour(12);
            double max = -Double.MAX_VALUE;
            double min = Double.MAX_VALUE;
            LocalDateTime date = jan1st.atStartOfDay();
            while (date.isBefore(dec31st)) {
                double d = getWaterHeight(date, station, constSpeed);
                max = Math.max(max, d);
                min = Math.min(min, d);

                date.plusHours(2);
            }
            minMax[MIN_POS] = min;
            minMax[MAX_POS] = max;
        }
        return minMax;
    }

    public static double[] getMinMaxWH(TideStation station, List<Coefficient> constSpeed, LocalDateTime from, LocalDateTime to) {
        double[] minMax = {0d, 0d};
        if (station != null) {
            double max = -Double.MAX_VALUE;
            double min = Double.MAX_VALUE;
            // Calculate min/max, for the graph
            LocalDateTime date = LocalDateTime.from(from);
            while (date.isBefore(to)) {
                // Calc Jan 1st of the current year
                double d = getWaterHeight(date, station, constSpeed);
                max = Math.max(max, d);
                min = Math.min(min, d);
                date.plusHours(2);
            }
            minMax[MIN_POS] = min;
            minMax[MAX_POS] = max;
        }
        return minMax;
    }

    /**
     * The "unit" param can be "meters" or "feet"
     * @param value double
     * @param station TideStation
     * @param unit String
     * @return double
     */
    public static double getWaterHeightIn(double value, TideStation station, String unit) {
        double val = value;
        if (station.isCurrentStation()) {
            throw new RuntimeException(station.getFullName() + " is a current station. Method getWaterHeightIn applies only to tide stations.");
        }
        if (!unit.equals(station.getUnit())) {
            if (!unit.equals(TideStation.METERS) && !unit.equals(TideStation.FEET)) {
                throw new RuntimeException("Unsupported unit [" + unit + "]. Only " + TideStation.METERS + " or " + TideStation.FEET + " please.");
            }
            if (unit.equals(TideStation.METERS) && station.getUnit().equals(TideStation.FEET)) {
                val *= FEET_2_METERS;
            } else {
                val /= FEET_2_METERS;
            }
        }
        return val;
    }

    public static List<String[]> getStationHarmonicConstituents(TideStation station, List<Coefficient> constSpeed) {
        List<String[]> harmonicsConstituentsList = new ArrayList<>();
        int rank = 1;

        Calendar now = GregorianCalendar.getInstance();
        Calendar jan1st = new GregorianCalendar(now.get(Calendar.YEAR), Calendar.JANUARY, 1);

        long nbSecSinceJan1st = (now.getTimeInMillis() - jan1st.getTimeInMillis()) / 1000L;
        double timeOffset = nbSecSinceJan1st * 0.00027777777777777778D;

        for (String key : COEFF_DEFINITION.keySet()) {
            int constIdx = getHarmonicIndex(station.getHarmonics(), key);
            if (constIdx > -1) {
                double amplitude = station.getHarmonics().get(constIdx).getAmplitude();
                double epoch = station.getHarmonics().get(constIdx).getEpoch();
                double speed = constSpeed.get(constIdx).getValue();
                double phase = Math.toDegrees(speed * timeOffset - epoch) % 360;

                String[] line = {
                        Integer.toString(rank),
                        key,
                        DF13.format(amplitude),
                        DF31.format(phase),
                        DF36.format(speed)
                };
                harmonicsConstituentsList.add(line);
                rank++;
            }
        }
        return harmonicsConstituentsList;
    }

    private static void addStationToTree(TideStation station, TreeMap<String, StationTreeNode> currentTree) {
        String timeZoneLabel = "";
        try {
            timeZoneLabel = station.getTimeZone().substring(0, station.getTimeZone().indexOf("/"));
        } catch (Exception ex) {
            System.err.println(ex + " for " + station.getFullName() + " , " + station.getTimeZone());
        }
        StationTreeNode treeNode = currentTree.get(timeZoneLabel);
        if (treeNode == null) {
            treeNode = new StationTreeNode(timeZoneLabel);
            currentTree.put(timeZoneLabel, treeNode);
        }
        currentTree = treeNode.getSubTree();

        String timeZoneLabelPart2 = station.getTimeZone().substring(station.getTimeZone().indexOf("/") + 1);
        treeNode = currentTree.get(timeZoneLabelPart2);
        if (treeNode == null) {
            treeNode = new StationTreeNode(timeZoneLabelPart2);
            currentTree.put(timeZoneLabelPart2, treeNode);
        }
        currentTree = treeNode.getSubTree();

        StationTreeNode stationTreeNode = null;
        for (String name : station.getNameParts()) {
            stationTreeNode = currentTree.get(name);
            if (stationTreeNode == null) {
                stationTreeNode = new StationTreeNode(name);
                stationTreeNode.setStationType(station.isCurrentStation() ? StationTreeNode.CURRENT_STATION : StationTreeNode.TIDE_STATION);
                currentTree.put(name, stationTreeNode);
            }
            currentTree = stationTreeNode.getSubTree();
        }
        stationTreeNode.setFullStationName(station.getFullName());
    }

}