package uk.co.informaticslab;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.retry.PredefinedRetryPolicies;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.codahale.metrics.MetricRegistry;

public final class Constants {

    public static final MetricRegistry METRICS = new MetricRegistry();

    public static final int MEGABYTE = 1024 * 1024;

    public static final Regions MY_S3_DATA_REGION = Regions.EU_WEST_2;

    public static AmazonS3 getS3Client() {
        ClientConfiguration config = new ClientConfiguration();
        config.setMaxConnections(128);
        config.setMaxErrorRetry(16);
        config.setConnectionTimeout(100000);
        config.setSocketTimeout(100000);
        config.setRetryPolicy(PredefinedRetryPolicies.getDefaultRetryPolicyWithCustomMaxRetries(32));
        return AmazonS3ClientBuilder.standard().withRegion(MY_S3_DATA_REGION).withClientConfiguration(config).build();
    }

    private Constants () {
    }
}
