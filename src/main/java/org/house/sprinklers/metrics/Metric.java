package org.house.sprinklers.metrics;

import lombok.Data;
import org.joda.time.DateTime;

@Data
public class Metric<T> {

    private final String name;
    private final DateTime timestamp;
    private final T value;

    public Metric(String name, DateTime timestamp, T value) {
        this.name = name;
        this.timestamp = timestamp;
        this.value = value;
    }

    public Metric(String name, T value) {
        this(name, DateTime.now(), value);
    }
}
