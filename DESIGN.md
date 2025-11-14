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
  - Builds manager instances and drives console menus for personnel, services, tasks, undo, and summary.
  - Responsible for capturing user input and delegating to managers while pushing undo actions.
