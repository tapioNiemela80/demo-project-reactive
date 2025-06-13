package tn.portfolio.reactive.common.domain;

import java.util.Objects;

@ValueObject
public final class ActualSpentTime {

    private final int hours;
    private final int minutes;

    public ActualSpentTime(int hours, int minutes) {
        if (hours < 0 || minutes < 0) {
            throw new IllegalArgumentException("Negative values not allowed");
        }
        int totalMinutes = hours * 60 + minutes;
        this.hours = totalMinutes / 60;
        this.minutes = totalMinutes % 60;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public static ActualSpentTime fromMinutes(int totalMinutes) {
        return new ActualSpentTime(0, totalMinutes);
    }

    @Override
    public String toString() {
        return String.format("%dh %02dm", hours, minutes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActualSpentTime that = (ActualSpentTime) o;
        return hours == that.hours && minutes == that.minutes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hours, minutes);
    }
}