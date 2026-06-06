package com.reese.expensetracker;

import com.reese.transactiontrackingapi.config.AwsDeploymentProperties;
import com.reese.transactiontrackingapi.controller.InfrastructureController;
import com.reese.transactiontrackingapi.dto.AwsDeploymentStatusResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfrastructureControllerTests {

    @Test
    void shouldReportConfiguredAwsDependencies() {
        AwsDeploymentProperties properties = new AwsDeploymentProperties(
            "us-east-1",
            "transaction-imports",
            "db.example.amazonaws.com",
            5432,
            "transactionsdb",
            "transaction-tracker/prod",
            "tracker-alb.amazonaws.com"
        );

        InfrastructureController controller = new InfrastructureController(properties);
        AwsDeploymentStatusResponse response = controller.getAwsDeploymentStatus();

        assertEquals("us-east-1", response.region());
        assertTrue(response.rdsConfigured());
        assertTrue(response.s3Configured());
        assertTrue(response.secretsManagerConfigured());
        assertTrue(response.loadBalancerConfigured());
    }

    @Test
    void shouldReportMissingAwsDependenciesWhenUnset() {
        AwsDeploymentProperties properties = new AwsDeploymentProperties(
            "",
            "",
            "",
            null,
            "",
            "",
            ""
        );

        InfrastructureController controller = new InfrastructureController(properties);
        AwsDeploymentStatusResponse response = controller.getAwsDeploymentStatus();

        assertEquals("", response.region());
        assertFalse(response.rdsConfigured());
        assertFalse(response.s3Configured());
        assertFalse(response.secretsManagerConfigured());
        assertFalse(response.loadBalancerConfigured());
    }
}
