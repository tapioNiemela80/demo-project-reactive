package tn.portfolio.reactive.team.controller;

public record ActualSpentTime(int hours, int minutes) {
    public static ActualSpentTime zero() {
        return new ActualSpentTime(0,0);
    }

    public ActualSpentTime add(ActualSpentTime other){
        return new ActualSpentTime(hours + other.hours(), minutes + other.minutes());
    }

}
