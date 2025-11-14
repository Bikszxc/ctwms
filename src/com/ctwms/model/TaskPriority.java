package com.ctwms.model;

/**
 * Enumerates the priority levels for queued campus tasks.
 */
public enum TaskPriority {
    HIGH,
    MEDIUM,
    LOW;

    public static TaskPriority fromInput(String input) {
        for (TaskPriority priority : values()) {
            if (priority.name().equalsIgnoreCase(input)) {
                return priority;
            }
        }
        return LOW;
    }
}
