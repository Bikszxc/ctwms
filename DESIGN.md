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
  - Drives the ANSI-styled console menus for personnel, services, tasks, undo, and reports.
  - Responsible for building manager instances and routing user input into those services while logging undo actions.

## Console Experience
- Visual style
  - ANSI colors and bold text draw a clear hierarchy between banners, menus, notices, and tabular headers.
  - All prompts share a consistent bright-white foreground, while subtle hints use dim text.
  - Icons (`[OK]`, `[i]`, `[!]`) remain, but are now color-coded (green/cyan/yellow).
- Main menu
  - Hero splash centers the app title and mantra (“Manage · Inspect · Undo · Report”).
  - Menu numbers are highlighted, and dividers use cyan/blue accents for clarity.
- Sub-menus
  - Each module clears the screen, prints a fresh banner, and shows its own option list with highlighted shortcuts.
  - After performing an action the menu reappears, giving a predictable rhythm.
- Tables and reports
  - Listings for personnel, services, tasks, and undo history render bold headers with colorized separators.
  - Summary view prints metrics with aligned labels and values to spotlight current counts.
- Navigation aids
  - Prompt helper `waitForEnter()` pauses after large outputs so the user can read before returning.
  - Error and validation messages are emphasized in yellow/red text to stand out during data entry.
- Keyboard shortcuts
  - Global commands (typed with a leading `:` such as `:add-personnel`) instantly trigger common workflows (add/remove/search personnel, services, tasks, undo, summary).
  - A dedicated “Keyboard Shortcuts” screen lists all commands, and menus accept them anywhere you’d normally enter a choice.
