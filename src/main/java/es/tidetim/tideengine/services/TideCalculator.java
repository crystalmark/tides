package es.tidetim.tideengine.services;

import static es.tidetim.tideengine.models.TideType.FALLING;
import static es.tidetim.tideengine.models.TideType.RISING;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import es.tidetim.tideengine.models.Coefficient;
import es.tidetim.tideengine.models.TideStation;
import es.tidetim.tideengine.models.TideType;
import es.tidetim.tideengine.models.TimedValue;

@Component
public class TideCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(TideCalculator.class);
    
    @Autowired
    TideStationService tideService;

    public List<TimedValue> getTides(String location, LocalDate now, int period) throws Exception {

        List<TimedValue> tides = new LinkedList<>();

        TideType trend = null;

        TideStation ts = tideService.getTideStation(location, now.getYear());

        List<Coefficient> constSpeed = tideService.getSiteConstSpeed();

        double previousWH = Double.NaN;

        // Go back 6 hours so we can calculate the tide type for midnight
        LocalDateTime cal = now.atStartOfDay().minusMinutes(period);

        while (cal.getDayOfYear() <= now.getDayOfYear() && cal.getYear() <= now.getYear()) {
            double wh = TideUtilities.getWaterHeight(ts, constSpeed, cal);

            TimedValue tide = new TimedValue(cal, wh);
            tides.add(tide);

            LOG.debug("Current tide time = {}", tide.getCalendar());

            if (Double.isNaN(previousWH)) {
                previousWH = wh;
                LOG.debug("Start height = {}", wh);
            } else {
                if (trend == null) {
                    if (previousWH > wh) {
                        trend = FALLING;
                    } else if (previousWH < wh) {
                        trend = RISING;
                    }
                    tide.setType(trend);
                } else {
                    switch (trend) {
                        case RISING:
                            if (previousWH > wh) // Now going down
                            {
                                tide.setType(TideType.HW);
                                trend = FALLING; // Now falling
                                LOG.debug("Found High Water at {}", cal);
                            }
                            break;
                        case FALLING:
                            if (previousWH < wh) // Now going up
                            {
                                tide.setType(TideType.LW);
                                trend = RISING; // Now rising
                                LOG.debug("Found Low Water at {}", cal);
                            }
                            break;
                        default: {
                            tide.setType(trend);
                            break;
                        }
                    }
                }
                previousWH = wh;
            }
            cal = cal.plusMinutes(period);
        }

        return tides.stream().filter(tide -> tide.getCalendar().getDayOfYear() == now.getDayOfYear()).sorted().collect(Collectors.toList());

    }

    public List<TimedValue> getHighAndLowTides(String location, LocalDate day) throws Exception {
        return getTides(location, day, 1).stream().filter(tide -> tide.getType() != null && (tide.getType().equals(TideType.HW) || tide.getType().equals(TideType.LW))).collect(Collectors.toList());
    }
}
