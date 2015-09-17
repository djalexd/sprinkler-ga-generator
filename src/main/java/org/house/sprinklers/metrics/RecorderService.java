package org.house.sprinklers.metrics;

import java.util.List;
import java.util.Set;

/**
 * Similar to {@link org.springframework.boot.actuate.metrics.CounterService} and/or
 * {@link org.springframework.boot.actuate.metrics.GaugeService}, but it allows
 * any kind of {@literal T} object to be stored.
 */
public interface RecorderService {
    /**
     * Submits a new data value for given metric
     */
    <T> void submit(String metricName, T data);

    /**
     * Retrieves all values for given metric
     */
    <T> List<T> getMetricValues(String metricName);

    /**
     * Retrieves value for given metric or provides default value.
     */
    <T extends Number> T getMetricValue(String metricName, T zero);

    /**
     * Retrieves numeric value metrics for given prefix.
     */
    <T extends Number> Set<Metric<T>> filterMetrics(String metricPrefix);

    /**
     * Increments a metric by 1
     */
    void increment(String metricName);

    /**
     * Increments a metric by given count
     */
    void increment(String metricName, long count);

    /**
     * Resets a given metric (can be counter or histogram).
     */
    void reset(String metricName);
}
