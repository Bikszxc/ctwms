# CTWMS – Campus Task Workflow Management System

CTWMS is a fully interactive terminal experience that showcases fundamental data structures through a campus-inspired scenario. Personnel are stored in a linked list, services in an ArrayList-backed catalog, tasks in a priority-aware queue, and undo history in a custom stack. The console UI uses vivid ANSI styling, command shortcuts, and robust validation to keep every workflow smooth.

## ✨ Highlights

- **Modular managers:** `PersonnelManager`, `ServiceCatalog`, `TaskManager`, and `UndoService` encapsulate linked list, array list, queue, and stack logic.
- **Rich CLI:** Clear screens, bold headings, and colon-prefixed commands (`:add-task`, `:undo`, `:shortcuts`) create a pseudo-TUI inside any terminal.
- **Undo system:** Every mutating action pushes a snapshot onto a stack so you can revert adds, removes, task servicing, service edits, and even personnel sorting.
- **Validation first:** Required fields, numeric positions, valid emails, and priority selection are enforced with friendly warnings and re-prompts.
- **Executable jar:** `mvn package` generates `target/ctwms-1.0.0-SNAPSHOT.jar`, which you can double-click or run via `java -jar`.

## 🚀 Getting Started

```bash
# Build
mvn -q package

# Run via exec plugin (optional)
mvn -q exec:java

# Or run the fat jar
java -jar target/ctwms-1.0.0-SNAPSHOT.jar
```

Once running, type menu numbers or command shortcuts (e.g., `:add-personnel`, `:summary`, `:shortcuts`). After each large output, press Enter to return to the active menu.

## 📚 Data Structures & Big-O Notes

| Feature / Structure                        | Description                                            | Complexity (avg) |
|--------------------------------------------|--------------------------------------------------------|------------------|
| Personnel linked list (`PersonnelLinkedList.insertAtPosition`) | Inserts at arbitrary index in singly linked list       | `O(n)`           |
| Personnel removal by name (`removeByName`) | Linear scan to find and unlink node                    | `O(n)`           |
| Personnel sort (`sortByName`)              | Insertion sort rebuilding a new linked list            | `O(n²)`          |
| Service catalog (`ServiceCatalog.addService`) | ArrayList append                                       | `O(1)` amortized |
| Service search (`ServiceCatalog.search`)   | Iterates through ArrayList                             | `O(n)`           |
| Task queue (`TaskManager.addTask`)         | Enqueues into priority-aware structure (three queues)  | `O(1)`           |
| Task serve (`serveNextTask`)               | Dequeues highest priority queue                        | `O(1)`           |
| Undo stack (`UndoService.record`)          | Push/pop on custom stack                               | `O(1)`           |

> These complexities highlight the tradeoffs of each backing structure—linked lists for insertion flexibility, dynamic arrays for catalog browsing, specialized queues for priority handling, and stacks for undo history.

## 🧩 Keyboard Commands

Enter any of the following at a prompt (leading `:` is required):

| Command              | Action                              |
|----------------------|-------------------------------------|
| `:add-personnel`     | Launch personnel intake flow        |
| `:remove-personnel`  | Remove by exact name                |
| `:search-personnel`  | Search directory                    |
| `:list-personnel`    | Show formatted personnel table      |
| `:add-service`       | Add a service entry                 |
| `:remove-service`    | Remove service by name              |
| `:search-service`    | Search catalog                      |
| `:add-task`          | Enqueue new task request            |
| `:serve-task`        | Serve next available task           |
| `:list-tasks`        | Display pending queue               |
| `:undo`              | Undo last action                    |
| `:undo-history`      | Show undo stack contents            |
| `:summary`           | View system summary                 |
| `:shortcuts`         | Display the command reference       |

## 🧪 Validation Examples

- **Personnel add:** Blank name/role/department or invalid email prompts a warning until corrected.
- **Insert position:** Accepts integers ≥ 0 or blank for “append to end”.
- **Tasks:** Priority input loops until `HIGH`, `MEDIUM`, or `LOW` is supplied.
- **Shortcuts:** Unknown commands warn and pause so you never miss the message.

## 🛠️ Extending CTWMS

- Swap data structures to compare performance (e.g., replace linked list with `ArrayList` and observe complexity changes).
- Plug in persistence or networking at the manager layer without touching the UI.
- Add reports using `UndoService.history()` or `TaskManager.listPendingTasks()`.

---

Made by Bikzxc to highlight data structures, undo mechanics, and thoughtful CLI-centered UX. Enjoy hacking on CTWMS! 🎓
