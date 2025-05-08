package manager;

import org.junit.jupiter.api.Test;


public interface HistoryManagerTest<T extends HistoryManager> {

    T createHistoryManager();

    @Test
    void add_shouldAddTasksToHistory();

    @Test
    void add_shouldNotAddDuplicates_onlyKeepLatest();

    @Test
    void remove_shouldRemoveTaskFromHistory();

    @Test
    void remove_shouldHandleRemovingNonExistingTask();

    @Test
    void getHistory_shouldReturnEmptyListWhenHistoryIsEmpty();

    @Test
    void remove_shouldRemoveFromBeginning();

    @Test
    void remove_shouldRemoveFromMiddle();

    @Test
    void remove_shouldRemoveFromEnd();

    @Test
    void removeAll_shouldClearHistory();
}
