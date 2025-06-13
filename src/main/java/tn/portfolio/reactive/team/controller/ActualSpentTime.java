package tn.portfolio.reactive.team.controller;

public record ActualSpentTime(int hours, int minutes) {
    public ActualSpentTime {
        if (hours < 0 || minutes < 0) {
            throw new IllegalArgumentException("Negative values not allowed");
        }
    }
}
