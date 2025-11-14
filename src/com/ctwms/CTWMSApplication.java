package com.ctwms;

import com.ctwms.datastructures.PersonnelLinkedList;
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
import jexer.TAction;
import jexer.TApplication;
import jexer.TCheckBox;
import jexer.TComboBox;
import jexer.TField;
import jexer.TInputBox;
import jexer.TList;
import jexer.TMessageBox;
import jexer.TPanel;
import jexer.TTableWidget;
import jexer.TWidget;
import jexer.TWindow;
import jexer.event.TResizeEvent;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Launches the Jexer-based terminal UI for the Campus Task Workflow Management System.
 */
public class CTWMSApplication {

    public static void main(String[] args) {
        launch();
    }

    public void start() {
        launch();
    }

    public static void launch() {
        try {
            ControlCenterApp app = new ControlCenterApp();
            app.run();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to start CTWMS", e);
        }
    }

    private static final class ControlCenterApp extends TApplication {
        private final PersonnelManager personnelManager = new PersonnelManager();
        private final ServiceCatalog serviceCatalog = new ServiceCatalog();
        private final TaskManager taskManager = new TaskManager();
        private final UndoService undoService = new UndoService();
        private int taskSequence = 1;

        private ControlCenterApp() throws Exception {
            super(TApplication.BackendType.SWING);
            setHideMenuBar(true);
            setFocusFollowsMouse(true);
            new ControlCenterWindow();
        }

        private final class ControlCenterWindow extends TWindow {
            private static final int NAV_WIDTH = 24;
            private static final DateTimeFormatter TASK_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd HH:mm");

            private final TPanel navigationPanel;
            private final TPanel contentPanel;
            private TList navList;

            private TTableWidget personnelTable;
            private List<Personnel> personnelSnapshot = Collections.emptyList();

            private TTableWidget serviceTable;
            private List<Service> serviceSnapshot = Collections.emptyList();

            private TTableWidget taskTable;
            private List<Task> taskSnapshot = Collections.emptyList();

            private TList undoList;

            private View activeView = View.DASHBOARD;

            ControlCenterWindow() {
                super(ControlCenterApp.this, "CTWMS Control Center", 110, 36,
                        TWindow.RESIZABLE | TWindow.CENTERED);
                navigationPanel = addPanel(0, 0, NAV_WIDTH, getHeight() - 2);
                contentPanel = addPanel(NAV_WIDTH + 1, 0, getWidth() - NAV_WIDTH - 3, getHeight() - 2);
                buildNavigation();
                renderActiveView();
            }

            @Override
            public void onResize(TResizeEvent event) {
                super.onResize(event);
                navigationPanel.setHeight(getHeight() - 2);
                if (navList != null) {
                    navList.setHeight(Math.max(5, navigationPanel.getHeight() - 6));
                }
                contentPanel.setX(NAV_WIDTH + 1);
                contentPanel.setWidth(Math.max(40, getWidth() - NAV_WIDTH - 3));
                contentPanel.setHeight(getHeight() - 2);
                renderActiveView();
            }

            private void buildNavigation() {
                navigationPanel.addLabel("Workspaces", 1, 0);
                navList = navigationPanel.addList(viewNames(), 1, 2, NAV_WIDTH - 2,
                        Math.max(10, getHeight() - 6), new TAction() {
                            @Override
                            public void DO() {
                                int index = navList.getSelectedIndex();
                                if (index >= 0 && index < View.values().length) {
                                    switchView(View.values()[index]);
                                }
                            }
                        });
                navList.setSelectedIndex(activeView.ordinal());
            }

            private List<String> viewNames() {
                List<String> names = new ArrayList<>();
                for (View view : View.values()) {
                    names.add(view.getTitle());
                }
                return names;
            }

            private void switchView(View view) {
                activeView = view;
                if (navList != null) {
                    navList.setSelectedIndex(view.ordinal());
                }
                renderActiveView();
            }

            private void renderActiveView() {
                clearContent();
                switch (activeView) {
                    case DASHBOARD -> renderDashboard();
                    case PERSONNEL -> renderPersonnelHub();
                    case SERVICES -> renderServicesHub();
                    case TASKS -> renderTasksHub();
                    case UNDO -> renderUndoHub();
                    case REPORTS -> renderReportsHub();
                }
            }

            private void clearContent() {
                List<TWidget> children = new ArrayList<>(contentPanel.getChildren());
                for (TWidget child : children) {
                    child.remove();
                }
                personnelTable = null;
                serviceTable = null;
                taskTable = null;
                undoList = null;
            }

            private void renderDashboard() {
                contentPanel.addLabel("Live System Snapshot", 2, 1);
                TTableWidget metrics = contentPanel.addTable(2, 2,
                        contentPanel.getWidth() - 4, 8);
                metrics.setGridSize(4, 2);
                metrics.setShowRowLabels(false);
                metrics.setShowColumnLabels(false);
                metrics.setCellText(0, 0, "Personnel");
                metrics.setCellText(0, 1, String.valueOf(personnelManager.count()));
                metrics.setCellText(1, 0, "Services");
                metrics.setCellText(1, 1, String.valueOf(serviceCatalog.count()));
                metrics.setCellText(2, 0, "Pending Tasks");
                metrics.setCellText(2, 1, String.valueOf(taskManager.count()));
                metrics.setCellText(3, 0, "Undo Stack");
                metrics.setCellText(3, 1, String.valueOf(undoService.size()));

                contentPanel.addLabel("Next Task", 2, 11);
                Task next = taskManager.peekNextTask();
                String nextLabel = (next == null) ? "No pending tasks." : next.toString();
                contentPanel.addLabel(nextLabel, 4, 12);

                contentPanel.addLabel("Undo Preview", 2, 14);
                List<Action> history = undoService.history();
                String undoText = history.isEmpty() ? "Nothing to undo."
                        : history.get(0).toString();
                contentPanel.addLabel(undoText, 4, 15);

                int buttonRow = contentPanel.getHeight() - 5;
                contentPanel.addButton("Personnel Hub (1)", 2, buttonRow, new TAction() {
                    @Override
                    public void DO() {
                        switchView(View.PERSONNEL);
                    }
                });
                contentPanel.addButton("Services (2)", 22, buttonRow, new TAction() {
                    @Override
                    public void DO() {
                        switchView(View.SERVICES);
                    }
                });
                contentPanel.addButton("Tasks (3)", 40, buttonRow, new TAction() {
                    @Override
                    public void DO() {
                        switchView(View.TASKS);
                    }
                });
                contentPanel.addButton("Reports (4)", 56, buttonRow, new TAction() {
                    @Override
                    public void DO() {
                        switchView(View.REPORTS);
                    }
                });
            }

            private void renderPersonnelHub() {
                contentPanel.addLabel("Personnel Hub · Directory", 2, 1);
                int toolbarY = 3;
                contentPanel.addButton("Add", 2, toolbarY, new TAction() {
                    @Override
                    public void DO() {
                        openPersonnelForm();
                    }
                });
                contentPanel.addButton("Remove Selected", 12, toolbarY, new TAction() {
                    @Override
                    public void DO() {
                        removeSelectedPersonnel();
                    }
                });
                contentPanel.addButton("Search", 32, toolbarY, new TAction() {
                    @Override
                    public void DO() {
                        searchPersonnel();
                    }
                });
                contentPanel.addButton("Sort", 44, toolbarY, new TAction() {
                    @Override
                    public void DO() {
                        personnelManager.sortByName();
                        showInfo("Personnel sorted alphabetically.");
                        renderActiveView();
                    }
                });
                contentPanel.addButton("Refresh", 54, toolbarY, new TAction() {
                    @Override
                    public void DO() {
                        renderActiveView();
                    }
                });

                int tableTop = toolbarY + 2;
                int tableHeight = Math.max(8, contentPanel.getHeight() - tableTop - 2);
                personnelTable = contentPanel.addTable(2, tableTop,
                        contentPanel.getWidth() - 4, tableHeight);
                configurePersonnelTable();
            }

            private void configurePersonnelTable() {
                personnelSnapshot = personnelManager.listAll();
                int rows = Math.max(2, personnelSnapshot.size() + 1);
                personnelTable.setGridSize(rows, 5);
                personnelTable.setShowRowLabels(false);
                personnelTable.setShowColumnLabels(false);
                String[] headers = {"ID", "Name", "Role", "Department", "Email"};
                for (int col = 0; col < headers.length; col++) {
                    personnelTable.setCellText(0, col, headers[col]);
                }
                for (int i = 0; i < personnelSnapshot.size(); i++) {
                    Personnel p = personnelSnapshot.get(i);
                    int row = i + 1;
                    personnelTable.setCellText(row, 0, p.getId());
                    personnelTable.setCellText(row, 1, p.getName());
                    personnelTable.setCellText(row, 2, p.getRole());
                    personnelTable.setCellText(row, 3, p.getDepartment());
                    personnelTable.setCellText(row, 4, p.getEmail());
                }
                personnelTable.setHighlightRow(true);
                if (personnelSnapshot.size() >= 1) {
                    personnelTable.setSelectedRowNumber(1);
                }
            }

            private void openPersonnelForm() {
                final TWindow dialog = new TWindow(ControlCenterApp.this, "Add Personnel",
                        64, 18, TWindow.CENTERED | TWindow.MODAL);
                dialog.addLabel("ID (blank = auto)", 2, 2);
                final TField idField = dialog.addField(22, 2, 36, false);
                dialog.addLabel("Name", 2, 4);
                final TField nameField = dialog.addField(22, 4, 36, false);
                dialog.addLabel("Role", 2, 6);
                final TField roleField = dialog.addField(22, 6, 36, false);
                dialog.addLabel("Department", 2, 8);
                final TField deptField = dialog.addField(22, 8, 36, false);
                dialog.addLabel("Email", 2, 10);
                final TField emailField = dialog.addField(22, 10, 36, false);
                dialog.addLabel("Insert at position", 2, 12);
                final TField positionField = dialog.addField(22, 12, 10, false,
                        String.valueOf(personnelManager.count()));

                dialog.addButton("Save", 2, 14, new TAction() {
                    @Override
                    public void DO() {
                        String id = idField.getText().trim();
                        if (id.isEmpty()) {
                            id = "PER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
                        }
                        String name = nameField.getText().trim();
                        String role = roleField.getText().trim();
                        String department = deptField.getText().trim();
                        String email = emailField.getText().trim();
                        if (name.isEmpty() || role.isEmpty()) {
                            messageBox("Validation", "Name and Role are required.");
                            return;
                        }
                        int position = parseInt(positionField.getText(), personnelManager.count());
                        Personnel personnel = new Personnel(id, name, role, department, email);
                        personnelManager.addPersonnel(personnel, position);
                        undoService.record(Action.personnelAction(ActionType.ADD_PERSONNEL, personnel, position,
                                "Added personnel " + name));
                        showInfo("Personnel added.");
                        dialog.close();
                        renderActiveView();
                    }
                });
                dialog.addButton("Cancel", 12, 14, new TAction() {
                    @Override
                    public void DO() {
                        dialog.close();
                    }
                });
            }

            private void removeSelectedPersonnel() {
                Personnel selected = getSelectedPersonnel();
                if (selected == null) {
                    messageBox("Personnel", "Select a record first.");
                    return;
                }
                TMessageBox confirm = messageBox("Confirm",
                        "Remove " + selected.getName() + "?", TMessageBox.Type.YESNO);
                if (!confirm.isYes()) {
                    return;
                }
                PersonnelLinkedList.RemovalResult result = personnelManager.removeByName(selected.getName());
                if (result.isRemoved()) {
                    undoService.record(Action.personnelAction(ActionType.REMOVE_PERSONNEL,
                            result.getRemovedPersonnel(), result.getIndex(),
                            "Removed personnel " + selected.getName()));
                    showInfo("Personnel removed.");
                    renderActiveView();
                } else {
                    messageBox("Personnel", "Record was not found.");
                }
            }

            private Personnel getSelectedPersonnel() {
                if (personnelTable == null || personnelSnapshot.isEmpty()) {
                    return null;
                }
                int row = personnelTable.getSelectedRowNumber() - 1;
                if (row < 0 || row >= personnelSnapshot.size()) {
                    return null;
                }
                return personnelSnapshot.get(row);
            }

            private void searchPersonnel() {
                TInputBox input = inputBox("Search Personnel", "Enter name keyword:");
                if (input == null || !input.isOk()) {
                    return;
                }
                String keyword = input.getText().trim();
                if (keyword.isEmpty()) {
                    return;
                }
                Personnel found = personnelManager.findByName(keyword);
                if (found != null) {
                    messageBox("Personnel Found", found.toString());
                } else {
                    messageBox("Personnel", "No record matches \"" + keyword + "\".");
                }
            }

            private void renderServicesHub() {
                contentPanel.addLabel("Services Gallery · Catalog", 2, 1);
                int toolbarY = 3;
                contentPanel.addButton("Add Service", 2, toolbarY, new TAction() {
                    @Override
                    public void DO() {
                        openServiceForm(null);
                    }
                });
                contentPanel.addButton("Edit Selected", 16, toolbarY, new TAction() {
                    @Override
                    public void DO() {
                        Service selected = getSelectedService();
                        if (selected == null) {
                            messageBox("Services", "Select a service to edit.");
                            return;
                        }
                        openServiceForm(selected);
                    }
                });
                contentPanel.addButton("Remove Selected", 32, toolbarY, new TAction() {
                    @Override
                    public void DO() {
                        removeSelectedService();
                    }
                });
                contentPanel.addButton("Search", 52, toolbarY, new TAction() {
                    @Override
                    public void DO() {
                        searchService();
                    }
                });
                contentPanel.addButton("Sort", 62, toolbarY, new TAction() {
                    @Override
                    public void DO() {
                        serviceCatalog.sortAlphabetically();
                        showInfo("Services sorted alphabetically.");
                        renderActiveView();
                    }
                });

                int tableTop = toolbarY + 2;
                int tableHeight = Math.max(8, contentPanel.getHeight() - tableTop - 2);
                serviceTable = contentPanel.addTable(2, tableTop,
                        contentPanel.getWidth() - 4, tableHeight);
                configureServiceTable();
            }

            private void configureServiceTable() {
                serviceSnapshot = serviceCatalog.listAll();
                int rows = Math.max(2, serviceSnapshot.size() + 1);
                serviceTable.setGridSize(rows, 5);
                serviceTable.setShowRowLabels(false);
                serviceTable.setShowColumnLabels(false);
                String[] headers = {"Name", "Category", "Status", "Description", " "};
                for (int col = 0; col < headers.length; col++) {
                    serviceTable.setCellText(0, col, headers[col]);
                }
                for (int i = 0; i < serviceSnapshot.size(); i++) {
                    Service service = serviceSnapshot.get(i);
                    int row = i + 1;
                    serviceTable.setCellText(row, 0, service.getName());
                    serviceTable.setCellText(row, 1, service.getCategory());
                    serviceTable.setCellText(row, 2, service.getStatusLabel());
                    serviceTable.setCellText(row, 3, service.getDescription());
                    serviceTable.setCellText(row, 4, service.isActive() ? "✓" : " ");
                }
                serviceTable.setHighlightRow(true);
                if (!serviceSnapshot.isEmpty()) {
                    serviceTable.setSelectedRowNumber(1);
                }
            }

            private void openServiceForm(Service existing) {
                final boolean editing = existing != null;
                final Service original = editing
                        ? serviceCatalog.findByName(existing.getName())
                        : null;
                final TWindow dialog = new TWindow(ControlCenterApp.this,
                        editing ? "Edit Service" : "Add Service",
                        68, 18, TWindow.CENTERED | TWindow.MODAL);
                dialog.addLabel("Name", 2, 2);
                final TField nameField = dialog.addField(20, 2, 40, false,
                        editing ? original.getName() : "");
                dialog.addLabel("Category", 2, 4);
                final TField categoryField = dialog.addField(20, 4, 40, false,
                        editing ? original.getCategory() : "");
                dialog.addLabel("Description", 2, 6);
                final TField descriptionField = dialog.addField(20, 6, 40, false,
                        editing ? original.getDescription() : "");
                dialog.addLabel("Active", 2, 8);
                final TCheckBox activeBox = dialog.addCheckBox(20, 8, "", editing && original.isActive());

                dialog.addButton("Save", 2, 12, new TAction() {
                    @Override
                    public void DO() {
                        String name = nameField.getText().trim();
                        String category = categoryField.getText().trim();
                        String description = descriptionField.getText().trim();
                        boolean active = activeBox.isChecked();
                        if (name.isEmpty()) {
                            messageBox("Validation", "Name is required.");
                            return;
                        }
                        if (!editing) {
                            Service service = new Service(name, description, category, active);
                            serviceCatalog.addService(service);
                            undoService.record(Action.serviceAction(ActionType.ADD_SERVICE, null, service,
                                    serviceCatalog.count() - 1, "Added service " + name));
                            showInfo("Service added.");
                        } else {
                            if (original == null) {
                                messageBox("Services", "Unable to locate original service.");
                                return;
                            }
                            Service before = original.clone();
                            original.setName(name);
                            original.setCategory(category);
                            original.setDescription(description);
                            original.setActive(active);
                            Service after = original.clone();
                            serviceCatalog.replaceService(before.getName(), after);
                            undoService.record(Action.serviceAction(ActionType.EDIT_SERVICE,
                                    before, after, -1, "Edited service " + before.getName()));
                            showInfo("Service updated.");
                        }
                        dialog.close();
                        renderActiveView();
                    }
                });
                dialog.addButton("Cancel", 12, 12, new TAction() {
                    @Override
                    public void DO() {
                        dialog.close();
                    }
                });
            }

            private Service getSelectedService() {
                if (serviceTable == null || serviceSnapshot.isEmpty()) {
                    return null;
                }
                int row = serviceTable.getSelectedRowNumber() - 1;
                if (row < 0 || row >= serviceSnapshot.size()) {
                    return null;
                }
                return serviceSnapshot.get(row);
            }

            private void removeSelectedService() {
                Service selected = getSelectedService();
                if (selected == null) {
                    messageBox("Services", "Select a service first.");
                    return;
                }
                TMessageBox confirm = messageBox("Confirm",
                        "Remove service \"" + selected.getName() + "\"?",
                        TMessageBox.Type.YESNO);
                if (!confirm.isYes()) {
                    return;
                }
                int index = serviceCatalog.indexOf(selected.getName());
                Service removed = serviceCatalog.removeService(selected.getName());
                if (removed != null) {
                    undoService.record(Action.serviceAction(ActionType.REMOVE_SERVICE,
                            removed, null, index, "Removed service " + removed.getName()));
                    showInfo("Service removed.");
                    renderActiveView();
                } else {
                    messageBox("Services", "Record not found.");
                }
            }

            private void searchService() {
                TInputBox input = inputBox("Search Services", "Enter keyword:");
                if (input == null || !input.isOk()) {
                    return;
                }
                String keyword = input.getText().trim();
                if (keyword.isEmpty()) {
                    return;
                }
                List<Service> matches = serviceCatalog.search(keyword);
                if (matches.isEmpty()) {
                    messageBox("Services", "No services match \"" + keyword + "\".");
                } else {
                    String joined = matches.stream()
                            .map(Service::toString)
                            .collect(Collectors.joining("\n"));
                    messageBox("Matches", joined);
                }
            }

            private void renderTasksHub() {
                contentPanel.addLabel("Task Queue · Intake Portal", 2, 1);
                int toolbarY = 3;
                contentPanel.addButton("Add Task", 2, toolbarY, new TAction() {
                    @Override
                    public void DO() {
                        openTaskForm();
                    }
                });
                contentPanel.addButton("Serve Next", 14, toolbarY, new TAction() {
                    @Override
                    public void DO() {
                        serveNextTask();
                    }
                });
                contentPanel.addButton("Refresh", 28, toolbarY, new TAction() {
                    @Override
                    public void DO() {
                        renderActiveView();
                    }
                });
                contentPanel.addLabel("Next:", 44, toolbarY);
                Task next = taskManager.peekNextTask();
                String label = next == null ? "None" : next.toString();
                contentPanel.addLabel(label, 50, toolbarY);

                int tableTop = toolbarY + 2;
                int tableHeight = Math.max(8, contentPanel.getHeight() - tableTop - 2);
                taskTable = contentPanel.addTable(2, tableTop,
                        contentPanel.getWidth() - 4, tableHeight);
                configureTaskTable();
            }

            private void configureTaskTable() {
                taskSnapshot = taskManager.listPendingTasks();
                int rows = Math.max(2, taskSnapshot.size() + 1);
                taskTable.setGridSize(rows, 5);
                taskTable.setShowRowLabels(false);
                taskTable.setShowColumnLabels(false);
                String[] headers = {"ID", "Requestor", "Description", "Priority", "Created"};
                for (int col = 0; col < headers.length; col++) {
                    taskTable.setCellText(0, col, headers[col]);
                }
                for (int i = 0; i < taskSnapshot.size(); i++) {
                    Task task = taskSnapshot.get(i);
                    int row = i + 1;
                    taskTable.setCellText(row, 0, task.getTaskId());
                    taskTable.setCellText(row, 1, task.getRequestor());
                    taskTable.setCellText(row, 2, task.getDescription());
                    taskTable.setCellText(row, 3, task.getPriority().name());
                    taskTable.setCellText(row, 4,
                            task.getCreatedAt().format(TASK_TIME_FORMATTER));
                }
                taskTable.setHighlightRow(true);
                if (!taskSnapshot.isEmpty()) {
                    taskTable.setSelectedRowNumber(1);
                }
            }

            private void openTaskForm() {
                final TWindow dialog = new TWindow(ControlCenterApp.this, "Add Task",
                        64, 18, TWindow.CENTERED | TWindow.MODAL);
                dialog.addLabel("Requestor", 2, 2);
                final TField requestorField = dialog.addField(18, 2, 40, false);
                dialog.addLabel("Description", 2, 4);
                final TField descriptionField = dialog.addField(18, 4, 40, false);
                dialog.addLabel("Priority", 2, 6);
                List<String> priorities = List.of("HIGH", "MEDIUM", "LOW");
                final TComboBox priorityCombo = dialog.addComboBox(18, 6, 20,
                        priorities, priorities.size(), 1, null);
                dialog.addButton("Save", 2, 10, new TAction() {
                    @Override
                    public void DO() {
                        String requestor = requestorField.getText().trim();
                        String description = descriptionField.getText().trim();
                        String priorityLabel = priorityCombo.getText();
                        if (requestor.isEmpty() || description.isEmpty()) {
                            messageBox("Validation", "Requestor and description are required.");
                            return;
                        }
                        TaskPriority priority = TaskPriority.fromInput(priorityLabel);
                        String taskId = "TASK-" + taskSequence++;
                        Task task = new Task(taskId, requestor, description, priority);
                        taskManager.addTask(task);
                        undoService.record(Action.taskAction(ActionType.ADD_TASK, task,
                                "Added task " + taskId));
                        showInfo("Task enqueued with ID " + taskId);
                        dialog.close();
                        renderActiveView();
                    }
                });
                dialog.addButton("Cancel", 12, 10, new TAction() {
                    @Override
                    public void DO() {
                        dialog.close();
                    }
                });
            }

            private void serveNextTask() {
                Task served = taskManager.serveNextTask();
                if (served == null) {
                    messageBox("Tasks", "No tasks available.");
                    return;
                }
                undoService.record(Action.taskAction(ActionType.SERVE_TASK, served,
                        "Served task " + served.getTaskId()));
                showInfo("Serving task: " + served);
                renderActiveView();
            }

            private void renderUndoHub() {
                contentPanel.addLabel("Undo Center · Safety Net", 2, 1);
                contentPanel.addButton("Undo Last", 2, 3, new TAction() {
                    @Override
                    public void DO() {
                        if (undoService.undoLast(personnelManager, taskManager, serviceCatalog)) {
                            showInfo("Last action undone.");
                            renderActiveView();
                        } else {
                            messageBox("Undo", "Nothing to undo.");
                        }
                    }
                });
                contentPanel.addButton("Clear History", 16, 3, new TAction() {
                    @Override
                    public void DO() {
                        TMessageBox confirm = messageBox("Confirm",
                                "Clear entire undo history?", TMessageBox.Type.YESNO);
                        if (confirm.isYes()) {
                            undoService.clear();
                            showInfo("Undo history cleared.");
                            renderActiveView();
                        }
                    }
                });
                undoList = contentPanel.addList(Collections.emptyList(), 2, 6,
                        contentPanel.getWidth() - 4, contentPanel.getHeight() - 8);
                refreshUndoList();
            }

            private void refreshUndoList() {
                if (undoList == null) {
                    return;
                }
                List<String> entries = undoService.history().stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());
                if (entries.isEmpty()) {
                    entries.add("Undo stack is empty.");
                }
                undoList.setList(entries);
            }

            private void renderReportsHub() {
                contentPanel.addLabel("Reports Hub · Operational Pulse", 2, 1);
                TTableWidget summary = contentPanel.addTable(2, 3,
                        contentPanel.getWidth() - 4, 7);
                summary.setGridSize(3, 2);
                summary.setShowRowLabels(false);
                summary.setShowColumnLabels(false);
                summary.setCellText(0, 0, "Personnel count");
                summary.setCellText(0, 1, String.valueOf(personnelManager.count()));
                summary.setCellText(1, 0, "Service catalog size");
                summary.setCellText(1, 1, String.valueOf(serviceCatalog.count()));
                summary.setCellText(2, 0, "Pending tasks");
                summary.setCellText(2, 1, String.valueOf(taskManager.count()));

                contentPanel.addButton("Show Summary Dialog", 2, 12, new TAction() {
                    @Override
                    public void DO() {
                        StringBuilder builder = new StringBuilder();
                        builder.append("Personnel: ").append(personnelManager.count()).append("\n")
                                .append("Services: ").append(serviceCatalog.count()).append("\n")
                                .append("Pending tasks: ").append(taskManager.count()).append("\n");
                        Task next = taskManager.peekNextTask();
                        if (next != null) {
                            builder.append("Next task: ").append(next).append("\n");
                        } else {
                            builder.append("Next task: None\n");
                        }
                        List<Action> history = undoService.history();
                        if (!history.isEmpty()) {
                            builder.append("Last undoable action: ").append(history.get(0));
                        } else {
                            builder.append("Last undoable action: None");
                        }
                        messageBox("System Summary", builder.toString());
                    }
                });
            }

            private void showInfo(String message) {
                messageBox("CTWMS", message);
            }

            private int parseInt(String value, int defaultValue) {
                try {
                    return Integer.parseInt(value.trim());
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            }
        }
    }

    private enum View {
        DASHBOARD("Dashboard"),
        PERSONNEL("Personnel Hub"),
        SERVICES("Services Gallery"),
        TASKS("Task Queue"),
        UNDO("Undo Center"),
        REPORTS("Reports Hub");

        private final String title;

        View(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }
}
