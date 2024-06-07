package uk.tw.energy.controller;

import java.util.List;
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
import uk.tw.energy.domain.MeterReadings;
import uk.tw.energy.service.MeterReadingService;

@RestController
@RequestMapping("/readings")
public class MeterReadingController {

    private final MeterReadingService meterReadingService;

    public MeterReadingController(MeterReadingService meterReadingService) {
        this.meterReadingService = meterReadingService;
    }

    @PostMapping
    public ResponseEntity<Void> storeReadings(@RequestBody MeterReadings meterReadings) {

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

    @GetMapping("/{smartMeterId}")
    public ResponseEntity<List<ElectricityReading>> readReadings(
            @PathVariable String smartMeterId,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {

        Optional<List<ElectricityReading>> readings = meterReadingService.getReadings(smartMeterId, offset, limit);

        return readings.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
