package manager;

import model.Task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
<<<<<<< HEAD
<<<<<<< HEAD
=======
=======
>>>>>>> 707e19a (Борьба с тестами)

>>>>>>> 707e19a (Борьба с тестами)
public class InMemoryHistoryManager implements HistoryManager {
    private static final int MAX_HISTORY_SIZE = 10;
    private final LinkedList<Task> history = new LinkedList<>();

    @Override
    public void add(Task task) {
<<<<<<< HEAD
<<<<<<< HEAD
=======
        history.removeIf(historyTask -> historyTask.getId() == task.getId());
>>>>>>> 707e19a (Борьба с тестами)
=======
        history.removeIf(historyTask -> historyTask.getId() == task.getId());
>>>>>>> 707e19a (Борьба с тестами)
        history.add(task);
        if (history.size() > MAX_HISTORY_SIZE) {
            history.removeFirst();
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
<<<<<<< HEAD
<<<<<<< HEAD
=======
=======
>>>>>>> 707e19a (Борьба с тестами)
    }

    @Override
    public void remove(int id) {
        history.removeIf(task -> task.getId() == id);
<<<<<<< HEAD
>>>>>>> 707e19a (Борьба с тестами)
=======
>>>>>>> 707e19a (Борьба с тестами)
    }
}