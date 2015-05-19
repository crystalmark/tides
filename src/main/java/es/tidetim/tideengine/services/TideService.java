package es.tidetim.tideengine.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.tidetim.tideengine.models.TideStation;
import es.tidetim.tideengine.models.TimedValue;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Controller
@RequestMapping("/tides")
public class TideService {

    private static final Logger LOG = LoggerFactory
            .getLogger(TideService.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyyMMdd");

    @Value("${elasticsearch.cluster:crystalmark}")
    private String cluster;
    @Value("${elasticsearch.index:tides}")
    private String indexName;
    @Value("${elasticsearch.port:9300}")
    private int port;
    @Value("${elasticsearch.host:localhost}")
    private String host;

    @Autowired
    TideCalculator calculator;

    @Autowired
    TideStationService tideStationsService;

    @RequestMapping(value = "{location}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    List<TimedValue> getTideHeightAtTimeAndPlace(
            @PathVariable("location") String location,
            @RequestParam(value = "date", required = false) String date) {

        LocalDate calendar = getDate(date);

        try {
            return calculator.getHighAndLowTides(location, calendar);
        } catch (Exception e) {
            LOG.error("Unable to find tide times for " + location, e);
            return null;
        }
    }

    private LocalDate getDate(String date) {
        if (!StringUtils.isEmpty(date)) {
            try {
                return LocalDateTime.ofInstant(
                        dateFormat.parse(date).toInstant(),
                        ZoneId.systemDefault()).toLocalDate();
            } catch (ParseException e) {
                LOG.error("Unable to parse {} to date", date, e);
            }
        }
        return LocalDate.now();
    }

    @RequestMapping(value = "hourly/{location}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    List<TimedValue> getTides(
            @PathVariable("location") String location,
            @RequestParam(value = "date", required = false) String date) {

        LocalDate calendar = getDate(date);

        try {
            return calculator.getTides(location, calendar, 60);
        } catch (Exception e) {
            LOG.error("Unable to find tide times for " + location, e);
            return null;
        }
    }

    @RequestMapping(value = "stations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Collection<String> getStations() throws Exception {
        return tideStationsService.getTideStations().stream().map(TideStation::getFullName).sorted().collect(toList());
    }

    @RequestMapping(value = "load", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    String load() throws Exception {
        // Create Client
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", cluster).build();
        TransportClient tclient = new TransportClient(settings);
        try {
            final TransportClient client = tclient
                    .addTransportAddress(new InetSocketTransportAddress(host,
                            port));

            final ObjectMapper mapper = new ObjectMapper();

            boolean hasIndex = client.admin().indices()
                    .exists(new IndicesExistsRequest("indexName")).actionGet()
                    .isExists();

            if (!hasIndex) {
                CreateIndexRequestBuilder createIndexRequestBuilder = client
                        .admin().indices().prepareCreate(indexName);
                createIndexRequestBuilder.execute().actionGet();
            }

            tideStationsService.getTideStations()
                    .stream()
                    .forEach(
                            station -> {
                                try {
                                    client.prepareIndex(indexName, "station")
                                            .setId(station.getFullName())
                                            .setSource(
                                                    mapper.writeValueAsString(station))
                                            .execute().actionGet();
                                } catch (Exception e) {
                                    LOG.error("Unable to parse " + station, e);
                                }
                            });

            return "{\"result\":\"success\"}";
        } finally {
            tclient.close();
        }
    }
}
