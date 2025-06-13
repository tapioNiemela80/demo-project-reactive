package tn.portfolio.reactive.project.view;

public record TimeEstimate(int hours, int minutes) {

    public TimeEstimate(int hours, int minutes) {
        if (hours < 0 || minutes < 0) {
            throw new IllegalArgumentException("Negative values not allowed");
        }
        int totalMinutes = hours * 60 + minutes;
        this.hours = totalMinutes / 60;
        this.minutes = totalMinutes % 60;
    }

    public TimeEstimate add(TimeEstimate other) {
        return fromMinutes(this.toTotalMinutes() + other.toTotalMinutes());
    }

    public TimeEstimate subtract(TimeEstimate other) {
        return fromMinutes(this.toTotalMinutes() - other.toTotalMinutes());
    }

    public int toTotalMinutes() {
        return hours * 60 + minutes;
    }

    public static TimeEstimate zeroEstimation() {
        return fromMinutes(0);
    }

    public static TimeEstimate fromMinutes(int totalMinutes) {
        return new TimeEstimate(0, totalMinutes);
    }
}