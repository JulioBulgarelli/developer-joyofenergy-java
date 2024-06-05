package uk.tw.energy.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.tw.energy.domain.ElectricityReading;

@Service
public class MeterReadingService {

  private final Logger log = LoggerFactory.getLogger(MeterReadingService.class);

  private final Map<String, List<ElectricityReading>> meterAssociatedReadings;

  public MeterReadingService(Map<String, List<ElectricityReading>> meterAssociatedReadings) {
    log.info("Instantiating Bean || meterAssociatedReadings = {}", meterAssociatedReadings);
    this.meterAssociatedReadings = meterAssociatedReadings;
    log.info("Instantiated Bean successfully");
  }

  public Optional<List<ElectricityReading>> getReadings(
      String smartMeterId, Integer offset, Integer limit) {
    log.info(
        "Started ::getReadings || smartMeterId = {} || offset = {} || limit = {}",
        smartMeterId,
        offset,
        limit);

    if (!meterAssociatedReadings.containsKey(smartMeterId)) {
      log.info("Error ::getReadings || not-found");
      return Optional.empty();
    }

    List<ElectricityReading> readings = getPagedReadings(smartMeterId, offset, limit);

    log.info("Finished ::getReadings || readings = {}", readings);
    return Optional.of(readings);
  }

  public void storeReadings(String smartMeterId, List<ElectricityReading> electricityReadings) {
    log.info(
        "Started ::storeReadings || smartMeterId = {} || electricityReadings = {}",
        smartMeterId,
        electricityReadings);

    meterAssociatedReadings
        .computeIfAbsent(smartMeterId, s -> new ArrayList<>())
        .addAll(electricityReadings);

    log.info(
        "Finished ::storeReadings || meterAssociatedReadings.get(smartMeterId) = {}",
        meterAssociatedReadings.get(smartMeterId));
  }

  private List<ElectricityReading> getPagedReadings(
      String smartMeterId, Integer offset, Integer limit) {
    log.info(
        "Started ::getPagedReadings || smartMeterId = {} || offset = {} || limit = {}",
        smartMeterId,
        offset,
        limit);

    List<ElectricityReading> allReadings = meterAssociatedReadings.get(smartMeterId);

    if (allReadings.isEmpty()) {
      log.info("Finished ::getPagedReadings || readings = []");
      return new ArrayList<>();
    }

    int endIndex = (offset + limit);

    List<ElectricityReading> readings =
        allReadings
            .subList(
                (offset < allReadings.size()) ? offset : allReadings.size(),
                (offset < allReadings.size() && endIndex < allReadings.size())
                    ? endIndex
                    : allReadings.size())
            .stream()
            .sorted(Comparator.comparing(ElectricityReading::time))
            .toList();

    log.info("Finished ::getPagedReadings || readings = {}", readings);
    return readings;
  }
}
