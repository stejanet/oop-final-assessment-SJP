package entities;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActivityLoggerObserver implements WorkItemObserver {
    private static final String LOG_FILE = "activityLog.log";
    private static final Logger logger = Logger.getLogger(ActivityLoggerObserver.class.getName());

    private void log(String message) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
            fw.write(LocalDateTime.now() + " " + message + System.lineSeparator());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to log activity", e);
        }
    }

    @Override
    public void onWorkItemAdded(WorkItem item) {
        log("Added: " + item.getClass().getSimpleName() + " ID=" + item.getID() + " Name=" + item.getName());
    }

    @Override
    public void onWorkItemDeleted(WorkItem item) {
        log("Deleted: " + item.getClass().getSimpleName() + " ID=" + item.getID() + " Name=" + item.getName());
    }
}