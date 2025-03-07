package model;

import model.Epic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class EpicTest {
    @Test
    void equals_shouldReturnTrue_whenEpicsHaveSameId() {
        // Проверяем, что объекты Epic считаются равными, если у них одинаковый ID
        Epic epic1 = new Epic("Epic1", "Description1", 1);
        Epic epic2 = new Epic("Epic2", "Description2", 1);

        assertEquals(epic1, epic2, "Эпики с одинаковым ID должны быть равны.");
    }

    @Test
    void equals_shouldReturnFalse_whenEpicsHaveDifferentIds() {
        // Проверяем, что объекты Epic считаются неравными, если у них разные ID
        Epic epic1 = new Epic("Epic1", "Description1", 1);
        Epic epic2 = new Epic("Epic2", "Description2", 2);

        assertNotEquals(epic1, epic2, "Эпики с разными ID не должны быть равны.");
    }
}
