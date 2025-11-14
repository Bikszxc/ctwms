package com.ctwms.datastructures;

import com.ctwms.model.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple LIFO stack to track undoable actions.
 */
public class ActionStack {
    private static class Node {
        private final Action data;
        private Node next;

        Node(Action data) {
            this.data = data;
        }
    }

    private Node top;
    private int size;

    public void push(Action action) {
        if (action == null) {
            return;
        }
        Node node = new Node(action);
        node.next = top;
        top = node;
        size++;
    }

    public Action pop() {
        if (top == null) {
            return null;
        }
        Action data = top.data;
        top = top.next;
        size--;
        return data;
    }

    public Action peek() {
        return top != null ? top.data : null;
    }

    public boolean isEmpty() {
        return top == null;
    }

    public int size() {
        return size;
    }

    public void clear() {
        top = null;
        size = 0;
    }

    public List<Action> asList() {
        List<Action> actions = new ArrayList<>();
        Node current = top;
        while (current != null) {
            actions.add(current.data);
            current = current.next;
        }
        return actions;
    }
}
