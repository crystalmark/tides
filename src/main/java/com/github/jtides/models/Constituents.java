package com.github.jtides.models;

import java.util.LinkedHashMap;
import java.util.Map;

public class Constituents {

  private final Map<String, ConstSpeed> constSpeedMap = new LinkedHashMap<>();
  
  public Map<String, ConstSpeed> getConstSpeedMap() {
    return constSpeedMap;
  }

}
