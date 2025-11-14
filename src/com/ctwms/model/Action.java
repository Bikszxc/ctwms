package com.ctwms.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Captures the information required to undo an operation.
 */
public class Action {
    private final ActionType type;
    private final Personnel personnelSnapshot;
    private final Service serviceBefore;
    private final Service serviceAfter;
    private final Task taskSnapshot;
    private final int positionIndex;
    private final List<Personnel> personnelOrderSnapshot;
    private final String description;
    private final String timestamp;

    private Action(ActionType type,
                   Personnel personnelSnapshot,
                   Service serviceBefore,
                   Service serviceAfter,
                   Task taskSnapshot,
                   int positionIndex,
                   String description,
                   List<Personnel> personnelOrderSnapshot) {
        this.type = type;
        this.personnelSnapshot = personnelSnapshot;
        this.serviceBefore = serviceBefore;
        this.serviceAfter = serviceAfter;
        this.taskSnapshot = taskSnapshot;
        this.positionIndex = positionIndex;
        this.personnelOrderSnapshot = personnelOrderSnapshot;
        this.description = description;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static Action personnelAction(ActionType type, Personnel personnel, int positionIndex, String description) {
        return new Action(type, personnel != null ? personnel.clone() : null, null, null,
                null, positionIndex, description, null);
    }

    public static Action personnelOrderAction(List<Personnel> personnelOrderSnapshot, String description) {
        return new Action(ActionType.SORT_PERSONNEL,
                null,
                null,
                null,
                null,
                -1,
                description,
                clonePersonnelList(personnelOrderSnapshot));
    }

    public static Action taskAction(ActionType type, Task task, String description) {
        return new Action(type, null, null, null, task != null ? task.clone() : null, -1, description, null);
    }

    public static Action serviceAction(ActionType type, Service before, Service after, int positionIndex, String description) {
        return new Action(type,
                null,
                before != null ? before.clone() : null,
                after != null ? after.clone() : null,
                null,
                positionIndex,
                description,
                null);
    }

    public ActionType getType() {
        return type;
    }

    public Personnel getPersonnelSnapshot() {
        return personnelSnapshot;
    }

    public Service getServiceBefore() {
        return serviceBefore;
    }

    public Service getServiceAfter() {
        return serviceAfter;
    }

    public Task getTaskSnapshot() {
        return taskSnapshot;
    }

    public int getPositionIndex() {
        return positionIndex;
    }

    public List<Personnel> getPersonnelOrderSnapshot() {
        return personnelOrderSnapshot;
    }

    public String getDescription() {
        return description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s", timestamp, type, description);
    }

    private static List<Personnel> clonePersonnelList(List<Personnel> source) {
        if (source == null) {
            return null;
        }
        List<Personnel> clones = new ArrayList<>(source.size());
        for (Personnel personnel : source) {
            clones.add(personnel.clone());
        }
        return clones;
    }
}
