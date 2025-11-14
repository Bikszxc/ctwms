package com.ctwms.manager;

import com.ctwms.datastructures.ActionStack;
import com.ctwms.model.Action;
import com.ctwms.model.ActionType;
import com.ctwms.model.Personnel;
import com.ctwms.model.Service;
import com.ctwms.model.Task;

import java.util.List;

/**
 * Central place to record and revert undoable actions.
 */
public class UndoService {
    private final ActionStack actionStack = new ActionStack();

    public void record(Action action) {
        actionStack.push(action);
    }

    public boolean undoLast(PersonnelManager personnelManager,
                            TaskManager taskManager,
                            ServiceCatalog serviceCatalog) {
        Action action = actionStack.pop();
        if (action == null) {
            return false;
        }
        switch (action.getType()) {
            case ADD_PERSONNEL -> revertAddPersonnel(personnelManager, action.getPersonnelSnapshot());
            case REMOVE_PERSONNEL -> revertRemovePersonnel(personnelManager, action.getPersonnelSnapshot(), action.getPositionIndex());
            case ADD_TASK -> revertAddTask(taskManager, action.getTaskSnapshot());
            case SERVE_TASK -> revertServeTask(taskManager, action.getTaskSnapshot());
            case EDIT_SERVICE -> revertEditService(serviceCatalog, action.getServiceBefore(), action.getServiceAfter());
            default -> {
                return false;
            }
        }
        return true;
    }

    public List<Action> history() {
        return actionStack.asList();
    }

    public void clear() {
        actionStack.clear();
    }

    public int size() {
        return actionStack.size();
    }

    private void revertAddPersonnel(PersonnelManager manager, Personnel personnel) {
        if (personnel == null) {
            return;
        }
        manager.removeById(personnel.getId());
    }

    private void revertRemovePersonnel(PersonnelManager manager, Personnel personnel, int position) {
        if (personnel == null) {
            return;
        }
        manager.reinsert(personnel, position);
    }

    private void revertAddTask(TaskManager manager, Task task) {
        if (task == null) {
            return;
        }
        manager.removeById(task.getTaskId());
    }

    private void revertServeTask(TaskManager manager, Task task) {
        if (task == null) {
            return;
        }
        manager.requeueAtFront(task);
    }

    private void revertEditService(ServiceCatalog catalog, Service previous, Service current) {
        if (previous == null) {
            return;
        }
        String targetName = current != null ? current.getName() : previous.getName();
        Service replacement = previous.clone();
        Service outcome = catalog.replaceService(targetName, replacement);
        if (outcome == null && !previous.getName().equalsIgnoreCase(targetName)) {
            catalog.replaceService(previous.getName(), replacement);
        }
    }
}
