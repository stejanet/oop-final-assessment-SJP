package app;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;

import entities.*;
import enums.*;
import factories.WorkItemFactory;
import strategies.SortByID;
import strategies.SortByName;
import strategies.SortStrategy;
import validators.ExistingWorkItemValidator;
import validators.InputValidator;
import validators.IntegerValidator;
import validators.NotEmptyValidator;
import validators.NotTaskValidator;
import factories.WorkItemAbstractFactory;
import factories.DefaultWorkItemFactory;
import factories.AgileWorkItemFactory;

public class Main {
    private static WorkItemAbstractFactory workItemFactory = new DefaultWorkItemFactory();
    private static SortStrategy sortStrategy = new SortByID(); // Default strategy
    private static List<WorkItem> workItems = new ArrayList<>();
    private static final String DATA_FILE = "data.txt";
    private static Scanner scanner = new Scanner(System.in);
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    static {
        try {
            Logger rootLogger = Logger.getLogger("");
            
            // Remove default console handlers
            Arrays.stream(rootLogger.getHandlers()).forEach(rootLogger::removeHandler);

            // Add file handler
            java.util.logging.FileHandler fileHandler = new java.util.logging.FileHandler("application.log", true);
            fileHandler.setFormatter(new java.util.logging.SimpleFormatter());
            rootLogger.addHandler(fileHandler);
        } catch (IOException e) {
            System.err.println("Could not set up file logging: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            // Add observer
            WorkItem.addObserver(new ActivityLoggerObserver());

            // Load work items from data.txt
            loadData();
    
            System.out.println("Welcome to Project Manager! Type 'info' for commands.");
    
            // Read command
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
    
                if (input.equalsIgnoreCase("exit")) {
                    saveData();
                    System.out.println("Goodbye!");
                    break;
                } else if (input.equalsIgnoreCase("info")) {
                    showInfo();
                } else if (input.equalsIgnoreCase("list")) {
                    listWorkItems(true);
                } else if (input.equalsIgnoreCase("new")) {
                    createNewWorkItem();
                } else if (input.equalsIgnoreCase("delete")) {
                    deleteWorkItem();
                } else if (input.equalsIgnoreCase("sort id")) {
                    sortStrategy = new SortByID();
                    System.out.println("Sorting by ID.");
                } else if (input.equalsIgnoreCase("sort name")) {
                    sortStrategy = new SortByName();
                    System.out.println("Sorting by name.");
                } else if (input.equalsIgnoreCase("theme default")) {
                    workItemFactory = new DefaultWorkItemFactory();
                    System.out.println("Switched to Default theme.");
                } else if (input.equalsIgnoreCase("theme agile")) {
                    workItemFactory = new AgileWorkItemFactory();
                    System.out.println("Switched to Agile theme.");
                } else if (input.equalsIgnoreCase("quick epic")) {
                    createQuickEpic();
                } else if (input.equalsIgnoreCase("quick phase")) {
                    createQuickPhase();
                } else if (input.equalsIgnoreCase("quick feature")) {
                    createQuickFeature();
                } else if (input.equalsIgnoreCase("quick milestone")) {
                    createQuickMilestone();
                } else if (input.equalsIgnoreCase("quick task")) {
                    createQuickTask();
                } else if (input.equalsIgnoreCase("sync")) {
                    syncAllWorkItems();
                } else {
                    System.out.println("Unknown command. Type 'info' to see available commands.");
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "An unexpected error occurred.", e);
            System.out.println("An unexpected error occurred.");
        }
    }

    private static void showInfo() {
        System.out.println("Available commands:");
        System.out.println("- info            : Show this info");
        System.out.println("- list            : List all items");
        System.out.println("- new             : Create a new item");
        System.out.println("- delete          : Delete an existing item");
        System.out.println("- sort id         : Sort root epics by ID (children are always sorted by ID)");
        System.out.println("- sort name       : Sort root epics by name (children are always sorted by ID)");
        System.out.println("- theme default   : Use default work item creation");
        System.out.println("- theme agile     : Use agile work item creation");
        System.out.println("- quick epic      : Create a sample epic");
        System.out.println("- quick phase     : Create a sample phase");
        System.out.println("- quick feature   : Create a sample feature");
        System.out.println("- quick milestone : Create a sample milestone");
        System.out.println("- quick task      : Create a sample task");
        System.out.println("- sync            : Simulate syncing all work items to a server (multithreaded)");
        System.out.println("- exit            : Quit the program");
    }

    private static void listWorkItems(boolean printDetails) {
        List<WorkItem> roots = new ArrayList<>(workItems);
        sortStrategy.sort(roots);
        roots.stream()
            .filter(wi -> wi instanceof Epic)
            .forEach(wi -> wi.printHierarchy(0, printDetails));
    }

    private static void createNewWorkItem() {
        int type = 0;
        String name;
        WorkItemStatus status = null;
        
        // Prompt for type
        while (true) {
            System.out.println("Choose type: [1] Epic, [2] Phase, [3] Feature, [4] Milestone, [5] Task");
            String input = scanner.nextLine().trim();
            try {
                type = Integer.parseInt(input);
                if (type >= 1 && type <= 5) break;
            } catch (NumberFormatException ignored) {}
            System.out.println("Invalid choice. Please enter a number between 1 and 5.");
        }

        // Prompt for status
        while (true) {
            System.out.println("Enter status: [1] Not Started, [2] In Progress, [3] Completed");
            System.err.println("(Default status: " + workItemFactory.getDefaultWorkItemStatus().getDisplayName() + ")");
            String s = scanner.nextLine().trim();
            if (s.equals("1")) {
                status = WorkItemStatus.NOT_STARTED;
                break;
            } else if (s.equals("2")) {
                status = WorkItemStatus.IN_PROGRESS;
                break;
            } else if (s.equals("3")) {
                status = WorkItemStatus.COMPLETED;
                break;
            } else if (s.isEmpty()) {
                break; // Will be handled by the abstract factory, depending if default or agile
            } else {
                System.out.println("Invalid status. Try again.");
            }
        }

        // Prompt for parent selection if not Epic
        WorkItem parent = null;
        if (type != 1) {
            listWorkItems(false);

            // Build the validation chain
            InputValidator validator = new NotEmptyValidator();
            validator.linkWith(new IntegerValidator())
                    .linkWith(new ExistingWorkItemValidator(workItems))
                    .linkWith(new NotTaskValidator(workItems));

            while (true) {
                System.err.println();
                System.out.println("Enter parent ID (must not be a Task):");
                String pid = scanner.nextLine().trim();

                if (validator.validate(pid)) {
                    int parentId = Integer.parseInt(pid);
                    parent = WorkItem.findById(workItems, parentId);
                    break;
                }
            }
        }

        // Prompt for name
        name = promptForString("Enter name (at least 3 characters):", 3);

        // Create based on type and ask further details
        switch (type) {
            case 1: // ------ Epic
                Epic epic = (Epic) workItemFactory.createEpic();
                epic.setID(WorkItem.getNextID(workItems));
                if (status != null) {
                    epic.setStatus(status);
                }
                epic.setName(name);
                
                // Prompt for description
                epic.setDescription(promptForString("Enter description (optional, leave blank for none):", 0));

                // Prompt for start date
                epic.setStartDate(
                    promptForLocalDate("Enter start date YYYY-MM-DD (optional, leave blank for none):", 
                    true, epic.getStartDate())
                );

                // Prompt for end date
                epic.setEndDate(
                    promptForLocalDate("Enter end date YYYY-MM-DD (optional, leave blank for none):", 
                    true, epic.getEndDate())
                );

                // Add to collection and save
                workItems.add(epic);
                WorkItem.notifyWorkItemAdded(epic);
                break;
            case 2: // ------ Phase
                Phase phase = workItemFactory.createPhase();
                phase.setID(WorkItem.getNextID(workItems));
                if (status != null) {
                    phase.setStatus(status);
                }
                phase.setName(name);
                
                // Prompt for phase type
                while (true) {
                    System.out.println("Enter phase type: [1] Research & Development, [2] Project Management, [3] Development");
                    String s = scanner.nextLine().trim();
                    if (s.isEmpty() || s.equals("1")) {
                        phase.setPhaseType(PhaseType.R_AND_D);
                        break;
                    } else if (s.equals("2")) {
                        phase.setPhaseType(PhaseType.PROJECT_MANAGEMENT);
                        break;
                    } else if (s.equals("3")) {
                        phase.setPhaseType(PhaseType.DEVELOPMENT);
                        break;
                    } else {
                        System.out.println("Invalid status. Try again.");
                    }
                }
                
                // Prompt for start date
                phase.setStartDate(
                    promptForLocalDate("Enter start date YYYY-MM-DD (optional, leave blank for none):", 
                    true, phase.getStartDate())
                );

                // Prompt for end date
                phase.setEndDate(
                    promptForLocalDate("Enter end date YYYY-MM-DD (optional, leave blank for none):", 
                    true, phase.getEndDate())
                );

                // Add to collection and save
                parent.addChild(phase);
                WorkItem.notifyWorkItemAdded(phase);
                break;
            case 3: // ------ Feature
                Feature feature = workItemFactory.createFeature();
                feature.setID(WorkItem.getNextID(workItems));
                if (status != null) {
                    feature.setStatus(status);
                }
                feature.setName(name);
                
                // Prompt for description
                feature.setDescription(
                    promptForString("Enter description (optional, leave blank for none):",
                    0)
                );

                // Prompt for estimated hours
                feature.setEstimatedHours(
                    promptForDouble("Enter estimated hours (optional, leave blank for none):", 
                    true)
                );

                // Add to collection and save
                parent.addChild(feature);
                WorkItem.notifyWorkItemAdded(feature);
                break;
            case 4: // ------ Milestone
                Milestone milestone = workItemFactory.createMilestone();
                milestone.setID(WorkItem.getNextID(workItems));
                if (status != null) {
                    milestone.setStatus(status);
                }
                milestone.setName(name);
                
                // Prompt for due date
                milestone.setDueDate(
                    promptForLocalDate("Enter due date YYYY-MM-DD (optional, leave blank for none):", 
                    true, null)
                );

                // Add to collection and save
                parent.addChild(milestone);
                WorkItem.notifyWorkItemAdded(milestone);
                break;
            case 5: // ------ Task
                Task task = workItemFactory.createTask();
                task.setID(WorkItem.getNextID(workItems));
                if (status != null) {
                    task.setStatus(status);
                }
                task.setName(name);
                                
                // Prompt for description
                task.setDescription(
                    promptForString("Enter description (optional, leave blank for none):", 
                    0)
                );

                // Prompt for due date
                task.setDueDate(
                    promptForLocalDate("Enter due date YYYY-MM-DD (optional, leave blank for none):", 
                    true, null)
                );

                // Prompt for estimated hours
                task.setEstimatedHours(
                    promptForDouble("Enter estimated hours (optional, leave blank for none):", 
                    true)
                );

                // Add to collection and save
                parent.addChild(task);
                WorkItem.notifyWorkItemAdded(task);
                break;
        }

        // Save data
        saveData();

        System.out.println("Work item created!");
    }

    private static void deleteWorkItem() {
        // Show all work items
        listWorkItems(false);

        WorkItem toDelete = null;
        while (toDelete == null) {
            System.out.println("Enter the ID of the work item to delete:");
            String input = scanner.nextLine().trim();
            int id;
            try {
                id = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                logger.log(Level.WARNING, "Invalid ID input for deletion: " + input, e);
                System.out.println("Invalid input. Please enter a valid integer ID.");
                continue;
            }
            toDelete = WorkItem.findById(workItems, id);
            if (toDelete == null) {
                System.out.println("No work item found with ID " + id + ". Please try again.");
            }
        }

        // Remove from parent if not a root item
        boolean removed = false;
        for (WorkItem root : workItems) {
            if (root == toDelete) {
                workItems.remove(root);
                notifyDeletionRecursive(root);
                removed = true;
                break;
            } else {
                removed = removeFromChildren(root, toDelete);
                if (removed) break;
            }
        }

        if (removed) {
            System.out.println("Work Item ID " + toDelete.getID() + " has been deleted.");
            saveData();
        } else {
            System.out.println("Failed to delete work item.");
        }
    }

    private static boolean removeFromChildren(WorkItem parent, WorkItem toDelete) {
        List<WorkItem> children = parent.getChildren();
        if (children.remove(toDelete)) {
            notifyDeletionRecursive(toDelete);
            return true;
        }
        for (WorkItem child : children) {
            if (removeFromChildren(child, toDelete)) {
                return true;
            }
        }
        return false;
    }

    private static void notifyDeletionRecursive(WorkItem item) {
        for (WorkItem child : item.getChildren()) {
            notifyDeletionRecursive(child);
        }
        WorkItem.notifyWorkItemDeleted(item);
    }

    private static void createQuickEpic() {
        Epic epic = Epic.builder()
            .setName("Quick Epic")
            .setStatus(workItemFactory.getDefaultWorkItemStatus())
            .setDescription("This is a sample epic")
            .setStartDate(LocalDate.now())
            .setEndDate(LocalDate.now().plusDays(60))
            .build();
        epic.setID(WorkItem.getNextID(workItems));
        workItems.add(epic);
        WorkItem.notifyWorkItemAdded(epic);
        saveData();
        System.out.println("Quick epic created with Builder!");
    }

    private static void createQuickPhase() {
        // Prompt for parent selection
        WorkItem parent = null;
        listWorkItems(false);
        
        // Build the validation chain
        InputValidator validator = new NotEmptyValidator();
        validator.linkWith(new IntegerValidator())
                .linkWith(new ExistingWorkItemValidator(workItems))
                .linkWith(new NotTaskValidator(workItems));

        while (true) {
            System.err.println();
            System.out.println("Enter parent ID (must not be a Task):");
            String pid = scanner.nextLine().trim();

            if (validator.validate(pid)) {
                int parentId = Integer.parseInt(pid);
                parent = WorkItem.findById(workItems, parentId);
                break;
            }
        }

        Phase phase = Phase.builder()
            .setName("Quick Phase")
            .setStatus(workItemFactory.getDefaultWorkItemStatus())
            .setPhaseType(PhaseType.DEVELOPMENT)
            .setStartDate(LocalDate.now())
            .setEndDate(LocalDate.now().plusDays(14))
            .build();
        phase.setID(WorkItem.getNextID(workItems));
        parent.addChild(phase);
        WorkItem.notifyWorkItemAdded(phase);
        saveData();
        System.out.println("Quick phase created with Builder!");
    }

    private static void createQuickFeature() {
        // Prompt for parent selection
        WorkItem parent = null;
        listWorkItems(false);
        
        // Build the validation chain
        InputValidator validator = new NotEmptyValidator();
        validator.linkWith(new IntegerValidator())
                .linkWith(new ExistingWorkItemValidator(workItems))
                .linkWith(new NotTaskValidator(workItems));

        while (true) {
            System.err.println();
            System.out.println("Enter parent ID (must not be a Task):");
            String pid = scanner.nextLine().trim();

            if (validator.validate(pid)) {
                int parentId = Integer.parseInt(pid);
                parent = WorkItem.findById(workItems, parentId);
                break;
            }
        }

        Feature feature = Feature.builder()
            .setName("Quick Feature")
            .setStatus(workItemFactory.getDefaultWorkItemStatus())
            .setDescription("This is a sample feature")
            .setEstimatedHours(7.0)
            .build();
        feature.setID(WorkItem.getNextID(workItems));
        parent.addChild(feature);
        WorkItem.notifyWorkItemAdded(feature);
        saveData();
        System.out.println("Quick feature created with Builder!");
    }

    private static void createQuickMilestone() {
        // Prompt for parent selection
        WorkItem parent = null;
        listWorkItems(false);
        
        // Build the validation chain
        InputValidator validator = new NotEmptyValidator();
        validator.linkWith(new IntegerValidator())
                .linkWith(new ExistingWorkItemValidator(workItems))
                .linkWith(new NotTaskValidator(workItems));

        while (true) {
            System.err.println();
            System.out.println("Enter parent ID (must not be a Task):");
            String pid = scanner.nextLine().trim();

            if (validator.validate(pid)) {
                int parentId = Integer.parseInt(pid);
                parent = WorkItem.findById(workItems, parentId);
                break;
            }
        }

        Milestone milestone = Milestone.builder()
            .setName("Quick Milestone")
            .setStatus(workItemFactory.getDefaultWorkItemStatus())
            .setDueDate(LocalDate.now().plusDays(28))
            .build();
        milestone.setID(WorkItem.getNextID(workItems));
        parent.addChild(milestone);
        WorkItem.notifyWorkItemAdded(milestone);
        saveData();
        System.out.println("Quick milestone created with Builder!");
    }

    private static void createQuickTask() {
        // Prompt for parent selection
        WorkItem parent = null;
        
        // Build the validation chain
        InputValidator validator = new NotEmptyValidator();
        validator.linkWith(new IntegerValidator())
                .linkWith(new ExistingWorkItemValidator(workItems))
                .linkWith(new NotTaskValidator(workItems));

        while (true) {
            System.err.println();
            System.out.println("Enter parent ID (must not be a Task):");
            String pid = scanner.nextLine().trim();

            if (validator.validate(pid)) {
                int parentId = Integer.parseInt(pid);
                parent = WorkItem.findById(workItems, parentId);
                break;
            }
        }

        Task task = Task.builder()
            .setName("Quick Task")
            .setStatus(workItemFactory.getDefaultWorkItemStatus())
            .setDescription("This is a sample task")
            .setDueDate(LocalDate.now().plusDays(3))
            .setEstimatedHours(2.0)
            .build();
        task.setID(WorkItem.getNextID(workItems));
        parent.addChild(task);
        WorkItem.notifyWorkItemAdded(task);
        saveData();
        System.out.println("Quick task created with Builder!");
    }

    private static String promptForString(String prompt, int minLength) {
        // Prompt
        System.out.println(prompt);
        String result = null;
        
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    if (minLength == 0) {
                        result = null;
                        break;
                    }
                    System.out.println("Please insert a value:");
                }
                else if (input.length() < minLength) {
                    System.out.println("Input must be at least " + minLength + " characters. Try again:");
                } else {
                    result = input;
                    break;
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error occurred while reading String input.", e);
                System.out.println("Invalid string. Please try again.");
            }
        }

        return result;
    }

    private static LocalDate promptForLocalDate(String prompt, boolean isOptional, LocalDate defaultValue) {
        // Prompt
        System.out.println(prompt);
        LocalDate date = defaultValue;

        while (true) {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                if (isOptional) {
                    date = defaultValue;
                    break;
                }
                System.out.println("Please insert a value:");
            }
            else {
                try {
                    date = LocalDate.parse(input);
                    break;
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error occurred while reading LocalDate input.", e);
                    System.out.println("Invalid date format. Please enter in YYYY-MM-DD format" + (isOptional ? " or leave blank" : "") + ":");
                }
            }
        }

        return date;
    }

    private static Double promptForDouble(String prompt, boolean isOptional) {
        // Prompt
        System.out.println(prompt);
        Double value = null;

        while (true) {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                if (isOptional) {
                    value = null;
                    break;
                }
                System.out.println("Please insert a value:");
            }
            else {
                try {
                    value = Double.parseDouble(input);
                    break;
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error occurred while reading Double input.", e);
                    System.out.println("Invalid number. Please enter a decimal number" + (isOptional ? " or leave blank" : "") + ":");
                }
            }
        }

        return value;
    }

    private static void syncAllWorkItems() {
        // Gather all work items (roots and children) in flat list
        List<WorkItem> allItems = new ArrayList<>();
        for (WorkItem root : workItems) {
            for (WorkItem wi : root) {
                allItems.add(wi);
            }
        }
        if (allItems.isEmpty()) {
            System.out.println("No work items to sync.");
            return;
        }

        System.out.println("Starting sync of " + allItems.size() + " work items...");

        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(allItems.size());
        ConcurrentHashMap<WorkItem, Boolean> synced = new ConcurrentHashMap<>();

        for (WorkItem wi : allItems) {
            executor.submit(() -> {
                try {
                    int seconds = 1 + new Random().nextInt(3); // 1 to 3 seconds random time
                    Thread.sleep(seconds * 1000L);
                    synced.put(wi, true);
                    int done = synced.size();
                    int total = allItems.size();
                    int percent = (int) ((done * 100.0) / total);
                    System.out.println("Synced: " + wi.getClass().getSimpleName() + " ID=" + wi.getID() + " (" + percent + "% done)");
                } catch (InterruptedException ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            while (!latch.await(1, TimeUnit.SECONDS)) {
                int done = synced.size();
                int total = allItems.size();
                int percent = (int) ((done * 100.0) / total);
                System.out.println("Progress: " + percent + "% (" + done + "/" + total + ")");
            }
            System.out.println("Sync complete!");
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "An error occurred during the sync.", e);
            System.out.println("Sync interrupted.");
        } finally {
            executor.shutdownNow();
        }
    }

    private static void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            System.out.println("No existing data found. Starting fresh.");
            return;
        }

        try (Scanner fileScanner = new Scanner(file)) {
            Map<WorkItem, Integer> allItems = new HashMap<>();

            // Parse each line into a WorkItem, store in a local list
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                Integer parentID = WorkItem.getParentIdFromLine(line);

                WorkItem parsedItem = WorkItemFactory.createWorkItem(line);
                if (parsedItem != null) {
                    allItems.put(parsedItem, parentID);
                }
            }

            // Populate global list with parent-child relationships
            for (Map.Entry<WorkItem, Integer> item : allItems.entrySet()) {
                Integer parentId = item.getValue();
                if (parentId != null) {
                    WorkItem parent = allItems.keySet().stream()
                            .filter(wi -> wi.getID().equals(parentId))
                            .findFirst()
                            .orElse(null);
                    // Parent can't be null and can't be a Task
                    if (parent != null && !(parent instanceof Task)) {
                        // Prevent circular reference
                        if (!WorkItem.wouldCreateCycle(parent, item.getKey())) {
                            parent.addChild(item.getKey());
                        } else {
                            logger.log(Level.WARNING, "Skipped circular reference for item ID " + item.getKey().getID());  
                            System.out.println("Warning: Skipped circular reference for item ID " + item.getKey().getID());
                        }
                    } else {
                        logger.log(Level.WARNING, "Invalid Parent ID, skipped item ID " + item.getKey().getID());  
                        System.out.println("Warning: Invalid Parent ID, skipped item ID " + item.getKey().getID());
                    }
                } else {
                    workItems.add(item.getKey());
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load data", e); // Internal log
            System.out.println("An error occurred while loading data. Please try again."); // User-friendly
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error during data load", e);
            System.out.println("An unexpected error occurred. Please contact support.");
        }
    }

    private static void saveData() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
            Set<Integer> visited = new HashSet<>();
            for (WorkItem wi : workItems) {
                saveWorkItemRecursive(wi, null, writer, visited); // null for root parent
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save data", e);
            System.out.println("An error occurred while saving data. Please try again.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error during data save", e);
            System.out.println("An unexpected error occurred. Please contact support.");
        }
    }

    private static void saveWorkItemRecursive(WorkItem wi, Integer parentId, PrintWriter writer, Set<Integer> visited) {
        if (wi == null || !visited.add(wi.getID())) return;
        writer.println(wi.serialize(parentId));
        for (WorkItem child : wi.getChildren()) {
            saveWorkItemRecursive(child, wi.getID(), writer, visited);
        }
    }
}
