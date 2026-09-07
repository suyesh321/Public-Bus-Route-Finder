package service;

/**
 * Small helper used by the GUI to turn raw distance/fare numbers coming
 * back from RouteFinderService into a display-friendly summary, and to
 * estimate a fare when a route's exact segment fare isn't set.
 */
public class FareCalculatorService {

    /** Fallback rate (NPR per km) used when a route has no explicit fare table. */
    private static final double DEFAULT_RATE_PER_KM = 5.0;

    /** Minimum fare charged by most city bus/microbus/Sajha services. */
    private static final double MINIMUM_FARE = 15.0;

    public double estimateFare(double distanceKm, double ratePerKm) {
        double rate = ratePerKm > 0 ? ratePerKm : DEFAULT_RATE_PER_KM;
        double fare = distanceKm * rate;
        return Math.max(roundToNearestRupee(fare), MINIMUM_FARE);
    }

    public String formatFare(double fareNpr) {
        return String.format("NPR %.0f", fareNpr);
    }

    public String formatDistance(double distanceKm) {
        return String.format("%.1f km", distanceKm);
    }

    private double roundToNearestRupee(double amount) {
        return Math.round(amount);
    }
}
