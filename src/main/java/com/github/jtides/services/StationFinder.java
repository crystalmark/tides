package com.github.jtides.services;

import com.github.jtides.models.Harmonic;
import com.github.jtides.models.TideStation;
import com.github.jtides.util.TideUtilities;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Set;

public class StationFinder extends DefaultHandler {

    private String stationName = "";
    private TideStation tideStation = null;
    private Set<TideStation> stations;

    private boolean foundStation = false;
    private boolean foundNameCollection = false;
    private boolean foundStationData = false;

    public StationFinder() {
        super();
    }

    public StationFinder(Set<TideStation> stations) {
        this.stations = stations;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public TideStation getTideStation() {
        return tideStation;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (!foundStation && "station".equals(qName)) {
            String name = attributes.getValue("name");
            if (name.contains(this.stationName)) {
                foundStation = true;
                tideStation = new TideStation();
                tideStation.setFullName(name);
            }
        } else if (foundStation) {
            if ("name-collection".equals(qName)) {
                foundNameCollection = true;
            } else if ("name-part".equals(qName) && foundNameCollection) {
                tideStation.getNameParts().add(attributes.getValue("name"));
            } else if ("position".equals(qName)) {
                tideStation.setLatitude(Double.parseDouble(attributes.getValue("latitude")));
                tideStation.setLongitude(Double.parseDouble(attributes.getValue("longitude")));
            } else if ("time-zone".equals(qName)) {
                tideStation.setTimeZone(attributes.getValue("name"));
                tideStation.setTimeOffset(attributes.getValue("offset"));
            } else if ("base-height".equals(qName)) {
                tideStation.setBaseHeight(Double.parseDouble(attributes.getValue("value")));
                tideStation.setUnit(attributes.getValue("unit"));
            } else if ("station-data".equals(qName)) {
                foundStationData = true;
            } else if (foundStationData && "harmonic-coeff".equals(qName)) {
                String name = attributes.getValue("name");
                double amplitude = Double.parseDouble(attributes.getValue("amplitude"));
                double epoch = Double.parseDouble(attributes.getValue("epoch")) * TideUtilities.COEFF_FOR_EPOCH;
                Harmonic h = new Harmonic(name, amplitude, epoch);
                tideStation.getHarmonics().add(h);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (foundStation && "station".equals(qName)) {
            foundStation = false;
            if (stations == null) {
                throw new DoneWithSiteException("Done with it.");
            } else {
                stations.add(tideStation);
            }
        } else if (foundNameCollection && "name-collection".equals(qName)) {
            foundNameCollection = false;
        } else if (foundStationData && "station-data".equals(qName)) {
            foundStationData = false;
        }
    }

}
