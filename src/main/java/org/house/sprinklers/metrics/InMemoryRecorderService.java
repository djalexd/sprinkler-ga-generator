package org.house.sprinklers.metrics;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRecorderService implements RecorderService {

    private enum DataType {
        Numeric, MultipleValues
    }

    private final Map<String, Object> dataMap = new ConcurrentHashMap<>(128);
    private final Map<String, DataType> dataTypeMap = new ConcurrentHashMap<>(128);

    @SuppressWarnings("unchecked")
    @Override
    public <T> void submit(String metricName, T data) {
        ensureMultipleValuesMetric(metricName);

        if (!dataMap.containsKey(metricName)) {
            dataMap.put(metricName, Lists.newArrayList(data));
            dataTypeMap.put(metricName, DataType.MultipleValues);
        } else {
            final List<T> dataList = (List<T>) dataMap.get(metricName);
            dataList.add(data);
        }
    }

    private void ensureMultipleValuesMetric(String metricName) {
        if (dataMap.containsKey(metricName) && dataTypeMap.get(metricName) == DataType.Numeric) {
            throw new IllegalArgumentException("Cannot add value to numeric metric: " + metricName);
        }
    }

    private void ensureNumericMetric(String metricName) {
        if (dataMap.containsKey(metricName) && dataTypeMap.get(metricName) == DataType.MultipleValues) {
            throw new IllegalArgumentException("Cannot get/set numeric metric for MultipleValues of: " + metricName);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> getMetricValues(String metricName) {
        ensureMultipleValuesMetric(metricName);
        return (List<T>) dataMap.get(metricName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Number> T getMetricValue(String metricName, T zero) {
        if (!dataMap.containsKey(metricName)) {
            return zero;
        } else {
            if (dataTypeMap.get(metricName) == DataType.MultipleValues) {
                throw new IllegalArgumentException("Cannot retrieve numeric metric: " + metricName);
            }
            return (T) dataMap.get(metricName);
        }
    }

    @Override
    public void increment(String metricName) {
        increment(metricName, 1);
    }

    @Override
    public void increment(String metricName, long count) {
        ensureNumericMetric(metricName);
        if (dataMap.containsKey(metricName)) {
            dataMap.put(metricName, (Long) dataMap.get(metricName) + count);
        } else {
            dataMap.put(metricName, count);
        }
    }

    @Override
    public void reset(String metricName) {
        dataMap.remove(metricName);
        dataTypeMap.remove(metricName);
    }
}
