package tn.portfolio.reactive.project.controller;

public record TimeEstimation(int hours, int minutes) {

    public TimeEstimation {
        if (hours < 0 || minutes < 0) {
            throw new IllegalArgumentException("Negative values not allowed");
        }
    }
}