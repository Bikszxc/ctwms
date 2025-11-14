package com.ctwms.manager;

import com.ctwms.datastructures.PersonnelLinkedList;
import com.ctwms.model.Personnel;

import java.util.List;

/**
 * Provides higher-level operations on the personnel linked list.
 */
public class PersonnelManager {
    private final PersonnelLinkedList personnelList = new PersonnelLinkedList();

    public void addPersonnel(Personnel personnel, int position) {
        if (position < 0) {
            position = personnelList.size();
        }
        personnelList.insertAtPosition(personnel, position);
    }

    public PersonnelLinkedList.RemovalResult removeByName(String name) {
        return personnelList.removeByName(name);
    }

    public Personnel findByName(String name) {
        return personnelList.findByName(name);
    }

    public void sortByName() {
        personnelList.sortByName();
    }

    public int count() {
        return personnelList.size();
    }

    public List<Personnel> listAll() {
        return personnelList.toList();
    }

    public void reinsert(Personnel personnel, int index) {
        personnelList.reinsert(personnel, index);
    }

    public Personnel removeAt(int index) {
        return personnelList.removeAt(index);
    }

    public Personnel removeById(String id) {
        return personnelList.removeById(id);
    }

    public void replaceAll(List<Personnel> orderedPersonnel) {
        personnelList.replaceAll(orderedPersonnel);
    }
}
