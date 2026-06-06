# AWS Deployment Notes

This project now includes an `aws` Spring profile and environment-variable hooks to make later deployment easier.

## What is prepared

- `application-aws.properties` configures the app for PostgreSQL-backed AWS deployment.
- `.env.aws.example` shows the environment variables you can fill in later.
- `/infrastructure/aws` reports whether the app has enough AWS-related configuration to deploy cleanly.
- `.github/workflows/ci.yml` runs automated test, package, and Docker build checks on pushes and pull requests.
- `.github/workflows/deploy-aws.yml` is a GitHub Actions deployment scaffold for ECR + ECS.
- `docker-compose.yml` is now a simple single-container local Docker setup.
- `docker-compose.scaled.yml` contains the heavier multi-instance Postgres + load balancer stack for later use.

## Suggested AWS architecture

- Compute: ECS Fargate, Elastic Beanstalk, or EC2
- Load balancing: Application Load Balancer
- Database: Amazon RDS for PostgreSQL
- File storage: Amazon S3 for imported CSV archival
- Secrets: AWS Secrets Manager for database credentials and other sensitive values

## Local test command with AWS profile

```powershell
$env:SPRING_PROFILES_ACTIVE='aws'
$env:APP_AWS_REGION='us-east-1'
.\mvnw.cmd spring-boot:run
```

## Recommended next steps later

1. Replace placeholder values in `.env.aws.example`.
2. Move secrets into AWS Secrets Manager or Parameter Store.
3. Provision RDS and S3 before enabling multi-instance production deployment.
4. Point your AWS load balancer to the deployed app target group.
5. Add these GitHub repository secrets before enabling automated AWS deployment:

   - `AWS_ROLE_TO_ASSUME`
   - `AWS_REGION`
   - `ECR_REPOSITORY`
   - `ECS_CLUSTER`
   - `ECS_SERVICE`
