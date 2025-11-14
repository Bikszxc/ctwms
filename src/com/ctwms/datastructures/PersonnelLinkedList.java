package com.ctwms.datastructures;

import com.ctwms.model.Personnel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Custom singly linked list dedicated to storing {@link Personnel} records.
 */
public class PersonnelLinkedList {
    private static class Node {
        private Personnel data;
        private Node next;

        Node(Personnel data) {
            this.data = data;
        }
    }

    public static class RemovalResult {
        private final boolean removed;
        private final Personnel removedPersonnel;
        private final int index;

        private RemovalResult(boolean removed, Personnel removedPersonnel, int index) {
            this.removed = removed;
            this.removedPersonnel = removedPersonnel;
            this.index = index;
        }

        public boolean isRemoved() {
            return removed;
        }

        public Personnel getRemovedPersonnel() {
            return removedPersonnel;
        }

        public int getIndex() {
            return index;
        }
    }

    private Node head;
    private int size;

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Inserts a personnel record at the exact position requested (0 = head).
     */
    public void insertAtPosition(Personnel personnel, int position) {
        if (personnel == null) {
            return;
        }
        Node newNode = new Node(personnel);
        if (position <= 0 || head == null) {
            newNode.next = head;
            head = newNode;
        } else if (position >= size) {
            Node current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        } else {
            Node prev = head;
            for (int i = 0; i < position - 1; i++) {
                prev = prev.next;
            }
            newNode.next = prev.next;
            prev.next = newNode;
        }
        size++;
    }

    /**
     * Removes a personnel entry using a case-insensitive name match.
     */
    public RemovalResult removeByName(String name) {
        if (name == null || head == null) {
            return new RemovalResult(false, null, -1);
        }
        String target = name.toLowerCase(Locale.ROOT);
        Node current = head;
        Node prev = null;
        int index = 0;
        while (current != null) {
            if (current.data.getName().toLowerCase(Locale.ROOT).equals(target)) {
                if (prev == null) {
                    head = current.next;
                } else {
                    prev.next = current.next;
                }
                size--;
                return new RemovalResult(true, current.data, index);
            }
            prev = current;
            current = current.next;
            index++;
        }
        return new RemovalResult(false, null, -1);
    }

    public Personnel removeAt(int index) {
        if (index < 0 || index >= size || head == null) {
            return null;
        }
        Node current = head;
        Node prev = null;
        int currentIndex = 0;
        while (current != null && currentIndex < index) {
            prev = current;
            current = current.next;
            currentIndex++;
        }
        if (current == null) {
            return null;
        }
        if (prev == null) {
            head = current.next;
        } else {
            prev.next = current.next;
        }
        size--;
        return current.data;
    }

    public Personnel findByName(String name) {
        if (name == null) {
            return null;
        }
        String target = name.toLowerCase(Locale.ROOT);
        Node current = head;
        while (current != null) {
            if (current.data.getName().toLowerCase(Locale.ROOT).equals(target)) {
                return current.data;
            }
            current = current.next;
        }
        return null;
    }

    public int indexOf(String name) {
        if (name == null) {
            return -1;
        }
        String target = name.toLowerCase(Locale.ROOT);
        Node current = head;
        int index = 0;
        while (current != null) {
            if (current.data.getName().toLowerCase(Locale.ROOT).equals(target)) {
                return index;
            }
            current = current.next;
            index++;
        }
        return -1;
    }

    public Personnel removeById(String id) {
        if (id == null || head == null) {
            return null;
        }
        String target = id.toLowerCase(Locale.ROOT);
        Node current = head;
        Node prev = null;
        while (current != null) {
            if (current.data.getId().toLowerCase(Locale.ROOT).equals(target)) {
                if (prev == null) {
                    head = current.next;
                } else {
                    prev.next = current.next;
                }
                size--;
                return current.data;
            }
            prev = current;
            current = current.next;
        }
        return null;
    }

    /**
     * Performs an insertion sort by personnel name (case-insensitive).
     */
    public void sortByName() {
        if (head == null || head.next == null) {
            return;
        }
        Node sortedHead = null;
        Node current = head;
        while (current != null) {
            Node next = current.next;
            sortedHead = insertSorted(sortedHead, current);
            current = next;
        }
        head = sortedHead;
    }

    private Node insertSorted(Node sortedHead, Node node) {
        node.next = null;
        if (sortedHead == null || compare(node.data, sortedHead.data) <= 0) {
            node.next = sortedHead;
            return node;
        }
        Node current = sortedHead;
        while (current.next != null && compare(current.next.data, node.data) < 0) {
            current = current.next;
        }
        node.next = current.next;
        current.next = node;
        return sortedHead;
    }

    private int compare(Personnel a, Personnel b) {
        return a.getName().compareToIgnoreCase(b.getName());
    }

    public List<Personnel> toList() {
        List<Personnel> list = new ArrayList<>();
        Node current = head;
        while (current != null) {
            list.add(current.data);
            current = current.next;
        }
        return list;
    }

    /**
     * Reinserts the provided personnel at the original index; used during undo of deletions.
     */
    public void reinsert(Personnel personnel, int index) {
        insertAtPosition(personnel, index);
    }

    /**
     * Replaces the entire list contents with the provided ordering.
     */
    public void replaceAll(List<Personnel> orderedPersonnel) {
        head = null;
        size = 0;
        if (orderedPersonnel == null || orderedPersonnel.isEmpty()) {
            return;
        }
        for (Personnel personnel : orderedPersonnel) {
            insertAtPosition(personnel, size);
        }
    }
}
