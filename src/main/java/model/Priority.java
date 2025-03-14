package model;

import java.util.Objects;

public class Priority {
    private String name;

    public Priority(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Priority name cannot be null or empty.");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Priority name cannot be null or empty.");
        }
        this.name = name;
    }

    public boolean isDefault() {
        return "Default".equalsIgnoreCase(name);
    }

    @Override
    public String toString() {
        return "Priority{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Priority priority = (Priority) o;
        return Objects.equals(name.toLowerCase(), priority.name.toLowerCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase());
    }
}
