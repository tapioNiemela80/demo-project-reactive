package tn.portfolio.reactive.project.domain;

import java.util.Objects;

public final class TimeEstimation {

    private final int hours;
    private final int minutes;

    public TimeEstimation(int hours, int minutes) {
        if (hours < 0 || minutes < 0) {
            throw new IllegalArgumentException("Negative values not allowed");
        }
        int totalMinutes = hours * 60 + minutes;
        this.hours = totalMinutes / 60;
        this.minutes = totalMinutes % 60;
    }

    public static TimeEstimation zeroEstimation(){
        return new TimeEstimation(0, 0);
    }

    public TimeEstimation add(TimeEstimation other) {
        return fromMinutes(this.toTotalMinutes() + other.toTotalMinutes());
    }

    public boolean exceedsOther(TimeEstimation other){
        return toTotalMinutes() > other.toTotalMinutes();
    }

    public int toTotalMinutes() {
        return hours * 60 + minutes;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public static TimeEstimation fromMinutes(int totalMinutes) {
        return new TimeEstimation(0, totalMinutes);
    }

    @Override
    public String toString() {
        return String.format("%dh %02dm", hours, minutes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeEstimation that = (TimeEstimation) o;
        return hours == that.hours && minutes == that.minutes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hours, minutes);
    }
}