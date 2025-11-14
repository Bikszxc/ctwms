package com.ctwms.manager;

import com.ctwms.datastructures.TaskQueue;
import com.ctwms.model.Task;

import java.util.List;

/**
 * Wraps the task queue to expose higher-level operations.
 */
public class TaskManager {
    private final TaskQueue queue = new TaskQueue();

    public void addTask(Task task) {
        queue.enqueue(task);
    }

    public Task peekNextTask() {
        return queue.peek();
    }

    public Task serveNextTask() {
        return queue.dequeue();
    }

    public List<Task> listPendingTasks() {
        return queue.toList();
    }

    public int count() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public Task removeById(String taskId) {
        return queue.removeById(taskId);
    }

    public void requeueAtFront(Task task) {
        queue.requeueAtFront(task);
    }
}
