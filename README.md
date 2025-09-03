# Project Manager Application
_Stefany Janet Piccaro_

## Application overview and functionality
This is a project manager application that allows the user to create and organize different types of work items: Epics, Phases, Features, Milestones and Tasks. 

A few rules apply:
- Epics are at the root - only epics can have no parent work item
- Tasks can't have children - they are leaf work items
- Circular references are not permitted - i.e. a task cannot have itself as parent, otherwise the traversal of the list would cause exceptions

Whenever a work item is added or deleted, it will be tracked in the activityLog.log file.

Whenever an exception occurs, it will be logged in the application.log file.

**The application exposes the following commands:**
- info: Show available commands info
- list: List all items
- new: Create a new item
- delete: Delete an existing item
- sort id: Sort root epics by ID (children are always sorted by ID)
- sort name: Sort root epics by name (children are always sorted by ID)
- theme default: Use default work item creation (when working in Default mode, work items have a default status of "Not Started" and start dates are set to null)
- theme agile: Use agile work item creation (when working in Agile mode, work items have a default status of "In Progress" and start dates are set to the current date as default)
- quick epic: Create a sample epic
- quick phase: Create a sample phase
- quick feature: Create a sample feature
- quick milestone: Create a sample milestone
- quick task: Create a sample task
- sync: Simulate syncing all work items to a hypotetical server (to showcase multithreading)
- exit: Quit the program

Work items are stored in a data.txt file. 

## Technologies and patterns used

### Design Patterns
- **Factory**: Used to create work items from file data, encapsulating object creation logic and supporting easy extension for new types.
- **Composite**: Enables hierarchical organization of work items (epics, phases, features, etc.), allowing parent-child relationships and recursive operations.
- **Iterator**: Allows traversal of all work items (including nested children) using enhanced for-loops, simplifying hierarchy navigation.
- **Exception Shielding**: Input parsing and file operations are wrapped with try-catch blocks to prevent application crashes and provide user-friendly error messages.
- **Abstract Factory**: Provides interchangeable factories (Default and Agile) for creating families of work items with different default properties.
- **Builder**: Simplifies the construction of complex work items with optional fields, improving code readability and flexibility.
- **Strategy**: Enables dynamic selection of sorting algorithms for listing work items (by ID or by name) at runtime.
- **Observer**: Implements activity logging by notifying observers whenever work items are added or deleted, supporting extensible event handling.
- **Chain of Responsibility**: Structures input validation as a chain of validators, allowing flexible and reusable validation logic for user input.

### Technologies
- **Collections Framework**: Uses lists and sets to manage and organize work items and their relationships efficiently
- **Generics**: Ensures type safety and code reusability across collections and builder patterns.
- **Java I/O**: Handles reading from and writing to files for persistent storage of work items and activity logs.
- **Logging**: Utilizes Java's logging framework to record significant application events and errors for debugging and auditing.
- **JUnit Testing**: Provides automated unit tests for work item creation, serialization, and parsing to ensure code correctness.
- **Multithreading**: Demonstrates concurrent processing by simulating parallel syncing of work items, showcasing thread pools and progress reporting.
- **Custom Parsing and Serialization**:
The application implements custom parsing and serialization methods for all work item types. This approach ensures full control over how objects are saved to and loaded from the data.txt file, allowing for a human-readable, extensible, and robust file format. It also makes it easy to handle optional fields, maintain backward compatibility, and gracefully manage invalid or missing data during file operations.


## c. Setup and execution instructions
Open the project in VS Code.

Go to the "Run and Debug" section (from the left handside navbar) and click on "Launch Main" to launch the project with the correct VS Code settings that I have included as part of this project (.vscode).

When the application starts, I would suggest to:
1. launch the "info" command to see all available actions
2. Use the "list" command to view the sample data I have created, so that the application functionality becomes a bit more intuitive

The class files will be compiled in the /bin folder at the root of this project.

JUnit tests can be run in VS Code at the "Testing" section. 


## UML diagrams (class + architectural)

### UML Class Diagram

#### **Entities**

**_WorkItem (abstract)_**
- id
- name
- status
- children
- addChild(WorkItem)
- removeChild(WorkItem)
- serialize()
- parse(String)
- iterator()

**_Epic (extends WorkItem)_**
- description
- startDate
- endDate
- builder()

**_Phase (extends WorkItem)_**
- phaseType
- startDate
- endDate;
- builder()

**_Feature (extends WorkItem)_**
- description
- estimatedHours
- builder()

**_Milestone (extends WorkItem)_**
- dueDate
- builder()

**_Task (extends WorkItem)_**
- description
- dueDate
- estimatedHours
- builder()

**_WorkItemObserver (interface)_**
- onWorkItemAdded(WorkItem)
- onWorkItemDeleted(WorkItem)

**_ActivityLoggerObserver_** 
- implements WorkItemObserver

#### **Factories**
**_WorkItemAbstractFactory (interface)_**
- createEpic()
- createPhase()
- createFeature()
- createMilestone()
- createTask()
- getDefaultWorkItemStatus()
- getDefaultStartDate()

**_DefaultWorkItemFactory_**
- implements WorkItemAbstractFactory

**_AgileWorkItemFactory_**
- implements WorkItemAbstractFactory

**_WorkItemFactory_**
- static factory for parsing

####**Strategies**

**_SortStrategy (interface)_**
- sort(List<WorkItem>)

**_SortByID_**
- implements SortStrategy

**_SortByName_**
- implements SortStrategy

#### **Validators**

**_InputValidator (abstract)_**
- linkWith(InputValidator)

**_NotEmptyValidator_** 
- extends InputValidator

**_IntegerValidator_** 
- extends InputValidator

**_ExistingWorkItemValidator_** 
- extends InputValidator

**_NotTaskValidator_** 
- extends InputValidator

#### **Utilities**
**_ParseUtils_** 
- static parsing helpers

**_Main_**
- application entrypoint


### Architectural Diagram

**User Interface**: command line interface, work items are displayed in hierarchy to help the user visualise the work items structure.

**Storage**: Work items are stored in a data.txt file to ensure persistend and reliable access across multiple runs of the application.

**Activity Log**: Added and deleted work items are logged in the activityLog.log file to allow the user to review old activity.ù

**Application Log**: exceptions are tracked in the application.log file to shield the user from viewing stack traces while still keeping track and having visibility of any potential issue. 

**Code structure**: I have designed the application with the objective of keeping the codebase clean, by splitting it in different packages that each provide their own functionality.

**Test suite**: the JUnit tests aim to test work items creation and serialization, in aspects such as default fields, consistent parsing and correct serialization. 

## Known limitations and future work

### Offline-only operation
The application works entirely offline and does not support real-time collaboration or remote access.

### No concurrency control
Multiple users or processes accessing the data files simultaneously could cause data corruption; a database or file locking would be needed for safe concurrent use.

### No editing
At the moment, the Project Manager application only allows for the creation and deletion of work items. Implementing an edit feature would enhance the user experience. 

### No user authentication
Adding user authentication and permissions would allow users to operate on different levels of authority and enhance security.

### Time logging
Time logging features would enhance the user experience by allowing users to keep track of spent vs estimated time.

### No graphical user interface
The application is command-line only; a future version could provide a GUI or web interface for better usability.

### Scalability
Performance may degrade with a large number of work items due to in-memory and file-based storage; migrating to a database would improve scalability.
