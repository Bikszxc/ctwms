package com.ctwms.manager;

import com.ctwms.model.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Maintains the campus service catalog backed by an ArrayList.
 */
public class ServiceCatalog {
    private final List<Service> services = new ArrayList<>();

    public void addService(Service service) {
        services.add(service);
    }

    public Service removeService(String name) {
        int index = findIndexByName(name);
        if (index >= 0) {
            return services.remove(index);
        }
        return null;
    }

    public Service findByName(String name) {
        int index = findIndexByName(name);
        return index >= 0 ? services.get(index) : null;
    }

    public Service replaceService(String existingName, Service replacement) {
        int index = findIndexByName(existingName);
        if (index >= 0) {
            Service previous = services.get(index);
            services.set(index, replacement);
            return previous;
        }
        return null;
    }

    public List<Service> search(String keyword) {
        List<Service> matches = new ArrayList<>();
        if (keyword == null) {
            return matches;
        }
        String lower = keyword.toLowerCase(Locale.ROOT);
        for (Service service : services) {
            if (service.getName().toLowerCase(Locale.ROOT).contains(lower)
                    || service.getDescription().toLowerCase(Locale.ROOT).contains(lower)
                    || service.getCategory().toLowerCase(Locale.ROOT).contains(lower)) {
                matches.add(service);
            }
        }
        return matches;
    }

    public void sortAlphabetically() {
        services.sort(Comparator.comparing(Service::getName, String.CASE_INSENSITIVE_ORDER));
    }

    public List<Service> listAll() {
        return new ArrayList<>(services);
    }

    public int count() {
        return services.size();
    }

    private int findIndexByName(String name) {
        if (name == null) {
            return -1;
        }
        String target = name.toLowerCase(Locale.ROOT);
        for (int i = 0; i < services.size(); i++) {
            if (services.get(i).getName().toLowerCase(Locale.ROOT).equals(target)) {
                return i;
            }
        }
        return -1;
    }
}
