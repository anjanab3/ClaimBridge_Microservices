#!/bin/bash
echo "Starting ClaimBridge services..."

java -jar eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar &

echo "Waiting 30s for Eureka..."
sleep 30

java -jar api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar &
java -jar policy-service/target/claimbridge_policy-0.0.1-SNAPSHOT.jar &
java -jar claims-service/target/claimbridge_claims-0.0.1-SNAPSHOT.jar &
java -jar identity-service/target/identity-service-0.0.1-SNAPSHOT.jar &
java -jar payment-service/target/payment-service-0.0.1-SNAPSHOT.jar &
java -jar reporting-service/target/reporting-service-0.0.1-SNAPSHOT.jar &

echo "All services started. Give them ~30 seconds to fully load."
echo "API Gateway -> http://localhost:8085"
echo "Eureka      -> http://localhost:8761"

wait
