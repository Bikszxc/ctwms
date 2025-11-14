package com.ctwms.datastructures;

import com.ctwms.model.Task;
import com.ctwms.model.TaskPriority;

import java.util.ArrayList;
import java.util.List;

/**
 * Priority-aware queue that keeps FIFO ordering within each priority level.
 */
public class TaskQueue {
    private static class Node {
        private final Task data;
        private Node next;

        Node(Task data) {
            this.data = data;
        }
    }

    private static final TaskPriority[] ORDER = {TaskPriority.HIGH, TaskPriority.MEDIUM, TaskPriority.LOW};

    private final Node[] heads = new Node[TaskPriority.values().length];
    private final Node[] tails = new Node[TaskPriority.values().length];
    private int size;

    public void enqueue(Task task) {
        if (task == null) {
            return;
        }
        int index = priorityIndex(task.getPriority());
        Node node = new Node(task);
        if (tails[index] == null) {
            heads[index] = node;
            tails[index] = node;
        } else {
            tails[index].next = node;
            tails[index] = node;
        }
        size++;
    }

    public Task peek() {
        for (TaskPriority priority : ORDER) {
            int idx = priorityIndex(priority);
            if (heads[idx] != null) {
                return heads[idx].data;
            }
        }
        return null;
    }

    public Task dequeue() {
        for (TaskPriority priority : ORDER) {
            int idx = priorityIndex(priority);
            if (heads[idx] != null) {
                Node node = heads[idx];
                heads[idx] = node.next;
                if (heads[idx] == null) {
                    tails[idx] = null;
                }
                size--;
                return node.data;
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public List<Task> toList() {
        List<Task> tasks = new ArrayList<>();
        for (TaskPriority priority : ORDER) {
            int idx = priorityIndex(priority);
            Node current = heads[idx];
            while (current != null) {
                tasks.add(current.data);
                current = current.next;
            }
        }
        return tasks;
    }

    public Task removeById(String taskId) {
        if (taskId == null) {
            return null;
        }
        for (TaskPriority priority : ORDER) {
            int idx = priorityIndex(priority);
            Node current = heads[idx];
            Node prev = null;
            while (current != null) {
                if (current.data.getTaskId().equalsIgnoreCase(taskId)) {
                    if (prev == null) {
                        heads[idx] = current.next;
                    } else {
                        prev.next = current.next;
                    }
                    if (current.next == null) {
                        tails[idx] = prev;
                    }
                    size--;
                    return current.data;
                }
                prev = current;
                current = current.next;
            }
        }
        return null;
    }

    public void requeueAtFront(Task task) {
        if (task == null) {
            return;
        }
        int idx = priorityIndex(task.getPriority());
        Node node = new Node(task);
        node.next = heads[idx];
        heads[idx] = node;
        if (tails[idx] == null) {
            tails[idx] = node;
        }
        size++;
    }

    private int priorityIndex(TaskPriority priority) {
        return priority.ordinal();
    }
}
