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

import java.time.format.DateTimeFormatter;
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

    private static final int CONSOLE_WIDTH = 70;
    private static final String PRIMARY_DIVIDER = "=".repeat(CONSOLE_WIDTH);
    private static final String SECONDARY_DIVIDER = "-".repeat(CONSOLE_WIDTH);
    private static final DateTimeFormatter TASK_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    private int taskSequence = 1;

    public static void main(String[] args) {
        new CTWMSApplication().run();
    }

    /**
     * Allows external launchers (such as CTWMSTui) to reuse the same workflow.
     */
    public void start() {
        run();
    }

    private void run() {
        boolean exit = false;
        while (!exit) {
            printMainMenu();
            int choice = readMenuChoice("Select an option: ", 0, 5);
            switch (choice) {
                case 1 -> managePersonnelMenu();
                case 2 -> manageServicesMenu();
                case 3 -> manageTasksMenu();
                case 4 -> undoMenu();
                case 5 -> showSummary();
                case 0 -> exit = true;
                default -> printWarning("Invalid option. Please try again.");
            }
        }
        printBanner("Session Closed");
        printInfo("Thank you for using CTWMS. Goodbye!");
    }

    private void printMainMenu() {
        printMenu("Campus Task Workflow Management System",
                "1) Manage Campus Personnel (Linked List)",
                "2) Manage Campus Services Catalog (ArrayList)",
                "3) Manage Campus Task Requests (Queue)",
                "4) Undo System (Stack)",
                "5) View System Summary / Reports",
                "0) Exit");
    }

    private void managePersonnelMenu() {
        boolean back = false;
        while (!back) {
            printMenu("Personnel Management",
                    "1) Add Personnel",
                    "2) Remove Personnel by Name",
                    "3) Search Personnel by Name",
                    "4) Sort Personnel Alphabetically",
                    "5) Display All Personnel",
                    "6) Show Total Personnel Count",
                    "0) Back to Main Menu");
            int choice = readMenuChoice("Choose an option: ", 0, 6);
            switch (choice) {
                case 1 -> addPersonnel();
                case 2 -> removePersonnel();
                case 3 -> searchPersonnel();
                case 4 -> {
                    personnelManager.sortByName();
                    printInfo("Personnel list sorted alphabetically.");
                }
                case 5 -> displayPersonnel();
                case 6 -> printInfo(String.format("Total registered personnel: %d", personnelManager.count()));
                case 0 -> back = true;
                default -> printWarning("Invalid choice. Try again.");
            }
        }
    }

    private void addPersonnel() {
        printSubHeading("Personnel Details");
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
        printSuccess("Personnel added successfully.");
    }

    private void removePersonnel() {
        if (personnelManager.count() == 0) {
            printWarning("No personnel to remove.");
            return;
        }
        String name = readLine("Enter the exact name to remove: ");
        var result = personnelManager.removeByName(name);
        if (result.isRemoved()) {
            undoService.record(Action.personnelAction(ActionType.REMOVE_PERSONNEL,
                    result.getRemovedPersonnel(), result.getIndex(), "Removed personnel " + name));
            printSuccess("Personnel removed.");
        } else {
            printWarning("Personnel not found.");
        }
    }

    private void searchPersonnel() {
        String name = readLine("Enter name to search: ");
        Personnel found = personnelManager.findByName(name);
        if (found != null) {
            printInfo("Record found: " + found);
        } else {
            printWarning("No personnel located with that name.");
        }
    }

    private void displayPersonnel() {
        List<Personnel> personnel = personnelManager.listAll();
        if (personnel.isEmpty()) {
            printWarning("No personnel registered yet.");
            return;
        }
        printSubHeading(String.format("Personnel Directory (%d)", personnel.size()));
        System.out.printf("%-4s %-22s %-18s %-16s %-25s%n", "#", "Name", "Role", "Department", "Email");
        System.out.println(SECONDARY_DIVIDER);
        for (int i = 0; i < personnel.size(); i++) {
            Personnel person = personnel.get(i);
            System.out.printf("%-4d %-22s %-18s %-16s %-25s%n",
                    i + 1,
                    truncate(person.getName(), 22),
                    truncate(person.getRole(), 18),
                    truncate(person.getDepartment(), 16),
                    truncate(person.getEmail(), 25));
        }
        System.out.println(SECONDARY_DIVIDER);
        waitForEnter();
    }

    private void manageServicesMenu() {
        boolean back = false;
        while (!back) {
            printMenu("Service Catalog",
                    "1) Add Service",
                    "2) Upgrade (Edit) Service",
                    "3) Exclude (Remove) Service",
                    "4) Search Service",
                    "5) Sort Services Alphabetically",
                    "6) Display All Services",
                    "0) Back to Main Menu");
            int choice = readMenuChoice("Choose an option: ", 0, 6);
            switch (choice) {
                case 1 -> addService();
                case 2 -> editService();
                case 3 -> removeService();
                case 4 -> searchService();
                case 5 -> {
                    serviceCatalog.sortAlphabetically();
                    printInfo("Services sorted alphabetically.");
                }
                case 6 -> displayServices(serviceCatalog.listAll());
                case 0 -> back = true;
                default -> printWarning("Invalid option. Try again.");
            }
        }
    }

    private void addService() {
        printSubHeading("New Service Details");
        String name = readLine("Service Name: ");
        String description = readLine("Description: ");
        String category = readLine("Category: ");
        boolean active = readBoolean("Is the service active? (y/n): ");
        Service service = new Service(name, description, category, active);
        serviceCatalog.addService(service);
        undoService.record(Action.serviceAction(ActionType.ADD_SERVICE, null, service,
                serviceCatalog.count() - 1, "Added service " + name));
        printSuccess("Service added to catalog.");
    }

    private void editService() {
        if (serviceCatalog.count() == 0) {
            printWarning("No services available to edit.");
            return;
        }
        String name = readLine("Enter the current service name to edit: ");
        Service existing = serviceCatalog.findByName(name);
        if (existing == null) {
            printWarning("Service not found.");
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
        printSuccess("Service updated.");
    }

    private void removeService() {
        if (serviceCatalog.count() == 0) {
            printWarning("No services to remove.");
            return;
        }
        String name = readLine("Enter the service name to remove: ");
        int index = serviceCatalog.indexOf(name);
        Service removed = serviceCatalog.removeService(name);
        if (removed != null) {
            undoService.record(Action.serviceAction(ActionType.REMOVE_SERVICE, removed, null,
                    index, "Removed service " + name));
            printSuccess("Service removed from catalog.");
        } else {
            printWarning("Service not located.");
        }
    }

    private void searchService() {
        String keyword = readLine("Enter name or keyword: ");
        List<Service> matches = serviceCatalog.search(keyword);
        if (matches.isEmpty()) {
            printWarning("No services match your search.");
        } else {
            displayServices(matches);
        }
    }

    private void displayServices(List<Service> services) {
        if (services.isEmpty()) {
            printWarning("Service catalog is empty.");
            return;
        }
        printSubHeading(String.format("Service Catalog (%d)", services.size()));
        System.out.printf("%-4s %-22s %-16s %-10s %-30s%n", "#", "Name", "Category", "Status", "Description");
        System.out.println(SECONDARY_DIVIDER);
        for (int i = 0; i < services.size(); i++) {
            Service service = services.get(i);
            System.out.printf("%-4d %-22s %-16s %-10s %-30s%n",
                    i + 1,
                    truncate(service.getName(), 22),
                    truncate(service.getCategory(), 16),
                    service.getStatusLabel(),
                    truncate(service.getDescription(), 30));
        }
        System.out.println(SECONDARY_DIVIDER);
        waitForEnter();
    }

    private void manageTasksMenu() {
        boolean back = false;
        while (!back) {
            printMenu("Task Request Queue",
                    "1) Add Task Request",
                    "2) View Next Task",
                    "3) Serve Next Task",
                    "4) Display Pending Tasks",
                    "0) Back to Main Menu");
            int choice = readMenuChoice("Choose an option: ", 0, 4);
            switch (choice) {
                case 1 -> addTask();
                case 2 -> peekTask();
                case 3 -> serveTask();
                case 4 -> displayTasks();
                case 0 -> back = true;
                default -> printWarning("Invalid menu option.");
            }
        }
    }

    private void addTask() {
        printSubHeading("Task Request Details");
        String requestor = readLine("Requestor Name: ");
        String description = readLine("Task Description: ");
        String priorityInput = readLine("Priority (HIGH/MEDIUM/LOW): ");
        TaskPriority priority = TaskPriority.fromInput(priorityInput);
        String taskId = "TASK-" + taskSequence++;
        Task task = new Task(taskId, requestor, description, priority);
        taskManager.addTask(task);
        undoService.record(Action.taskAction(ActionType.ADD_TASK, task, "Added task " + taskId));
        printSuccess("Task enqueued with ID " + taskId);
    }

    private void peekTask() {
        Task next = taskManager.peekNextTask();
        if (next == null) {
            printWarning("No pending tasks.");
        } else {
            printInfo("Next task to serve: " + next);
        }
    }

    private void serveTask() {
        Task served = taskManager.serveNextTask();
        if (served == null) {
            printWarning("No tasks to serve.");
        } else {
            printSuccess("Serving task: " + served);
            undoService.record(Action.taskAction(ActionType.SERVE_TASK, served, "Served task " + served.getTaskId()));
        }
    }

    private void displayTasks() {
        List<Task> tasks = taskManager.listPendingTasks();
        if (tasks.isEmpty()) {
            printWarning("Task queue is empty.");
            return;
        }
        printSubHeading(String.format("Pending Tasks (%d)", tasks.size()));
        System.out.printf("%-8s %-15s %-30s %-8s %-16s%n", "ID", "Requestor", "Description", "Priority", "Created");
        System.out.println(SECONDARY_DIVIDER);
        for (Task task : tasks) {
            System.out.printf("%-8s %-15s %-30s %-8s %-16s%n",
                    task.getTaskId(),
                    truncate(task.getRequestor(), 15),
                    truncate(task.getDescription(), 30),
                    task.getPriority(),
                    task.getCreatedAt().format(TASK_TIME_FORMATTER));
        }
        System.out.println(SECONDARY_DIVIDER);
        waitForEnter();
    }

    private void undoMenu() {
        boolean back = false;
        while (!back) {
            printMenu("Undo System",
                    "1) Undo Last Action",
                    "2) Show Undo History",
                    "3) Clear Undo History",
                    "0) Back to Main Menu");
            int choice = readMenuChoice("Choose an option: ", 0, 3);
            switch (choice) {
                case 1 -> undoLastAction();
                case 2 -> showUndoHistory();
                case 3 -> {
                    undoService.clear();
                    printInfo("Undo history cleared.");
                }
                case 0 -> back = true;
                default -> printWarning("Invalid choice.");
            }
        }
    }

    private void undoLastAction() {
        boolean success = undoService.undoLast(personnelManager, taskManager, serviceCatalog);
        if (success) {
            printSuccess("Last action undone successfully.");
        } else {
            printWarning("No actions available to undo.");
        }
    }

    private void showUndoHistory() {
        List<Action> history = undoService.history();
        if (history.isEmpty()) {
            printWarning("Undo stack is empty.");
            return;
        }
        printSubHeading(String.format("Undo History (most recent first) [%d]", history.size()));
        for (int i = 0; i < history.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, history.get(i));
        }
        waitForEnter();
    }

    private void showSummary() {
        printBanner("System Summary");
        System.out.printf("%-25s : %d%n", "Personnel count", personnelManager.count());
        System.out.printf("%-25s : %d%n", "Service catalog size", serviceCatalog.count());
        System.out.printf("%-25s : %d%n", "Pending tasks", taskManager.count());
        Task nextTask = taskManager.peekNextTask();
        if (nextTask != null) {
            printInfo("Next task in queue: " + nextTask);
        } else {
            printWarning("No pending tasks at the moment.");
        }
        List<Action> history = undoService.history();
        if (!history.isEmpty()) {
            printInfo("Last undoable action: " + history.get(0));
        } else {
            printWarning("Undo stack is currently empty.");
        }
        waitForEnter("Press Enter to return to the main menu...");
    }

    private int readMenuChoice(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value < min || value > max) {
                printWarning(String.format("Please choose a number between %d and %d.", min, max));
            } else {
                return value;
            }
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                printWarning("Please enter a valid integer.");
            }
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private boolean readBoolean(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes") || input.equalsIgnoreCase("active")) {
                return true;
            }
            if (input.equalsIgnoreCase("n") || input.equalsIgnoreCase("no") || input.equalsIgnoreCase("inactive")) {
                return false;
            }
            printWarning("Please enter 'y' or 'n'.");
        }
    }

    private int parseOrDefault(String input, int defaultValue) {
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void printMenu(String title, String... options) {
        printBanner(title);
        for (String option : options) {
            System.out.printf("  %s%n", option);
        }
        System.out.println(SECONDARY_DIVIDER);
    }

    private void printBanner(String title) {
        System.out.println("\n" + PRIMARY_DIVIDER);
        System.out.println(centerText(title, PRIMARY_DIVIDER.length()));
        System.out.println(PRIMARY_DIVIDER);
    }

    private void printSubHeading(String title) {
        System.out.println("\n" + title);
        System.out.println(SECONDARY_DIVIDER);
    }

    private void printInfo(String message) {
        System.out.println("[i] " + message);
    }

    private void printSuccess(String message) {
        System.out.println("[OK] " + message);
    }

    private void printWarning(String message) {
        System.out.println("[!] " + message);
    }

    private void waitForEnter() {
        waitForEnter("Press Enter to return to the previous menu...");
    }

    private void waitForEnter(String prompt) {
        System.out.print("\n" + prompt);
        scanner.nextLine();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private String centerText(String text, int width) {
        if (text == null) {
            text = "";
        }
        if (text.length() >= width) {
            return text;
        }
        int padding = (width - text.length()) / 2;
        String left = " ".repeat(padding);
        String right = " ".repeat(width - text.length() - padding);
        return left + text + right;
    }
}
