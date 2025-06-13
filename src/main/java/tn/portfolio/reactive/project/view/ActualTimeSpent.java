package tn.portfolio.reactive.project.view;
public record ActualTimeSpent(int hours, int minutes) {
    public ActualTimeSpent {
        if (hours < 0 || minutes < 0) {
            throw new IllegalArgumentException("Negative values not allowed");
        }
        int totalMinutes = hours * 60 + minutes;
        hours = totalMinutes / 60;
        minutes = totalMinutes % 60;
    }
    public static ActualTimeSpent zero() {
        return new ActualTimeSpent(0, 0);
    }
    public ActualTimeSpent add(ActualTimeSpent other){
        return fromMinutes(toTotalMinutes()+other.toTotalMinutes());
    }
    public int toTotalMinutes() {
        return hours * 60 + minutes;
    }
    private static ActualTimeSpent fromMinutes(int totalMinutes) {
        return new ActualTimeSpent(0, totalMinutes);
    }
}
