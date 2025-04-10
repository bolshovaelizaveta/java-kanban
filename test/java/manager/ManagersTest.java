package manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ManagersTest {
    @Test
    void getDefault_shouldReturnNonNullTaskManager() {
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(taskManager, "getDefault() должен возвращать не null TaskManager.");
    }

    @Test
    void getDefaultHistory_shouldReturnNonNullHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "getDefaultHistory() должен возвращать не null HistoryManager.");
    }
}
