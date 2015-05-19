package es.tidetim.tideengine.services;

import es.tidetim.tideengine.models.Coefficient;
import es.tidetim.tideengine.models.Harmonic;
import es.tidetim.tideengine.models.TideStation;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class TideUtilities {
    private final static boolean verbose = false;
    public final static double FEET_2_METERS = 0.30480061d; // US feet to meters
    public final static double COEFF_FOR_EPOCH = 0.017453292519943289D;
    public final static DecimalFormat DF22 = new DecimalFormat("#0.00");
    public final static DecimalFormat DF22PLUS = new DecimalFormat("#0.00");

    static {
        DF22PLUS.setPositivePrefix("+");
    }

    public final static DecimalFormat DF2 = new DecimalFormat("#0");
    public final static DecimalFormat DF2PLUS = new DecimalFormat("#0");

    static {
        DF2PLUS.setPositivePrefix("+");
    }

    public final static DecimalFormat DF31 = new DecimalFormat("##0.0");
    public final static DecimalFormat DF13 = new DecimalFormat("##0.000");
    public final static DecimalFormat DF36 = new DecimalFormat("##0.000000");

    public final static Map<String, String> COEFF_DEFINITION = new HashMap<String, String>();

    static {
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

    private final static String[] ORDERED_COEFF = {"M2", "S2", "N2", "K1", "M4", "O1", "M6", "MK3", "S4",
            "MN4", "NU2", "S6", "MU2", "2N2", "OO1", "LAM2", "S1", "M1",
            "J1", "MM", "SSA", "SA", "MSF", "MF", "RHO", "Q1", "T2",
            "R2", "2Q1", "P1", "2SM2", "M3", "L2", "2MK3", "K2", "M8",
            "MS4"};

    public static String[] getOrderedCoeff() {
        return ORDERED_COEFF.clone();
    }

    public static TreeMap<String, StationTreeNode> buildStationTree(String stationFileName) {
        InputSource is = null;
        try {
            is = new InputSource(new FileInputStream(new File(stationFileName)));
            is.setEncoding("ISO-8859-1");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return buildStationTree(is);
    }

    public static TreeMap<String, StationTreeNode> buildStationTree(InputSource stationFileInputSource) {
        TreeMap<String, StationTreeNode> set = new TreeMap<>();

        long before = System.currentTimeMillis();
        StationObserver sf = new StationObserver();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            sf.setTreeToPopulate(set);
            saxParser.parse(stationFileInputSource, sf);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        long after = System.currentTimeMillis();
        if (verbose) System.out.println("Populating the tree took " + Long.toString(after - before) + " ms");

        return set;
    }

    public static TreeMap<String, StationTreeNode> buildStationTree(Set<TideStation> stations) {
        final TreeMap<String, StationTreeNode> set = new TreeMap<>();
        stations.stream().forEach( station -> addStationToTree(station, set));
        return set;
    }

    /**
     * Rendering on System.out
     *
     * @param tree
     * @param level
     */
    public static void renderTree(TreeMap<String, StationTreeNode> tree, int level) {
        Set<String> keys = tree.keySet();
        for (String key : keys) {
            StationTreeNode stn = tree.get(key);
            for (int i = 0; i < (2 * level); i++) // Indentation
                System.out.print(" ");
            System.out.println(stn.toString()); // Station name
            if (stn.getSubTree().size() > 0)
                renderTree(stn.getSubTree(), level + 1);
        }
    }

    public static double feetToMeters(double d) {
        return d * FEET_2_METERS;
    }

    public static double metersToFeet(double d) {
        return d / FEET_2_METERS;
    }

    public static double getWaterHeight(TideStation ts, List<Coefficient> constSpeed, LocalDateTime when) throws Exception {
        return getWaterHeight(when, ts, constSpeed, true);
    }

    public static double getWaterHeight(LocalDateTime d, TideStation ts, List<Coefficient> constSpeed) {
        return getWaterHeight(d, ts, constSpeed, false);
    }

    public static double getWaterHeight(LocalDateTime date, TideStation station, List<Coefficient> constSpeed, boolean b) {
        double value = 0d;

        LocalDateTime jan1st = LocalDate.of(date.getYear(), Month.JANUARY, 1).atStartOfDay();

        long d1 = date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long j1 = jan1st.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        double stationBaseHeight = station.getBaseHeight();
        long nbSecSinceJan1st = (d1 - j1) / 1000L;
        double timeOffset = nbSecSinceJan1st * 0.00027777777777777778D;
        value = stationBaseHeight;
        for (int i = 0; i < constSpeed.size(); i++) {
            assert (station.getHarmonics().get(i).getName().equals(constSpeed.get(i).getName()));
            if (!station.getHarmonics().get(i).getName().equals(constSpeed.get(i).getName()))
                System.out.println("..... Mismatch!!!");

            value += (station.getHarmonics().get(i).getAmplitude() * Math.cos(constSpeed.get(i).getValue() * timeOffset - station.getHarmonics().get(i).getEpoch()));
        }
        if (station.getUnit().indexOf("^2") > -1)
            value = (value >= 0.0D ? Math.sqrt(value) : -Math.sqrt(-value));

        return value;
    }

    public static String getHarmonicCoeffName(TideStation ts,
                                              List<Coefficient> constSpeed,
                                              int constSpeedIdx) {
        String name = "";
        if (ts != null)
            name = ts.getHarmonics().get(constSpeedIdx).getName();
        else
            name = constSpeed.get(constSpeedIdx).getName();
        return name;
    }

    public static String getHarmonicCoeffDefinition(String name) {
        return COEFF_DEFINITION.get(name);
    }

    public static double getHarmonicValue(Date d,
                                          Date jan1st,
                                          TideStation ts,
                                          List<Coefficient> constSpeed,
                                          int constSpeedIdx) {
        double value = 0d;

        double stationBaseHeight = ts.getBaseHeight();
        long nbSecSinceJan1st = (d.getTime() - jan1st.getTime()) / 1000L;
        double timeOffset = nbSecSinceJan1st * 0.00027777777777777778D;
        value = stationBaseHeight;

        value += (ts.getHarmonics().get(constSpeedIdx).getAmplitude() * Math.cos(constSpeed.get(constSpeedIdx).getValue() * timeOffset - ts.getHarmonics().get(constSpeedIdx).getEpoch()));
        if (ts.getUnit().indexOf("^2") > -1)
            value = (value >= 0.0D ? Math.sqrt(value) : -Math.sqrt(-value));

        return value;
    }

    public static double getHarmonicValue(Date d,
                                          Date jan1st,
                                          TideStation ts,
                                          List<Coefficient> constSpeed,
                                          String coeffName) {
        double value = 0d;

        double stationBaseHeight = ts.getBaseHeight();
        long nbSecSinceJan1st = (d.getTime() - jan1st.getTime()) / 1000L;
        double timeOffset = nbSecSinceJan1st * 0.00027777777777777778D;
        value = stationBaseHeight;

        int constSpeedIdx = getHarmonicIndex(ts.getHarmonics(), coeffName);

        value += (ts.getHarmonics().get(constSpeedIdx).getAmplitude() * Math.cos(constSpeed.get(constSpeedIdx).getValue() * timeOffset - ts.getHarmonics().get(constSpeedIdx).getEpoch()));
        if (ts.getUnit().indexOf("^2") > -1)
            value = (value >= 0.0D ? Math.sqrt(value) : -Math.sqrt(-value));

        return value;
    }

    public static int getHarmonicIndex(List<Harmonic> alh, String name) {
        int idx = 0;
        boolean found = false;
        for (Harmonic h : alh) {
            if (h.getName().equals(name)) {
                found = true;
                break;
            } else
                idx++;
        }
        if (!found)
            System.out.println("Coeff [" + name + "] not found.");
        return (found ? idx : -1);
    }

    public final static int MIN_POS = 0;
    public final static int MAX_POS = 1;

    public static double[] getMinMaxWH(TideStation ts, List<Coefficient> constSpeed, LocalDateTime when) throws Exception {
        double[] minMax = {0d, 0d};
        if (ts != null) {
            // Calculate min/max, for the graph
            int year = when.getYear();
            // Calc Jan 1st of the current year
            LocalDate jan1st = LocalDate.of(year, Month.JANUARY, 1);
            LocalDateTime dec31st = jan1st.atStartOfDay().withMonth(12).withDayOfMonth(31).withHour(12);
            double max = -Double.MAX_VALUE;
            double min = Double.MAX_VALUE;
            LocalDateTime date = jan1st.atStartOfDay();
            while (date.isBefore(dec31st)) {
                double d = getWaterHeight(date, ts, constSpeed);
                max = Math.max(max, d);
                min = Math.min(min, d);

                date.plusHours(2);
            }
            minMax[MIN_POS] = min;
            minMax[MAX_POS] = max;
        }
        return minMax;
    }

    public static double[] getMinMaxWH(TideStation ts, List<Coefficient> constSpeed, LocalDateTime from, LocalDateTime to) throws Exception {
        double[] minMax = {0d, 0d};
        if (ts != null) {
            double max = -Double.MAX_VALUE;
            double min = Double.MAX_VALUE;
            // Calculate min/max, for the graph
            LocalDateTime date = LocalDateTime.from(from);
            while (date.isBefore(to)) {
                // Calc Jan 1st of the current year
                double d = getWaterHeight(date, ts, constSpeed);
                max = Math.max(max, d);
                min = Math.min(min, d);

                date.plusHours(2);
            }
            minMax[MIN_POS] = min;
            minMax[MAX_POS] = max;
        }
        return minMax;
    }

    public static double getWaterHeightIn(double d, TideStation ts, String unit) {
        double val = d;
        if (ts.isCurrentStation())
            throw new RuntimeException(ts.getFullName() + " is a current station. Method getWaterHeightIn applies only to tide stations.");
        if (!unit.equals(ts.getUnit())) {
            if (!unit.equals(TideStation.METERS) && !unit.equals(TideStation.FEET))
                throw new RuntimeException("Unsupported unit [" + unit + "]. Only " + TideStation.METERS + " or " + TideStation.FEET + " please.");
            if (unit.equals(TideStation.METERS) && ts.getUnit().equals(TideStation.FEET))
                val *= FEET_2_METERS;
            else
                val /= FEET_2_METERS;
        }
        return val;
    }

    public static List<String[]> getStationHarmonicConstituents(TideStation ts, List<Coefficient> constSpeed) {
        List<String[]> hcList = new ArrayList<String[]>();
        int rank = 1;

        Calendar now = GregorianCalendar.getInstance();
        Calendar jan1st = new GregorianCalendar(now.get(Calendar.YEAR), Calendar.JANUARY, 1);

        long nbSecSinceJan1st = (now.getTimeInMillis() - jan1st.getTimeInMillis()) / 1000L;
        double timeOffset = nbSecSinceJan1st * 0.00027777777777777778D;

        for (String k : COEFF_DEFINITION.keySet()) {
            int constIdx = getHarmonicIndex(ts.getHarmonics(), k);
            if (constIdx > -1) {
                double amplitude = ts.getHarmonics().get(constIdx).getAmplitude();
                double epoch = ts.getHarmonics().get(constIdx).getEpoch();
                double speed = constSpeed.get(constIdx).getValue();
                double phase = Math.toDegrees(speed * timeOffset - epoch) % 360;

                String[] line = {Integer.toString(rank),
                        k,
                        DF13.format(amplitude),
                        DF31.format(phase),
                        DF36.format(speed)
                };
                hcList.add(line);
                rank++;
            }
        }
        return hcList;
    }

    private static void addStationToTree(TideStation ts, TreeMap<String, TideUtilities.StationTreeNode> currentTree) {
        String timeZoneLabel = "";
        try {
            timeZoneLabel = ts.getTimeZone().substring(0, ts.getTimeZone().indexOf("/"));
        } catch (Exception ex) {
            System.err.println(ex.toString() + " for " + ts.getFullName() + " , " + ts.getTimeZone());
        }
        StationTreeNode tzstn = currentTree.get(timeZoneLabel);
        if (tzstn == null) {
            tzstn = new StationTreeNode(timeZoneLabel);
            currentTree.put(timeZoneLabel, tzstn);
        }
        currentTree = tzstn.getSubTree();
        String timeZoneLabel2 = ts.getTimeZone().substring(ts.getTimeZone().indexOf("/") + 1);
        tzstn = currentTree.get(timeZoneLabel2);
        if (tzstn == null) {
            tzstn = new StationTreeNode(timeZoneLabel2);
            currentTree.put(timeZoneLabel2, tzstn);
        }
        currentTree = tzstn.getSubTree();

        StationTreeNode stn = null;
        for (String name : ts.getNameParts()) {
            stn = currentTree.get(name);
            if (stn == null) {
                stn = new StationTreeNode(name);
                stn.setStationType(ts.isCurrentStation() ? TideUtilities.StationTreeNode.CURRENT_STATION : TideUtilities.StationTreeNode.TIDE_STATION);
                currentTree.put(name, stn);
            }
            currentTree = stn.getSubTree();
        }
        stn.setFullStationName(ts.getFullName());
        // currentTree.put(ts.getFullName(), new StationTreeNode(ts.getFullName()));
    }

    public static class StationTreeNode implements Comparable {
        public final static int TIDE_STATION = 1;
        public final static int CURRENT_STATION = 2;

        private String label = "";
        private String fullStationName = null;
        private int stationType = 0;
        private TreeMap<String, StationTreeNode> subTree = new TreeMap<String, StationTreeNode>();

        public StationTreeNode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return this.label;
        }

        public int compareTo(Object o) {
            return this.label.compareTo(o.toString());
        }

        public TreeMap<String, TideUtilities.StationTreeNode> getSubTree() {
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

        public boolean equals(Object o) {
            return (o instanceof StationTreeNode && this.compareTo(o) == 0);
        }
    }

    public static class StationObserver extends DefaultHandler {
        private TideStation ts = null;

        private boolean foundStation = false;
        private boolean foundNameCollection = false;

        private TreeMap<String, TideUtilities.StationTreeNode> tree = null;

        public void setTreeToPopulate(TreeMap<String, TideUtilities.StationTreeNode> tree) {
            this.tree = tree;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            //    super.startElement(uri, localName, qName, attributes);
            if (!foundStation && "station".equals(qName)) {
                String name = attributes.getValue("name");
                foundStation = true;
                ts = new TideStation();
                ts.setFullName(name);
            } else if (foundStation) {
                if ("name-collection".equals(qName)) {
                    foundNameCollection = true;
                } else if ("name-part".equals(qName) && foundNameCollection) {
                    ts.getNameParts().add(attributes.getValue("name"));
                } else if ("position".equals(qName)) {
                    ts.setLatitude(Double.parseDouble(attributes.getValue("latitude")));
                    ts.setLongitude(Double.parseDouble(attributes.getValue("longitude")));
                } else if ("time-zone".equals(qName)) {
                    ts.setTimeZone(attributes.getValue("name"));
                    ts.setTimeOffset(attributes.getValue("offset"));
                } else if ("base-height".equals(qName)) {
                    ts.setBaseHeight(Double.parseDouble(attributes.getValue("value")));
                    ts.setUnit(attributes.getValue("unit"));
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            super.endElement(uri, localName, qName);
            if (foundStation && "station".equals(qName)) {
                foundStation = false;
                addStationToTree(ts, tree);
            } else if (foundNameCollection && "name-collection".equals(qName)) {
                foundNameCollection = false;
            }
        }
    }

}