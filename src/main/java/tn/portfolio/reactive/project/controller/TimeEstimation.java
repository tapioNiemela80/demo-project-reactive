package tn.portfolio.reactive.project.controller;

public record TimeEstimation(int hours, int minutes) {

    public TimeEstimation(int hours, int minutes){
        if (hours < 0 || minutes < 0) {
            throw new IllegalArgumentException("Negative values not allowed");
        }
        int totalMinutes = hours * 60 + minutes;
        this.hours = totalMinutes / 60;
        this.minutes = totalMinutes % 60;
    }

    public TimeEstimation add(TimeEstimation other) {
        return fromMinutes(this.toTotalMinutes() + other.toTotalMinutes());
    }

    public TimeEstimation subtract(TimeEstimation other) {
        return fromMinutes(this.toTotalMinutes() - other.toTotalMinutes());
    }

    public int toTotalMinutes() {
        return hours * 60 + minutes;
    }

    public static TimeEstimation zeroEstimation(){
        return fromMinutes(0);
    }

    public static TimeEstimation fromMinutes(int totalMinutes) {
        return new TimeEstimation(0, totalMinutes);
    }

}