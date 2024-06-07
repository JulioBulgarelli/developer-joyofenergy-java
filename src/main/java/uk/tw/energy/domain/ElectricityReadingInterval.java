package uk.tw.energy.domain;

import java.time.*;
import java.time.temporal.ChronoUnit;

public class ElectricityReadingInterval {
    private final Instant start;
    private final Instant end;

    final static int A_DAY_IN_SECONDS = 60 * 60 * 24;
    final static int A_WEEK_BEFORE_IN_SECONDS = A_DAY_IN_SECONDS * 7;

    private ElectricityReadingInterval(Instant start, Instant end) {
        this.start = start;
        this.end = end;
    }

    public Instant getStart() {
        return start;
    }

    public Instant getEnd() {
        return end;
    }

    public static ElectricityReadingInterval previousWeekInterval() {
        return previousWeekInterval(Clock.systemUTC());
    }

    public static ElectricityReadingInterval previousWeekInterval(Clock clock) {
        Instant now = Instant.now(clock);

        Instant sunday = ElectricityReadingInterval.getPreviousSundayOf(now);

        return new ElectricityReadingInterval(sunday.minusSeconds(A_WEEK_BEFORE_IN_SECONDS), sunday);
    }

    private static Instant getPreviousSundayOf(Instant instant) {
        Instant sunday = instant.truncatedTo(ChronoUnit.DAYS);

        while (!DayOfWeek.from(sunday.atZone(ZoneOffset.UTC)).equals(DayOfWeek.SUNDAY)) {
            sunday = sunday.minusSeconds(A_DAY_IN_SECONDS);
        }

        return sunday;
    }
}
