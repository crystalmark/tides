package com.github.guikeller.jtide.models;

import java.util.LinkedHashMap;
import java.util.Map;

public class Constituents {

  private Map<String, ConstSpeed> constSpeedMap = new LinkedHashMap<>();
  
  public Map<String, ConstSpeed> getConstSpeedMap() {
    return constSpeedMap;
  }

}
