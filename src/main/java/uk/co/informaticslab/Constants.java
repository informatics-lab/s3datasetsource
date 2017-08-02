package uk.co.informaticslab;

import com.codahale.metrics.MetricRegistry;

public final class Constants {

    public static final MetricRegistry METRICS = new MetricRegistry();

    public static final int MEGABYTE = 1024 * 1024;

    private Constants () {
    }
}
