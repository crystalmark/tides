package tideengine;

import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Access method agnostic front end. Calls the right methods, depending on the
 * chosen option (XML, SQL, JAVA, json, etc)
 */
public class XMLTideService {
	private static Constituents constituentsObject = null;
	private static Stations stationsObject = null;
	
	@Autowired
	StationsService stationsService;

	public static Stations getStations() {
		return stationsObject;
	}

	public static Constituents getConstituents() {
		return constituentsObject;
	}

	private void connect() throws Exception {
		constituentsObject = XMLStationsService.buildConstituents(); // Uses
		stationsObject = XMLStationsService.getTideStations(); // Uses SAX
	}

	public static void disconnect() throws Exception {
	}

	public List<Coefficient> getSiteConstSpeed() throws Exception {
		return buildSiteConstSpeed(constituentsObject);
	}

	public double getAmplitudeFix(int year, String name)
			throws Exception {
		double d = 0;
		d = getAmplitudeFix(constituentsObject, year, name);
		return d;
	}

	public double getEpochFix(int year, String name) throws Exception {
		double d = 0;
		d = getEpochFix(constituentsObject, year, name);
		return d;
	}

	public TideStation findTideStation(String stationName, int year)
			throws Exception {
		TideStation ts = null;
		ts = findTideStation(stationName, year, constituentsObject,
				stationsObject);
		return ts;
	}

	public List<TideStation> getTideStations() throws Exception {
		return getTideStations(stationsObject);
	}

	public TreeMap<String, TideUtilities.StationTreeNode> getStationTree() {
		TreeMap<String, TideUtilities.StationTreeNode> st = null;

		st = TideUtilities.buildStationTree(stationsObject);
		return st;
	}

	private InputStream getZipInputStream(String zipStream,
			String entryName) throws Exception {
		ZipInputStream zip = new ZipInputStream(
				XMLStationsService.class.getResourceAsStream(zipStream));
		InputStream is = null;
		boolean go = true;
		while (go) {
			ZipEntry ze = zip.getNextEntry();
			if (ze == null)
				go = false;
			else {
				if (ze.getName().equals(entryName)) {
					is = zip;
					go = false;
				}
			}
		}
		if (is == null) {
			throw new RuntimeException("Entry " + entryName + " not found in "
					+ zipStream.toString());
		}
		return is;
	}

	private InputSource getZipInputSource(String filename,
			String entryName) throws Exception {
		InputStream zipStream = XMLStationsService.class
				.getResourceAsStream(filename);
		ZipInputStream zip = new ZipInputStream(zipStream);
		InputSource is = null;
		boolean go = true;
		while (go) {
			ZipEntry ze = zip.getNextEntry();
			if (ze == null)
				go = false;
			else {
				if (ze.getName().equals(entryName)) {
					is = new InputSource(zip);
					is.setEncoding("ISO-8859-1");
					go = false;
				}
			}
		}
		if (is == null) {
			throw new RuntimeException("Entry " + entryName + " not found in "
					+ filename.toString());
		}
		return is;
	}

	public List<Coefficient> getSiteConstSpeed(Constituents doc)
			throws Exception {
		List<Coefficient> csal = new ArrayList<>();
		Map<String, Constituents.ConstSpeed> csm = doc.getConstSpeedMap();
		Set<String> keys = csm.keySet();
		for (String k : keys) {
			Constituents.ConstSpeed cs = csm.get(k);
			Coefficient coef = new Coefficient(cs.getCoeffName(),
					cs.getCoeffValue() * TideUtilities.COEFF_FOR_EPOCH);
			csal.add(coef);
		}
		return csal;
	}

	public double getAmplitudeFix(Constituents doc, int year, String name)
			throws Exception {
		double d = 0;
		try {
			Constituents.ConstSpeed cs = doc.getConstSpeedMap().get(name);
			double f = cs.getFactors().get(year);
			d = f;
		} catch (Exception ex) {
			System.err.println("Error for [" + name + "] in [" + year + "]");
			throw ex;
		}
		return d;
	}

	public double getEpochFix(Constituents doc, int year, String name)
			throws Exception {
		double d = 0;
		try {
			Constituents.ConstSpeed cs = doc.getConstSpeedMap().get(name);
			double f = cs.getEquilibrium().get(year);
			d = f * TideUtilities.COEFF_FOR_EPOCH;
		} catch (Exception ex) {
			System.err.println("Error for [" + name + "] in [" + year + "]");
			throw ex;
		}
		return d;
	}

	public TideStation findTideStation(String stationName, int year,
			Constituents constituents, Stations stations) throws Exception {
		TideStation station = stations.getStations().get(stationName);
		if (station == null) // Try match
		{
			Set<String> keys = stations.getStations().keySet();
			for (String s : keys) {
				if (s.contains(stationName)) {
					station = stations.getStations().get(s);
					if (station != null)
						break;
				}
			}
		}

		if (station != null && station.yearHarmonicsFixed() != -1
				&& station.yearHarmonicsFixed() != year) // Then reload station
															// data from source
		{
			try {
				TideStation newTs = reloadTideStation(station.getFullName());
				stations.getStations().put(station.getFullName(), newTs);
				station = newTs;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		// Correction to the Harmonics
		if (station.yearHarmonicsFixed() == -1) {
			for (Harmonic harm : station.getHarmonics()) {
				String name = harm.getName();
				if (!"x".equals(name)) {
					double amplitudeFix = getAmplitudeFix(constituents, year,
							name);
					double epochFix = getEpochFix(constituents, year, name);

					harm.setAmplitude(harm.getAmplitude() * amplitudeFix);
					harm.setEpoch(harm.getEpoch() - epochFix);
				}
			}
			station.setHarmonicsFixedForYear(year);
		}
		return station;
	}

	private TideStation getTideStation(String stationName)
			throws Exception {
		return stationsService.getTideStation(stationName);
	}

	public List<TideStation> getTideStation(Stations stations)
			throws Exception {
		List<TideStation> stationData = new ArrayList<TideStation>();
		Set<String> keys = stations.getStations().keySet();
		for (String k : keys) {
			try {
				stationData.add(stations.getStations().get(k));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return stationData;
	}

}
