package uk.tw.energy.domain;

import java.math.BigDecimal;
import java.time.DayOfWeek;

/**
 * Peak Time Multiplier class for aiding pricing calculations
 *
 * @param dayOfWeek the day of the week for the peak time
 * @param multiplier the value to by applied as multiplier in the calculation
 */
public record PeakTimeMultiplier(DayOfWeek dayOfWeek, BigDecimal multiplier) {}
