package uk.tw.energy.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.PricePlan;

@Service
public class PricePlanService {

  private final Logger log = LoggerFactory.getLogger(PricePlanService.class);

  private final List<PricePlan> pricePlans;
  private final MeterReadingService meterReadingService;

  public PricePlanService(List<PricePlan> pricePlans, MeterReadingService meterReadingService) {
    log.info(
        "Instantiating Bean || pricePlans = {} || meterReadingService = {}",
        pricePlans,
        meterReadingService);
    this.pricePlans = pricePlans;
    this.meterReadingService = meterReadingService;
    log.info("Instantiated Bean successfully");
  }

  public Optional<Map<String, BigDecimal>> getConsumptionCostOfElectricityReadingsForEachPricePlan(
      String smartMeterId, Integer offset, Integer limit) {
    Optional<List<ElectricityReading>> electricityReadings =
        meterReadingService.getReadings(smartMeterId, offset, limit);

    return electricityReadings.map(
        readings ->
            pricePlans.stream()
                .collect(
                    Collectors.toMap(PricePlan::getPlanName, t -> calculateCost(readings, t))));
  }

  private BigDecimal calculateCost(
      List<ElectricityReading> electricityReadings, PricePlan pricePlan) {
    log.info(
        "Started ::calculateCost || electricityReadings = {} || pricePlan = {}",
        electricityReadings,
        pricePlan);

    if (electricityReadings.isEmpty()) {
      return BigDecimal.ZERO;
    } else if (electricityReadings.size() == 1) {
      ElectricityReading reading = electricityReadings.getFirst();

      return reading
          .reading()
          .divide(BigDecimal.valueOf(reading.time().getEpochSecond()), RoundingMode.HALF_UP);
    }

    BigDecimal average = calculateAverageReading(electricityReadings);
    BigDecimal timeElapsed = calculateTimeElapsed(electricityReadings);

    BigDecimal averagedCost = average.divide(timeElapsed, RoundingMode.HALF_UP);
    BigDecimal cost = averagedCost.multiply(pricePlan.getUnitRate());

    log.info("Finished ::calculateCost || cost = {}", cost);
    return cost;
  }

  private BigDecimal calculateAverageReading(List<ElectricityReading> electricityReadings) {
    log.info("Started ::calculateAverageReading || electricityReadings = {}", electricityReadings);

    BigDecimal summedReadings =
        electricityReadings.stream()
            .map(ElectricityReading::reading)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal averageReading =
        summedReadings.divide(BigDecimal.valueOf(electricityReadings.size()), RoundingMode.HALF_UP);

    log.info("Finished ::calculateAverageReading || averageReading = {}", averageReading);
    return averageReading;
  }

  private BigDecimal calculateTimeElapsed(List<ElectricityReading> electricityReadings) {
    log.info("Started ::calculateTimeElapsed || electricityReadings = {}", electricityReadings);

    // Since the readings are already sorted by time, we can leverage Collections APIs to get those
    ElectricityReading first = electricityReadings.getFirst();
    ElectricityReading last = electricityReadings.getLast();

    BigDecimal timeElapsed =
        BigDecimal.valueOf(Duration.between(first.time(), last.time()).getSeconds() / 3600.0);

    log.info("Finished ::calculateTimeElapsed || timeElapsed = {}", timeElapsed);
    return timeElapsed;
  }
}
