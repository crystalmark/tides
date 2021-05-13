package com.github.jtides.models;

import java.util.HashMap;
import java.util.Map;

public class ConstSpeed {

    private int idx = 0;
    private String coeffName = "";
    private double coeffValue = 0d;
    private final Map<Integer, Double> equilibrium = new HashMap<Integer, Double>();
    private final Map<Integer, Double> factors     = new HashMap<Integer, Double>();

    public ConstSpeed(int idx, String name, double val) {
        this.idx = idx;
        this.coeffName = name;
        this.coeffValue = val;
    }

    public void putEquilibrium(int year, double val) {
        equilibrium.put(new Integer(year), new Double(val));
    }

    public void putFactor(int year, double val) {
        factors.put(new Integer(year), new Double(val));
    }

    public String getCoeffName() {
        return coeffName;
    }

    public double getCoeffValue() {
        return coeffValue;
    }

    public Map<Integer, Double> getEquilibrium() {
        return equilibrium;
    }

    public Map<Integer, Double> getFactors() {
        return factors;
    }

}
