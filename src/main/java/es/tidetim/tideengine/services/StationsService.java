package es.tidetim.tideengine.services;

import es.tidetim.tideengine.models.Constituents;
import es.tidetim.tideengine.models.TideStation;

import java.util.Set;

public interface StationsService {

	TideStation loadTideStation(String stationName);

	Constituents buildConstituents() throws Exception;

	Set<TideStation> loadTideStations();
}