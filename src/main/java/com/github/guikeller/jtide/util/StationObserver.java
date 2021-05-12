package com.github.guikeller.jtide.util;

import com.github.guikeller.jtide.models.TideStation;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.TreeMap;

public class StationObserver extends DefaultHandler {

    private TideStation ts = null;

    private boolean foundStation = false;
    private boolean foundNameCollection = false;

    private TreeMap<String, StationTreeNode> tree = null;

    public void setTreeToPopulate(TreeMap<String, StationTreeNode> tree) {
        this.tree = tree;
    }

    private static void addStationToTree(TideStation station, TreeMap<String, StationTreeNode> currentTree) {
        String timeZoneLabel = "";
        try {
            timeZoneLabel = station.getTimeZone().substring(0, station.getTimeZone().indexOf("/"));
        } catch (Exception ex) {
            System.err.println(ex.toString() + " for " + station.getFullName() + " , " + station.getTimeZone());
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
        if(stationTreeNode != null) {
            stationTreeNode.setFullStationName(station.getFullName());
        }
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
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (foundStation && "station".equals(qName)) {
            foundStation = false;
            addStationToTree(ts, tree);
        } else if (foundNameCollection && "name-collection".equals(qName)) {
            foundNameCollection = false;
        }
    }
}
