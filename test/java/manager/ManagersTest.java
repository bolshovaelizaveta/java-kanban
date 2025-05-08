package manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void getDefault_shouldReturnTaskManagerInstance() {
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(taskManager, "Managers.getDefault() должен возвращать экземпляр TaskManager.");
        assertTrue(taskManager instanceof InMemoryTaskManager, "Managers.getDefault() должен возвращать экземпляр InMemoryTaskManager по умолчанию.");
    }

    @Test
    void getDefaultHistory_shouldReturnHistoryManagerInstance() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "Managers.getDefaultHistory() должен возвращать экземпляр HistoryManager.");
        assertTrue(historyManager instanceof InMemoryHistoryManager, "Managers.getDefaultHistory() должен возвращать экзем экземпляр InMemoryHistoryManager по умолчанию.");
    }
}
