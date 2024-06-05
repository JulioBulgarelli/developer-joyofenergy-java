package uk.tw.energy.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MeterReadingServiceTest {

  private MeterReadingService meterReadingService;

  @BeforeEach
  public void setUp() {
    meterReadingService = new MeterReadingService(new HashMap<>());
  }

  @Test
  void givenMeterIdThatDoesNotExistShouldReturnNull() {
    assertThat(meterReadingService.getReadings("unknown-id", 0, 10)).isEmpty();
  }

  @Test
  void givenMeterReadingThatExistsShouldReturnMeterReadings() {
    meterReadingService.storeReadings("random-id", new ArrayList<>());
    assertThat(meterReadingService.getReadings("random-id", 0, 10))
        .isEqualTo(Optional.of(new ArrayList<>()));
  }
}
