package uk.tw.energy.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.tw.energy.builders.MeterReadingsBuilder;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.MeterReadings;
import uk.tw.energy.service.MeterReadingService;

class MeterReadingControllerTest {

    private static final String SMART_METER_ID = "10101010";
    private MeterReadingController meterReadingController;
    private MeterReadingService meterReadingService;

    @BeforeEach
    public void setUp() {
        this.meterReadingService = new MeterReadingService(new HashMap<>());
        this.meterReadingController = new MeterReadingController(meterReadingService);
    }

    @Test
    void givenNoMeterIdIsSuppliedWhenStoringShouldReturnErrorResponse() {
        MeterReadings meterReadings = new MeterReadings(null, Collections.emptyList());
        assertEquals(
                meterReadingController
                        .storeReadings(meterReadings)
                        .getStatusCode()
                        .value(),
                HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void givenEmptyMeterReadingShouldReturnErrorResponse() {
        MeterReadings meterReadings = new MeterReadings(SMART_METER_ID, Collections.emptyList());
        assertEquals(
                meterReadingController
                        .storeReadings(meterReadings)
                        .getStatusCode()
                        .value(),
                HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void givenNullReadingsAreSuppliedWhenStoringShouldReturnErrorResponse() {
        MeterReadings meterReadings = new MeterReadings(SMART_METER_ID, null);
        assertEquals(
                meterReadingController
                        .storeReadings(meterReadings)
                        .getStatusCode()
                        .value(),
                HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void givenMultipleBatchesOfMeterReadingsShouldStore() {
        MeterReadings meterReadings = new MeterReadingsBuilder()
                .setSmartMeterId(SMART_METER_ID)
                .generateElectricityReadings()
                .build();

        MeterReadings otherMeterReadings = new MeterReadingsBuilder()
                .setSmartMeterId(SMART_METER_ID)
                .generateElectricityReadings()
                .build();

        meterReadingController.storeReadings(meterReadings);
        meterReadingController.storeReadings(otherMeterReadings);

        List<ElectricityReading> expectedElectricityReadings = new ArrayList<>();
        expectedElectricityReadings.addAll(meterReadings.electricityReadings());
        expectedElectricityReadings.addAll(otherMeterReadings.electricityReadings());

        List<ElectricityReading> storedElectricityReadings =
                meterReadingService.getReadings(SMART_METER_ID, 0, 10).orElseThrow();

        assertEquals(expectedElectricityReadings.size(), storedElectricityReadings.size());
        //
        // assertTrue(containsInAnyOrder(List.copyOf(storedElectricityReadings)).matches(expectedElectricityReadings));
    }

    @Test
    void givenMeterReadingsAssociatedWithTheUserShouldStoreAssociatedWithUser() {
        MeterReadings meterReadings = new MeterReadingsBuilder()
                .setSmartMeterId(SMART_METER_ID)
                .generateElectricityReadings()
                .build();

        MeterReadings otherMeterReadings = new MeterReadingsBuilder()
                .setSmartMeterId("00001")
                .generateElectricityReadings()
                .build();

        meterReadingController.storeReadings(meterReadings);
        meterReadingController.storeReadings(otherMeterReadings);

        List<ElectricityReading> readings =
                meterReadingService.getReadings(SMART_METER_ID, 0, 10).orElseThrow();

        assertThat("", meterReadings.electricityReadings(), containsInAnyOrder(readings.toArray()));
    }

    @Test
    void givenMeterIdThatIsNotRecognisedShouldReturnNotFound() {
        assertEquals(
                meterReadingController
                        .readReadings(SMART_METER_ID, 0, 10)
                        .getStatusCode()
                        .value(),
                HttpStatus.NOT_FOUND.value());
    }
}
