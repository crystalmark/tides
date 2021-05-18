package com.github.jtides.services;

import com.github.jtides.models.ConstSpeed;
import com.github.jtides.models.Constituents;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SpeedConstituentFinder extends DefaultHandler {

    private ConstSpeed constituent = null;
    private Constituents constituents = null;

    private boolean foundConstituent = false;
    private boolean foundCoeffName = false;
    private boolean foundCoeffValue = false;

    private boolean foundEquilibrium = false;
    private boolean foundFactor = false;

    private String coeffName = null;
    private int coeffIdx = -1;
    private double coeffValue = Double.NaN;

    private double value = 0D;
    private int year = -1;

    public SpeedConstituentFinder() {
        constituents = new Constituents();
    }

    public Constituents getConstituents() {
        return constituents;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (!foundConstituent && "const-speed".equals(qName)) {
            foundConstituent = true;
            coeffIdx = Integer.parseInt(attributes.getValue("idx"));
        } else if (foundConstituent && "coeff-name".equals(qName)) {
            foundCoeffName = true;
        } else if (foundConstituent && "coeff-value".equals(qName)) {
            foundCoeffValue = true;
        } else if (foundConstituent) {
            if ("equilibrium".equals(qName)) {
                foundEquilibrium = true;
                year = Integer.parseInt(attributes.getValue("year"));
            } else if ("factor".equals(qName)) {
                foundFactor = true;
                year = Integer.parseInt(attributes.getValue("year"));
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);

        if (coeffName != null && coeffIdx != -1 && !Double.isNaN(coeffValue)) {
            constituent = new ConstSpeed(coeffIdx, coeffName, coeffValue);
            coeffName = null;
            coeffIdx = -1;
            coeffValue = Double.NaN;
        }

        if (foundConstituent && "const-speed".equals(qName)) {
            foundConstituent = false;
            coeffName = null;
            coeffIdx = -1;
            coeffValue = Double.NaN;
            constituents.getConstSpeedMap().put(constituent.getCoeffName(), constituent);
        } else if ("coeff-name".equals(qName)) {
            foundCoeffName = false;
        } else if ("coeff-value".equals(qName)) {
            foundCoeffValue = false;
        }

        if ("equilibrium".equals(qName)) {
            constituent.getEquilibrium().put(year, value);
            foundEquilibrium = false;
        } else if ("factor".equals(qName)) {
            constituent.getFactors().put(year, value);
            foundFactor = false;
        }
    }

    public void characters(char[] chars, int start, int length) {
        String result = new String(chars).substring(start, start + length).trim();
        if (foundCoeffName)
            coeffName = result;
        else if (foundCoeffValue)
            coeffValue = Double.parseDouble(result);
        else if (foundEquilibrium) {
            value = Double.parseDouble(result);
        } else if (foundFactor) {
            value = Double.parseDouble(result);
        }
    }

}
