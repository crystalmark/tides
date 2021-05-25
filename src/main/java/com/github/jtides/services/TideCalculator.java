package com.github.jtides.services;

import com.github.jtides.models.Coefficient;
import com.github.jtides.models.TideStation;
import com.github.jtides.models.TideType;
import com.github.jtides.models.TimedValue;
import com.github.jtides.util.TideUtilities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.jtides.models.TideType.FALLING;
import static com.github.jtides.models.TideType.RISING;

public class TideCalculator {

    private final XMLTideStationService tideService;

    public TideCalculator(XMLTideStationService tideService){
        this.tideService = tideService;
    }

    public List<TimedValue> getTides(String location, LocalDate now, int period) throws Exception {

        List<TimedValue> tides = new LinkedList<>();
        double previousWH = Double.NaN;

        TideType trend = null;
        TideStation ts = tideService.getTideStation(location, now.getYear());
        List<Coefficient> constSpeed = tideService.getSiteConstSpeed();

        // Goes back 6 hours so we can calculate the tide type for midnight
        LocalDateTime cal = now.atStartOfDay().minusMinutes(period);

        while (cal.getDayOfYear() <= now.getDayOfYear() && cal.getYear() <= now.getYear()) {
            double wh = TideUtilities.getWaterHeight(ts, constSpeed, cal);

            TimedValue tide = new TimedValue(cal, wh);
            tides.add(tide);

            if (Double.isNaN(previousWH)) {
                previousWH = wh;
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
                            }
                            break;
                        case FALLING:
                            if (previousWH < wh) // Now going up
                            {
                                tide.setType(TideType.LW);
                                trend = RISING; // Now rising
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
        return getTides(location, day, 1).stream().filter(tide -> isLowOrHighTide(tide)).collect(Collectors.toList());
    }

    protected boolean isLowOrHighTide(TimedValue tide) {
        return tide.getType() != null && (tide.getType().equals(TideType.HW) || tide.getType().equals(TideType.LW));
    }

}
