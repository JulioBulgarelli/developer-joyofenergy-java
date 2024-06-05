package uk.tw.energy.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Reading record for a meter
 *
 * @param time timestamp of the reading
 * @param reading value of reading in kW
 */
public record ElectricityReading(Instant time, BigDecimal reading) {}
