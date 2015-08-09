package org.house.sprinklers;

public final class MathUtils {

    private MathUtils() {
        throw new Error("Do not instantiate!");
    }

    //without decimal digits
    public static String toPercentage(float n){
        return String.format("%.0f",n*100)+"%";
    }

    //accept a param to determine the numbers of decimal digits
    public static String toPercentage(float n, int digits){
        return String.format("%."+digits+"f",n*100)+"%";
    }
}
