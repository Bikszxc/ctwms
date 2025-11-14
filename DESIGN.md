# CTWMS Class Design

## Models
- `Personnel`
  - Fields: `id`, `name`, `role`, `department`, `email`
  - Methods: getters, `toString`, helper `clone`.
- `Task`
  - Fields: `taskId`, `requestor`, `description`, `priority` (`TaskPriority` enum), `createdAt`.
- `Service`
  - Fields: `name`, `description`, `category`, `active` flag.
- `Action`
  - Captures undo information.
  - Fields: `type` (`ActionType` enum), `personnelSnapshot`, `serviceSnapshot`, `taskSnapshot`, metadata such as `description`, `positionIndex`.

## Data Structures
- `PersonnelLinkedList`
  - Custom singly linked list storing `Personnel` nodes.
  - Operations: insert at position, remove by name, search by name, selection sort by name, count, iteration utility.
- `ActionStack`
  - Custom stack using linked nodes storing `Action`.
  - Operations: push, pop, peek, clear, traversal for history.
- `TaskQueue`
  - Custom queue supporting priority levels (HIGH, MEDIUM, LOW) while keeping FIFO within each level.
  - Uses separate linked queues internally and exposes `enqueue`, `dequeue`, `peek`, `isEmpty`, `asList`.

## Managers / Services
- `PersonnelManager`
  - Holds `PersonnelLinkedList`.
  - Methods for add/insert, remove, search, list, sort, count.
- `ServiceCatalog`
  - Backed by `ArrayList<Service>`.
  - Methods for add, update, remove, search, sort, list.
- `TaskManager`
  - Uses `TaskQueue` for task lifecycle.
  - Methods for enqueue, peek, serve/dequeue, list pending tasks.
- `UndoManager`
  - Wraps `ActionStack`.
  - Provides `recordAction`, `undoLast`, `displayHistory`, `clear`.
  - Collaborates with other managers to revert operations.

## Main Application
- `CTWMSApplication`
  - Contains `main` method.
  - Launches a Jexer `TApplication` to render the control center UI.
  - Responsible for building manager instances and binding them to TUI views/components.

## TUI Experience (Jexer)
- Overall layout
  - Single maximized window titled "CTWMS Control Center".
  - Left `TPanel` hosts primary navigation buttons (Dashboard, Personnel, Services, Tasks, Undo, Reports).
  - Right content panel swaps between feature-specific boards using a lightweight view controller.
- Dashboard View
  - Displays metric cards for counts (personnel, services, tasks, undo items) using `TTable`.
  - Shows next task preview and recent undo summary.
  - Provides quick action buttons to jump into other views.
- Personnel View
  - Top toolbar with buttons: Add, Edit, Remove, Sort, Refresh.
  - Central `TTable` listing personnel with columns (ID, Name, Role, Department, Email).
  - Forms captured via modal `TWindow` (text fields + position spinner) with validation.
- Services View
  - Similar toolbar with Add / Edit / Remove / Search / Sort.
  - Table displays service catalog.
  - Edit dialog allows toggling active flag and updating fields.
- Tasks View
  - Buttons: Add Task, Peek, Serve, Refresh.
  - Table listing queue items plus mini status label for next task.
- Undo View
  - `TList` showing recorded undo actions, with buttons to Undo Last and Clear.
- Reports View
  - Snapshot table plus buttons to open summary modal and export textual summary (future).
- Global Features
  - Status bar messages for success/error feedback.
  - Modal alerts confirm destructive actions.
  - Keyboard shortcuts mirror navigation numbers (1-6) for quick switching.
