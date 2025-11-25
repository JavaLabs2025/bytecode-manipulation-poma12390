package org.example.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ClassMetrics {

    private String name;
    private String superName;
    private final List<String> interfaces = new ArrayList<>();

    private int fieldCount;
    private final List<MethodMetrics> methods = new ArrayList<>();
    private final Set<String> methodSignatures = new HashSet<>();

    private int overriddenMethodsCount;
    private int inheritanceDepth;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSuperName() {
        return superName;
    }

    public void setSuperName(String superName) {
        this.superName = superName;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public int getFieldCount() {
        return fieldCount;
    }

    public void incrementFieldCount() {
        fieldCount++;
    }

    public List<MethodMetrics> getMethods() {
        return methods;
    }

    public void addMethod(MethodMetrics methodMetrics) {
        methods.add(methodMetrics);
        methodSignatures.add(methodMetrics.getSignature());
    }

    public Set<String> getMethodSignatures() {
        return methodSignatures;
    }

    public int getOverriddenMethodsCount() {
        return overriddenMethodsCount;
    }

    public void setOverriddenMethodsCount(int overriddenMethodsCount) {
        this.overriddenMethodsCount = overriddenMethodsCount;
    }

    public int getInheritanceDepth() {
        return inheritanceDepth;
    }

    public void setInheritanceDepth(int inheritanceDepth) {
        this.inheritanceDepth = inheritanceDepth;
    }
}
