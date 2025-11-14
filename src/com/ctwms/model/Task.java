package com.ctwms.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a queued task request within the CTWMS.
 */
public class Task implements Cloneable {
    private final String taskId;
    private final String requestor;
    private final String description;
    private final TaskPriority priority;
    private final LocalDateTime createdAt;

    public Task(String taskId, String requestor, String description, TaskPriority priority) {
        this.taskId = taskId;
        this.requestor = requestor;
        this.description = description;
        this.priority = priority;
        this.createdAt = LocalDateTime.now();
    }

    private Task(String taskId, String requestor, String description, TaskPriority priority, LocalDateTime createdAt) {
        this.taskId = taskId;
        this.requestor = requestor;
        this.description = description;
        this.priority = priority;
        this.createdAt = createdAt;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getRequestor() {
        return requestor;
    }

    public String getDescription() {
        return description;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | %s | %s", taskId, requestor, description, priority,
                createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
    }

    @Override
    public Task clone() {
        return new Task(taskId, requestor, description, priority, createdAt);
    }
}
