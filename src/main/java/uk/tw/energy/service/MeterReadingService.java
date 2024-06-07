package uk.tw.energy.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.tw.energy.domain.ElectricityReading;

@Service
public class MeterReadingService {

    private final Map<String, List<ElectricityReading>> meterAssociatedReadings;

    public MeterReadingService(Map<String, List<ElectricityReading>> meterAssociatedReadings) {
        this.meterAssociatedReadings = meterAssociatedReadings;
    }

    public Optional<List<ElectricityReading>> getReadings(String smartMeterId) {
        return Optional.ofNullable(meterAssociatedReadings.get(smartMeterId));
    }

    public Optional<List<ElectricityReading>> getReadingsWithinRange(String smartMeterId, Instant from, Instant to) {
        return Optional.of(getReadings(smartMeterId).orElseThrow().stream()
                .filter(er -> er.time().isAfter(from) && er.time().isBefore(to)).toList());
    }

    public void storeReadings(String smartMeterId, List<ElectricityReading> electricityReadings) {
        if (!meterAssociatedReadings.containsKey(smartMeterId)) {
            meterAssociatedReadings.put(smartMeterId, new ArrayList<>());
        }
        meterAssociatedReadings.get(smartMeterId).addAll(electricityReadings);
    }
}
