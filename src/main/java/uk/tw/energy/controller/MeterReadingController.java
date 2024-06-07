package uk.tw.energy.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.ElectricityReadingInterval;
import uk.tw.energy.domain.MeterReadings;
import uk.tw.energy.service.MeterReadingService;
import uk.tw.energy.service.PricePlanService;

@RestController
@RequestMapping("/readings")
public class MeterReadingController {

    private final MeterReadingService meterReadingService;
    private final PricePlanService pricePlanService;

    public MeterReadingController(MeterReadingService meterReadingService, PricePlanService pricePlanService) {
        this.meterReadingService = meterReadingService;
        this.pricePlanService = pricePlanService;
    }

    @PostMapping("/store")
    public ResponseEntity storeReadings(@RequestBody MeterReadings meterReadings) {
        if (!isMeterReadingsValid(meterReadings)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        meterReadingService.storeReadings(meterReadings.smartMeterId(), meterReadings.electricityReadings());
        return ResponseEntity.ok().build();
    }

    private boolean isMeterReadingsValid(MeterReadings meterReadings) {
        String smartMeterId = meterReadings.smartMeterId();
        List<ElectricityReading> electricityReadings = meterReadings.electricityReadings();
        return smartMeterId != null
                && !smartMeterId.isEmpty()
                && electricityReadings != null
                && !electricityReadings.isEmpty();
    }

    @GetMapping("/read/{smartMeterId}")
    public ResponseEntity readReadings(@PathVariable String smartMeterId) {
        Optional<List<ElectricityReading>> readings = meterReadingService.getReadings(smartMeterId);
        return readings.isPresent()
                ? ResponseEntity.ok(readings.get())
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/last-week-cost/{smartMeterId}")
    public ResponseEntity getCostFromLastWeek(@PathVariable String smartMeterId) {
        ElectricityReadingInterval interval = ElectricityReadingInterval.previousWeekInterval();

        Optional<BigDecimal> costs = pricePlanService
                .getConsumptionCostOfElectricityReadingsForEachPricePlanWithinRange(
                        smartMeterId,
                        interval.getStart(),
                        interval.getEnd());

        return costs.isPresent()
                ? ResponseEntity.ok(costs.get())
                : ResponseEntity.notFound().build();
    }
}
