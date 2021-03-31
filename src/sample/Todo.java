package sample;

import java.io.Serializable;

public class Todo implements Serializable {
    private String todo;
    private boolean done = false;

    public Todo(String todo) {
        this.todo = todo;
    }

    public String getTodo() {
        return todo;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
