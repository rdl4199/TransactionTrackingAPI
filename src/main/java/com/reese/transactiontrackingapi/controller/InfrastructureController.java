package com.reese.transactiontrackingapi.controller;

import com.reese.transactiontrackingapi.config.AwsDeploymentProperties;
import com.reese.transactiontrackingapi.dto.AwsDeploymentStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfrastructureController {

    private final AwsDeploymentProperties awsDeploymentProperties;

    public InfrastructureController(AwsDeploymentProperties awsDeploymentProperties) {
        this.awsDeploymentProperties = awsDeploymentProperties;
    }

    @GetMapping("/infrastructure/aws")
    public AwsDeploymentStatusResponse getAwsDeploymentStatus() {
        return new AwsDeploymentStatusResponse(
            awsDeploymentProperties.region(),
            awsDeploymentProperties.hasRdsConfiguration(),
            awsDeploymentProperties.hasS3Bucket(),
            awsDeploymentProperties.hasSecretsManagerConfiguration(),
            awsDeploymentProperties.hasLoadBalancerConfiguration(),
            "Elastic Beanstalk, ECS Fargate, or EC2 behind an Application Load Balancer",
            "Set the aws profile and environment variables before deployment. RDS is recommended for shared transaction data, and S3 is a good fit for imported CSV archival."
        );
    }
}
