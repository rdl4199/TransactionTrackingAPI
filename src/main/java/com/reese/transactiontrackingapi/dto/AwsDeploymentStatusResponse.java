package com.reese.transactiontrackingapi.dto;

public record AwsDeploymentStatusResponse(
    String region,
    boolean rdsConfigured,
    boolean s3Configured,
    boolean secretsManagerConfigured,
    boolean loadBalancerConfigured,
    String recommendedPlatform,
    String notes
) {
}
