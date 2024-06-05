package uk.tw.energy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import uk.tw.energy.builders.MeterReadingsBuilder;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.MeterReadings;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = App.class)
class EndpointTest {

  @Autowired private TestRestTemplate restTemplate;

  private static HttpEntity<MeterReadings> toHttpEntity(MeterReadings meterReadings) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return new HttpEntity<>(meterReadings, headers);
  }

  @Test
  void shouldStoreReadings() {
    MeterReadings meterReadings = new MeterReadingsBuilder().generateElectricityReadings().build();
    HttpEntity<MeterReadings> entity = toHttpEntity(meterReadings);

    ResponseEntity<String> response = restTemplate.postForEntity("/readings", entity, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @SuppressWarnings("DataFlowIssue")
  @Test
  void givenMeterIdShouldReturnAMeterReadingAssociatedWithMeterId() {
    String smartMeterId = "alice";
    List<ElectricityReading> data =
        List.of(
            new ElectricityReading(Instant.parse("2024-04-26T00:00:10.00Z"), new BigDecimal(10)),
            new ElectricityReading(Instant.parse("2024-04-26T00:00:20.00Z"), new BigDecimal(20)),
            new ElectricityReading(Instant.parse("2024-04-26T00:00:30.00Z"), new BigDecimal(30)));
    populateReadingsForMeter(smartMeterId, data);

    ResponseEntity<ElectricityReading[]> response =
        restTemplate.getForEntity("/readings/" + smartMeterId, ElectricityReading[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(Arrays.asList(response.getBody())).isEqualTo(data);
  }

  @Test
  void shouldCalculateAllPrices() {
    String smartMeterId = "bob";
    List<ElectricityReading> data =
        List.of(
            new ElectricityReading(Instant.parse("2024-04-26T00:00:10.00Z"), new BigDecimal(10)),
            new ElectricityReading(Instant.parse("2024-04-26T00:00:20.00Z"), new BigDecimal(20)),
            new ElectricityReading(Instant.parse("2024-04-26T00:00:30.00Z"), new BigDecimal(30)));
    populateReadingsForMeter(smartMeterId, data);

    ResponseEntity<CompareAllResponse> response =
        restTemplate.getForEntity(
            "/price-plans/comparisons?smart-meter-id=" + smartMeterId, CompareAllResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
        .isEqualTo(
            new CompareAllResponse(
                Map.of("price-plan-0", 36000, "price-plan-1", 7200, "price-plan-2", 3600), null));
  }

  @SuppressWarnings("rawtypes")
  @Test
  void givenMeterIdAndLimitShouldReturnRecommendedCheapestPricePlans() {
    String smartMeterId = "jane";
    List<ElectricityReading> data =
        List.of(
            new ElectricityReading(Instant.parse("2024-04-26T00:00:10.00Z"), new BigDecimal(10)),
            new ElectricityReading(Instant.parse("2024-04-26T00:00:20.00Z"), new BigDecimal(20)),
            new ElectricityReading(Instant.parse("2024-04-26T00:00:30.00Z"), new BigDecimal(30)));
    populateReadingsForMeter(smartMeterId, data);

    ResponseEntity<Map[]> response =
        restTemplate.getForEntity(
            "/price-plans/recommendations?smart-meter-id=" + smartMeterId + "&limit=2",
            Map[].class);

    assertEquals(2, Objects.requireNonNull(response.getBody()).length);
  }

  private void populateReadingsForMeter(String smartMeterId, List<ElectricityReading> data) {
    MeterReadings readings = new MeterReadings(smartMeterId, data);

    HttpEntity<MeterReadings> entity = toHttpEntity(readings);
    restTemplate.postForEntity("/readings", entity, String.class);
  }

  record CompareAllResponse(Map<String, Integer> pricePlanComparisons, String pricePlanId) {}
}
