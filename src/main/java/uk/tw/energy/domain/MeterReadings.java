package uk.tw.energy.domain;

import java.util.List;

/**
 * Meter reading collection
 *
 * @param smartMeterId the identification of the meter
 * @param electricityReadings collection of readings for the meter
 */
public record MeterReadings(String smartMeterId, List<ElectricityReading> electricityReadings) {}
