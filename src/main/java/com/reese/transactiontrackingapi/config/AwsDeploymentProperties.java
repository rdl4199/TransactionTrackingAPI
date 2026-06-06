package com.reese.transactiontrackingapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.aws")
public record AwsDeploymentProperties(
    String region,
    String s3Bucket,
    String rdsHost,
    Integer rdsPort,
    String rdsDatabase,
    String secretsManagerSecretName,
    String loadBalancerDns
) {

    public boolean hasRegion() {
        return hasText(region);
    }

    public boolean hasS3Bucket() {
        return hasText(s3Bucket);
    }

    public boolean hasRdsConfiguration() {
        return hasText(rdsHost) && rdsPort != null && hasText(rdsDatabase);
    }

    public boolean hasSecretsManagerConfiguration() {
        return hasText(secretsManagerSecretName);
    }

    public boolean hasLoadBalancerConfiguration() {
        return hasText(loadBalancerDns);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
