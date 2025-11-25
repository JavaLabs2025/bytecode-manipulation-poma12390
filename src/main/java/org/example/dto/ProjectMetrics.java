package org.example.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;


public record ProjectMetrics(
        int classCount,
        int maxInheritanceDepth,
        double averageInheritanceDepth,
        int abcAssignmentCount,
        double abcAverageAssignmentsPerMethod,
        double averageOverriddenMethodsPerClass,
        double averageFieldCountPerClass
) {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectWriter PRETTY_WRITER = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();

    public String toJson() throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(this);

    }

    public String toPrettyJson() throws JsonProcessingException {
        return PRETTY_WRITER.writeValueAsString(this);
    }

    @Override
    public String toString() {
        try {
            return toPrettyJson();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
