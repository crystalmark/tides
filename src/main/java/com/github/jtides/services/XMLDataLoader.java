package com.github.jtides.services;

import com.github.jtides.models.Constituents;
import com.github.jtides.models.TideStation;
import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class XMLDataLoader {

    public final static String ARCHIVE_STREAM = "/xml/xml.zip";
    public final static String CONSTITUENTS_ENTRY = "constituents.xml";
    public final static String STATIONS_ENTRY = "stations.xml";

    public Constituents loadConstituentsFromXml() {
        SpeedConstituentFinder scf = new SpeedConstituentFinder();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            InputSource is = getZipInputSource(CONSTITUENTS_ENTRY);
            saxParser.parse(is, scf);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return scf.getConstituents();
    }

    public Set<TideStation> loadTideStationsFromXml() {
        Set<TideStation> stations = new HashSet<>();
        StationFinder stationFinder = new StationFinder(stations);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            InputSource source = getZipInputSource(STATIONS_ENTRY);
            saxParser.parse(source, stationFinder);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return stations;
    }

    public TideStation loadTideStation(String stationName) {
        StationFinder stationFinder = new StationFinder();
        stationFinder.setStationName(stationName);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            InputSource source = getZipInputSource(STATIONS_ENTRY);
            saxParser.parse(source, stationFinder);
        } catch (DoneWithSiteException dwse) {
            System.err.println(dwse.getLocalizedMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return stationFinder.getTideStation();
    }

    private InputSource getZipInputSource(String entryName) throws Exception {
        InputStream zipStream = XMLDataLoader.class.getResourceAsStream(ARCHIVE_STREAM);
        if (zipStream != null) {
            ZipInputStream zip = new ZipInputStream(zipStream);
            InputSource inputSource = null;
            boolean hasNext = true;
            while (hasNext) {
                ZipEntry zipEntry = zip.getNextEntry();
                if (zipEntry == null) {
                    hasNext = false;
                } else {
                    if (zipEntry.getName().equals(entryName)) {
                        inputSource = new InputSource(zip);
                        inputSource.setEncoding("ISO-8859-1");
                        hasNext = false;
                    }
                }
            }
            if (inputSource == null) {
                throw new RuntimeException("Entry " + entryName + " not found in " + ARCHIVE_STREAM);
            }
            return inputSource;
        } else {
            throw new IllegalStateException("Not able to read zip file with stations and constituents");
        }
    }

}
