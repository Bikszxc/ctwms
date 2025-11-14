package com.ctwms.model;

/**
 * Represents an individual registered in the campus directory.
 */
public class Personnel implements Cloneable {
    private final String id;
    private String name;
    private String role;
    private String department;
    private String email;

    public Personnel(String id, String name, String role, String department, String email) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.department = department;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s | %s | %s", name, id, role, department, email);
    }

    @Override
    public Personnel clone() {
        return new Personnel(id, name, role, department, email);
    }
}
