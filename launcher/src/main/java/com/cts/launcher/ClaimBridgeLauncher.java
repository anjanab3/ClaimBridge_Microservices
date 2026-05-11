package com.cts.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClaimBridgeLauncher {

    private static final List<Process> runningProcesses = new ArrayList<>();

    record ServiceConfig(String name, String relativeJarPath, int port, int maxWaitSeconds, String healthPath) {}

    public static void main(String[] args) throws Exception {
        String projectRoot = resolveProjectRoot();
        System.out.println("=================================================");
        System.out.println("  ClaimBridge Microservices Launcher");
        System.out.println("  Project root: " + projectRoot);
        System.out.println("=================================================\n");

        List<ServiceConfig> services = List.of(
            new ServiceConfig("eureka-server",    "eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar",          8761, 60, "/"),
            new ServiceConfig("policy-service",   "policy-service/target/claimbridge_policy-0.0.1-SNAPSHOT.jar",    9091, 90, "/v3/api-docs"),
            new ServiceConfig("claims-service",   "claims-service/target/claimbridge_claims-0.0.1-SNAPSHOT.jar",    9092, 90, "/v3/api-docs"),
            new ServiceConfig("identity-service", "identity-service/target/identity-service-0.0.1-SNAPSHOT.jar",   9093, 90, "/v3/api-docs"),
            new ServiceConfig("payment-service",  "payment-service/target/payment-service-0.0.1-SNAPSHOT.jar",     9094, 90, "/v3/api-docs"),
            new ServiceConfig("reporting-service","reporting-service/target/reporting-service-0.0.1-SNAPSHOT.jar", 9095, 90, "/actuator/health")
        );

        // Verify all JARs exist before starting anything
        System.out.println("Verifying JARs...");
        for (ServiceConfig svc : services) {
            File jar = new File(projectRoot, svc.relativeJarPath());
            if (!jar.exists()) {
                System.err.println("  [MISSING] " + jar.getAbsolutePath());
                System.err.println("\nOne or more JARs are missing. Build with:");
                System.err.println("  .\\mvnw clean package -DskipTests");
                System.exit(1);
            }
            System.out.println("  [OK] " + svc.name());
        }
        System.out.println();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n\nShutting down all services...");
            runningProcesses.forEach(p -> {
                p.descendants().forEach(ProcessHandle::destroy);
                p.destroy();
            });
            System.out.println("All services stopped.");
        }));

        // Start each service sequentially, waiting for HTTP readiness before the next
        Map<String, String> startupStatus = new LinkedHashMap<>();
        for (ServiceConfig svc : services) {
            String status = startService(svc, projectRoot);
            startupStatus.put(svc.name(), status);
        }

        // Wait a moment for Eureka registrations to propagate
        System.out.println("\nWaiting 5s for Eureka registrations to propagate...");
        Thread.sleep(5000);

        // Print startup summary
        printStartupSummary(startupStatus);

        // Check Eureka registry to confirm which services registered
        checkEurekaRegistry();

        System.out.println("\n  Press Ctrl+C to stop all services.");

        Thread.currentThread().join();
    }

    private static String startService(ServiceConfig svc, String projectRoot) throws Exception {
        File jar = new File(projectRoot, svc.relativeJarPath());
        File logFile = new File(jar.getParentFile(), svc.name() + ".log");

        System.out.println("[" + svc.name() + "] Starting... (log: " + logFile.getName() + ")");
        ProcessBuilder pb = new ProcessBuilder("java", "-jar", jar.getAbsolutePath());
        pb.redirectOutput(logFile);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        runningProcesses.add(process);

        // Phase 1: wait for TCP port to open
        boolean portOpen = waitForPort(svc.port(), svc.maxWaitSeconds());
        if (!portOpen) {
            System.out.println("[" + svc.name() + "] WARN: port " + svc.port() + " never opened. Check: " + logFile.getAbsolutePath());
            return "PORT_TIMEOUT";
        }

        // Phase 2: wait for HTTP endpoint to respond (service fully initialized)
        System.out.println("[" + svc.name() + "] Port open. Waiting for HTTP readiness...");
        boolean httpReady = waitForHttp(svc.port(), svc.healthPath, svc.maxWaitSeconds());
        if (httpReady) {
            System.out.println("[" + svc.name() + "] UP  ->  http://localhost:" + svc.port());
            return "UP";
        } else {
            System.out.println("[" + svc.name() + "] WARN: HTTP not ready after " + svc.maxWaitSeconds() + "s. Check: " + logFile.getAbsolutePath());
            return "HTTP_TIMEOUT";
        }
    }

    private static boolean waitForPort(int port, int maxSeconds) throws InterruptedException {
        long deadline = System.currentTimeMillis() + maxSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            try (Socket socket = new Socket("localhost", port)) {
                return true;
            } catch (Exception ignored) {
                Thread.sleep(2000);
            }
        }
        return false;
    }

    private static boolean waitForHttp(int port, String path, int maxSeconds) throws InterruptedException {
        long deadline = System.currentTimeMillis() + maxSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            try {
                HttpURLConnection conn = (HttpURLConnection)
                        new URL("http://localhost:" + port + path).openConnection();
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);
                conn.setRequestMethod("GET");
                int status = conn.getResponseCode();
                if (status >= 200 && status < 400) return true;
            } catch (Exception ignored) {}
            Thread.sleep(3000);
        }
        return false;
    }

    private static void checkEurekaRegistry() {
        System.out.println("\n--- Eureka Service Registry ---");
        try {
            HttpURLConnection conn = (HttpURLConnection)
                    new URL("http://localhost:8761/eureka/apps").openConnection();
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            if (conn.getResponseCode() != 200) {
                System.out.println("  Could not reach Eureka registry (HTTP " + conn.getResponseCode() + ")");
                return;
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
            }

            String body = response.toString();
            // Parse app names from JSON: "name":"SERVICE-NAME"
            List<String> registered = new ArrayList<>();
            int idx = 0;
            while ((idx = body.indexOf("\"name\":\"", idx)) != -1) {
                int start = idx + 8;
                int end = body.indexOf("\"", start);
                if (end > start) {
                    String appName = body.substring(start, end);
                    // Skip duplicate entries (Eureka returns name at multiple levels)
                    if (!registered.contains(appName) && !appName.equals("apps")) {
                        registered.add(appName);
                    }
                }
                idx = end + 1;
            }

            if (registered.isEmpty()) {
                System.out.println("  No services registered yet (they may still be connecting).");
                System.out.println("  Check the Eureka dashboard: http://localhost:8761");
            } else {
                System.out.println("  Registered services (" + registered.size() + "):");
                registered.forEach(name -> System.out.println("    [REGISTERED] " + name));
            }

        } catch (Exception e) {
            System.out.println("  Could not query Eureka: " + e.getMessage());
        }
    }

    private static void printStartupSummary(Map<String, String> statuses) {
        System.out.println("\n=================================================");
        System.out.println("  STARTUP SUMMARY");
        System.out.println("=================================================");
        System.out.printf("  %-20s %-10s %s%n", "Service", "Status", "URL");
        System.out.println("  " + "-".repeat(60));

        Map<String, Integer> ports = Map.of(
            "eureka-server", 8761,
            "policy-service", 9091,
            "claims-service", 9092,
            "identity-service", 9093,
            "payment-service", 9094,
            "reporting-service", 9095
        );

        statuses.forEach((name, status) -> {
            String icon = status.equals("UP") ? "[UP]   " : "[WARN] ";
            System.out.printf("  %-20s %-10s http://localhost:%d%n",
                    icon + name, status, ports.getOrDefault(name, 0));
        });

        System.out.println("\n  Swagger UI (API docs):");
        ports.entrySet().stream()
            .filter(e -> !e.getKey().equals("eureka-server"))
            .forEach(e -> System.out.printf("    %-20s http://localhost:%d/swagger-ui.html%n",
                    e.getKey(), e.getValue()));
        System.out.println("  Eureka Dashboard      : http://localhost:8761");
        System.out.println("=================================================");
    }

    private static String resolveProjectRoot() {
        try {
            // Use Paths.get(URI) directly — avoids the leading-slash bug on Windows
            // where toURI().getPath() returns "/C:/..." which Paths.get(String) misreads
            Path jarPath = Paths.get(
                    ClaimBridgeLauncher.class.getProtectionDomain()
                            .getCodeSource().getLocation().toURI()
            ).toAbsolutePath();
            // jar is at: <root>/launcher/target/claimbridge-launcher.jar
            // go up 3 levels → project root
            return jarPath.getParent().getParent().getParent().toString();
        } catch (Exception e) {
            return System.getProperty("user.dir");
        }
    }
}
