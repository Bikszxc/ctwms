package com.ctwms;

import com.ctwms.manager.PersonnelManager;
import com.ctwms.manager.ServiceCatalog;
import com.ctwms.manager.TaskManager;
import com.ctwms.manager.UndoService;
import com.ctwms.model.Action;
import com.ctwms.model.ActionType;
import com.ctwms.model.Personnel;
import com.ctwms.model.Service;
import com.ctwms.model.Task;
import com.ctwms.model.TaskPriority;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * Console entry point for the Campus Task Workflow Management System.
 */
public class CTWMSApplication {
    private final Scanner scanner = new Scanner(System.in);
    private final PersonnelManager personnelManager = new PersonnelManager();
    private final ServiceCatalog serviceCatalog = new ServiceCatalog();
    private final TaskManager taskManager = new TaskManager();
    private final UndoService undoService = new UndoService();

    private int taskSequence = 1;

    public static void main(String[] args) {
        new CTWMSApplication().run();
    }

    private void run() {
        boolean exit = false;
        while (!exit) {
            printMainMenu();
            int choice = readInt("Select an option: ");
            switch (choice) {
                case 1 -> managePersonnelMenu();
                case 2 -> manageServicesMenu();
                case 3 -> manageTasksMenu();
                case 4 -> undoMenu();
                case 5 -> showSummary();
                case 0 -> exit = true;
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
        System.out.println("Thank you for using CTWMS. Goodbye!");
    }

    private void printMainMenu() {
        System.out.println("\n=== Campus Task Workflow Management System ===");
        System.out.println("1. Manage Campus Personnel (Linked List)");
        System.out.println("2. Manage Campus Services Catalog (ArrayList)");
        System.out.println("3. Manage Campus Task Requests (Queue)");
        System.out.println("4. Undo System (Stack)");
        System.out.println("5. View System Summary / Reports");
        System.out.println("0. Exit");
    }

    private void managePersonnelMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Personnel Management ---");
            System.out.println("1. Add Personnel");
            System.out.println("2. Remove Personnel by Name");
            System.out.println("3. Search Personnel by Name");
            System.out.println("4. Sort Personnel Alphabetically");
            System.out.println("5. Display All Personnel");
            System.out.println("6. Show Total Personnel Count");
            System.out.println("0. Back to Main Menu");
            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1 -> addPersonnel();
                case 2 -> removePersonnel();
                case 3 -> searchPersonnel();
                case 4 -> {
                    personnelManager.sortByName();
                    System.out.println("Personnel list sorted alphabetically.");
                }
                case 5 -> displayPersonnel();
                case 6 -> System.out.printf("Total registered personnel: %d%n", personnelManager.count());
                case 0 -> back = true;
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void addPersonnel() {
        System.out.println("\nEnter personnel details.");
        String id = readLine("ID (leave blank to auto-generate): ");
        if (id.isBlank()) {
            id = "PER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        String name = readLine("Full Name: ");
        String role = readLine("Role/Position: ");
        String department = readLine("Department: ");
        String email = readLine("Email: ");
        Personnel personnel = new Personnel(id, name, role, department, email);

        String positionInput = readLine("Insert position (0-based, blank for end): ");
        int position = positionInput.isBlank() ? personnelManager.count() : parseOrDefault(positionInput, personnelManager.count());
        personnelManager.addPersonnel(personnel, position);
        undoService.record(Action.personnelAction(ActionType.ADD_PERSONNEL, personnel,
                position, "Added personnel " + name));
        System.out.println("Personnel added successfully.");
    }

    private void removePersonnel() {
        if (personnelManager.count() == 0) {
            System.out.println("No personnel to remove.");
            return;
        }
        String name = readLine("Enter the exact name to remove: ");
        var result = personnelManager.removeByName(name);
        if (result.isRemoved()) {
            undoService.record(Action.personnelAction(ActionType.REMOVE_PERSONNEL,
                    result.getRemovedPersonnel(), result.getIndex(), "Removed personnel " + name));
            System.out.println("Personnel removed.");
        } else {
            System.out.println("Personnel not found.");
        }
    }

    private void searchPersonnel() {
        String name = readLine("Enter name to search: ");
        Personnel found = personnelManager.findByName(name);
        if (found != null) {
            System.out.println("Record found: " + found);
        } else {
            System.out.println("No personnel located with that name.");
        }
    }

    private void displayPersonnel() {
        List<Personnel> personnel = personnelManager.listAll();
        if (personnel.isEmpty()) {
            System.out.println("No personnel registered yet.");
            return;
        }
        System.out.println("\n-- Personnel List --");
        for (int i = 0; i < personnel.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, personnel.get(i));
        }
    }

    private void manageServicesMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Service Catalog ---");
            System.out.println("1. Add Service");
            System.out.println("2. Upgrade (Edit) Service");
            System.out.println("3. Exclude (Remove) Service");
            System.out.println("4. Search Service");
            System.out.println("5. Sort Services Alphabetically");
            System.out.println("6. Display All Services");
            System.out.println("0. Back to Main Menu");
            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1 -> addService();
                case 2 -> editService();
                case 3 -> removeService();
                case 4 -> searchService();
                case 5 -> {
                    serviceCatalog.sortAlphabetically();
                    System.out.println("Services sorted alphabetically.");
                }
                case 6 -> displayServices(serviceCatalog.listAll());
                case 0 -> back = true;
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void addService() {
        System.out.println("\nEnter new service details.");
        String name = readLine("Service Name: ");
        String description = readLine("Description: ");
        String category = readLine("Category: ");
        boolean active = readBoolean("Is the service active? (y/n): ");
        Service service = new Service(name, description, category, active);
        serviceCatalog.addService(service);
        undoService.record(Action.serviceAction(ActionType.ADD_SERVICE, null, service,
                serviceCatalog.count() - 1, "Added service " + name));
        System.out.println("Service added to catalog.");
    }

    private void editService() {
        if (serviceCatalog.count() == 0) {
            System.out.println("No services available to edit.");
            return;
        }
        String name = readLine("Enter the current service name to edit: ");
        Service existing = serviceCatalog.findByName(name);
        if (existing == null) {
            System.out.println("Service not found.");
            return;
        }
        Service before = existing.clone();
        String newName = readLine("New name (blank to keep): ");
        if (!newName.isBlank()) {
            existing.setName(newName);
        }
        String newDesc = readLine("New description (blank to keep): ");
        if (!newDesc.isBlank()) {
            existing.setDescription(newDesc);
        }
        String newCategory = readLine("New category (blank to keep): ");
        if (!newCategory.isBlank()) {
            existing.setCategory(newCategory);
        }
        String statusInput = readLine("Toggle status? Enter 'active', 'inactive', or blank to keep: ");
        if (!statusInput.isBlank()) {
            boolean active = statusInput.equalsIgnoreCase("active");
            existing.setActive(active);
        }
        Service after = existing.clone();
        serviceCatalog.replaceService(after.getName(), after);
        undoService.record(Action.serviceAction(ActionType.EDIT_SERVICE, before, after, -1,
                "Edited service " + before.getName()));
        System.out.println("Service updated.");
    }

    private void removeService() {
        if (serviceCatalog.count() == 0) {
            System.out.println("No services to remove.");
            return;
        }
        String name = readLine("Enter the service name to remove: ");
        int index = serviceCatalog.indexOf(name);
        Service removed = serviceCatalog.removeService(name);
        if (removed != null) {
            undoService.record(Action.serviceAction(ActionType.REMOVE_SERVICE, removed, null,
                    index, "Removed service " + name));
            System.out.println("Service removed from catalog.");
        } else {
            System.out.println("Service not located.");
        }
    }

    private void searchService() {
        String keyword = readLine("Enter name or keyword: ");
        List<Service> matches = serviceCatalog.search(keyword);
        if (matches.isEmpty()) {
            System.out.println("No services match your search.");
        } else {
            displayServices(matches);
        }
    }

    private void displayServices(List<Service> services) {
        if (services.isEmpty()) {
            System.out.println("Service catalog is empty.");
            return;
        }
        System.out.println("\n-- Services --");
        for (int i = 0; i < services.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, services.get(i));
        }
    }

    private void manageTasksMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Task Request Queue ---");
            System.out.println("1. Add Task Request");
            System.out.println("2. View Next Task");
            System.out.println("3. Serve Next Task");
            System.out.println("4. Display Pending Tasks");
            System.out.println("0. Back to Main Menu");
            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1 -> addTask();
                case 2 -> peekTask();
                case 3 -> serveTask();
                case 4 -> displayTasks();
                case 0 -> back = true;
                default -> System.out.println("Invalid menu option.");
            }
        }
    }

    private void addTask() {
        System.out.println("\nEnter task request details.");
        String requestor = readLine("Requestor Name: ");
        String description = readLine("Task Description: ");
        String priorityInput = readLine("Priority (HIGH/MEDIUM/LOW): ");
        TaskPriority priority = TaskPriority.fromInput(priorityInput);
        String taskId = "TASK-" + taskSequence++;
        Task task = new Task(taskId, requestor, description, priority);
        taskManager.addTask(task);
        undoService.record(Action.taskAction(ActionType.ADD_TASK, task, "Added task " + taskId));
        System.out.println("Task enqueued with ID " + taskId);
    }

    private void peekTask() {
        Task next = taskManager.peekNextTask();
        if (next == null) {
            System.out.println("No pending tasks.");
        } else {
            System.out.println("Next task to serve: " + next);
        }
    }

    private void serveTask() {
        Task served = taskManager.serveNextTask();
        if (served == null) {
            System.out.println("No tasks to serve.");
        } else {
            System.out.println("Serving task: " + served);
            undoService.record(Action.taskAction(ActionType.SERVE_TASK, served, "Served task " + served.getTaskId()));
        }
    }

    private void displayTasks() {
        List<Task> tasks = taskManager.listPendingTasks();
        if (tasks.isEmpty()) {
            System.out.println("Task queue is empty.");
            return;
        }
        System.out.println("\n-- Pending Tasks --");
        for (Task task : tasks) {
            System.out.println(task);
        }
    }

    private void undoMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Undo System ---");
            System.out.println("1. Undo Last Action");
            System.out.println("2. Show Undo History");
            System.out.println("3. Clear Undo History");
            System.out.println("0. Back to Main Menu");
            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1 -> undoLastAction();
                case 2 -> showUndoHistory();
                case 3 -> {
                    undoService.clear();
                    System.out.println("Undo history cleared.");
                }
                case 0 -> back = true;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void undoLastAction() {
        boolean success = undoService.undoLast(personnelManager, taskManager, serviceCatalog);
        if (success) {
            System.out.println("Last action undone successfully.");
        } else {
            System.out.println("No actions available to undo.");
        }
    }

    private void showUndoHistory() {
        List<Action> history = undoService.history();
        if (history.isEmpty()) {
            System.out.println("Undo stack is empty.");
            return;
        }
        System.out.println("\n-- Undo History (most recent first) --");
        for (int i = 0; i < history.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, history.get(i));
        }
    }

    private void showSummary() {
        System.out.println("\n=== System Summary ===");
        System.out.printf("Personnel count: %d%n", personnelManager.count());
        System.out.printf("Service catalog size: %d%n", serviceCatalog.count());
        System.out.printf("Pending tasks: %d%n", taskManager.count());
        Task nextTask = taskManager.peekNextTask();
        if (nextTask != null) {
            System.out.println("Next task in queue: " + nextTask);
        } else {
            System.out.println("No pending tasks at the moment.");
        }
        List<Action> history = undoService.history();
        if (!history.isEmpty()) {
            System.out.println("Last undoable action: " + history.get(0));
        } else {
            System.out.println("Undo stack is currently empty.");
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private boolean readBoolean(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        return input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes") || input.equalsIgnoreCase("active");
    }

    private int parseOrDefault(String input, int defaultValue) {
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
