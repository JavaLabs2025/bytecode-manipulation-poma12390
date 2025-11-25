package org.example;

import org.example.calculator.JarMetricsCalculator;
import org.example.dto.ProjectMetrics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MetricsApp {

    private static final String INPUT_JAR_PATH = "src/main/resources/guava.jar";

    private static final String OUTPUT_JSON_PATH = "results/metrics.json";

    private MetricsApp() {
    }

    public static void main(String[] args) throws IOException {
        Path jarPath = Path.of(INPUT_JAR_PATH);
        Path outputPath = Path.of(OUTPUT_JSON_PATH);

        JarMetricsCalculator calculator = new JarMetricsCalculator(jarPath);
        ProjectMetrics metrics = calculator.analyze();

        System.out.println(metrics.toPrettyJson());

        String json = metrics.toJson();
        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }
        Files.writeString(outputPath, json, StandardCharsets.UTF_8);

    }
}
