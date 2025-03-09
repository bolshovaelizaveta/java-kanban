package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {

    @Test
    void equals_shouldReturnTrue_whenTasksHaveSameId() {
        // Проверяем, что объекты Task считаются равными, если у них одинаковый ID
        Task task1 = new Task("Task1", "Description1", 1);
        Task task2 = new Task("Task2", "Description2", 1);

        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны."); // А вообще принципиально на каком языке вывод будет?
        // На магистратуре заставляют использовать искл англ, но мне удобнее на русском, поэтому выпендреж оставим в стороне, раз надо больше тестов... с:
    }

    @Test
    void equals_shouldReturnFalse_whenTasksHaveDifferentIds() {
        // Проверяем, что объекты Task считаются неравными, если у них разные ID
        Task task1 = new Task("Task1", "Description1", 1);
        Task task2 = new Task("Task2", "Description2", 2);

        assertNotEquals(task1, task2, "Задачи с разными ID не должны быть равны.");
    }
}