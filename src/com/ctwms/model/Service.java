package com.ctwms.model;

/**
 * Represents a campus service entry maintained in an ArrayList catalog.
 */
public class Service implements Cloneable {
    private String name;
    private String description;
    private String category;
    private boolean active;

    public Service(String name, String description, String category, boolean active) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getStatusLabel() {
        return active ? "ACTIVE" : "INACTIVE";
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s", name, category, description, getStatusLabel());
    }

    @Override
    public Service clone() {
        return new Service(name, description, category, active);
    }
}
