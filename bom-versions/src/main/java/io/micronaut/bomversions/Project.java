package io.micronaut.bomversions;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class Project {
    private final String name;
    private final String version;

    public Project(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "Project{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
