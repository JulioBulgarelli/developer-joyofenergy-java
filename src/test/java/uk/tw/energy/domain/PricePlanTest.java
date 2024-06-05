package uk.tw.energy.domain;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;

class PricePlanTest {

  private static final String ENERGY_SUPPLIER_NAME = "Energy Supplier Name";

  @Test
  void shouldReturnTheEnergySupplierGivenInTheConstructor() {
    PricePlan pricePlan = new PricePlan(null, ENERGY_SUPPLIER_NAME, null, null);

    assertThat(pricePlan.getEnergySupplier()).isEqualTo(ENERGY_SUPPLIER_NAME);
  }

  @Test
  void shouldReturnTheBasePriceGivenAnOrdinaryDateTime() {
    LocalDateTime normalDateTime = LocalDateTime.of(2017, Month.AUGUST, 31, 12, 0, 0);
    PeakTimeMultiplier peakTimeMultiplier =
        new PeakTimeMultiplier(DayOfWeek.WEDNESDAY, BigDecimal.TEN);
    PricePlan pricePlan =
        new PricePlan(null, null, BigDecimal.ONE, singletonList(peakTimeMultiplier));

    BigDecimal price = pricePlan.getPrice(normalDateTime);

    assertThat(price).isCloseTo(BigDecimal.ONE, Percentage.withPercentage(1));
  }

  @Test
  void shouldReturnAnExceptionPriceGivenExceptionalDateTime() {
    LocalDateTime exceptionalDateTime = LocalDateTime.of(2017, Month.AUGUST, 30, 23, 0, 0);
    PeakTimeMultiplier peakTimeMultiplier =
        new PeakTimeMultiplier(DayOfWeek.WEDNESDAY, BigDecimal.TEN);
    PricePlan pricePlan =
        new PricePlan(null, null, BigDecimal.ONE, singletonList(peakTimeMultiplier));

    BigDecimal price = pricePlan.getPrice(exceptionalDateTime);

    assertThat(price).isCloseTo(BigDecimal.TEN, Percentage.withPercentage(1));
  }

  @Test
  void shouldReceiveMultipleExceptionalDateTimes() {
    LocalDateTime exceptionalDateTime = LocalDateTime.of(2017, Month.AUGUST, 30, 23, 0, 0);
    PeakTimeMultiplier peakTimeMultiplier =
        new PeakTimeMultiplier(DayOfWeek.WEDNESDAY, BigDecimal.TEN);
    PeakTimeMultiplier otherPeakTimeMultiplier =
        new PeakTimeMultiplier(DayOfWeek.TUESDAY, BigDecimal.TEN);
    List<PeakTimeMultiplier> peakTimeMultipliers =
        Arrays.asList(peakTimeMultiplier, otherPeakTimeMultiplier);
    PricePlan pricePlan = new PricePlan(null, null, BigDecimal.ONE, peakTimeMultipliers);

    BigDecimal price = pricePlan.getPrice(exceptionalDateTime);

    assertThat(price).isCloseTo(BigDecimal.TEN, Percentage.withPercentage(1));
  }
}
