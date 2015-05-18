package es.tidetim.tideengine.services;

import es.tidetim.tideengine.models.Coefficient;
import es.tidetim.tideengine.models.TideStation;

import java.util.Collection;
import java.util.List;

public interface TideStationService {
    TideStation getTideStation(String location, int year) throws Exception;

    List<Coefficient> getSiteConstSpeed() throws Exception;

    Collection<TideStation> getTideStations();
}
