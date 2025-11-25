package org.example.dto;

public final class MethodMetrics {

    private final String name;
    private final String descriptor;

    private int abcAssignments;
    private int abcBranches;
    private int abcConditions;

    public MethodMetrics(String name, String descriptor) {
        this.name = name;
        this.descriptor = descriptor;
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String getSignature() {
        return name + descriptor;
    }

    public int getAbcAssignments() {
        return abcAssignments;
    }

    public int getAbcBranches() {
        return abcBranches;
    }

    public int getAbcConditions() {
        return abcConditions;
    }

    public void incrementAbcAssignments() {
        abcAssignments++;
    }

    public void incrementAbcBranches() {
        abcBranches++;
    }

    public void incrementAbcConditions() {
        abcConditions++;
    }

    public void addAbcConditions(int delta) {
        abcConditions += delta;
    }
}
