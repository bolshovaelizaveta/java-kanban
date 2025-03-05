package model;

    public class Subtask extends Task {
        private int epicId;

        public Subtask(String name, String description, TaskStatus status, int epicId) {
            super(name, description, status);
            this.epicId = epicId;
        }

        public int getEpicId() {
            return epicId;
        }

        public void setEpicId(int epicId) {
            this.epicId = epicId;
        }
    }