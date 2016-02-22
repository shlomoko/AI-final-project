package solver;

import javafx.concurrent.Task;

import java.util.List;

/**
 * Created by Zohar on 22/02/2016.
 */
public abstract class Manager extends Task<Void> {
    public abstract void display(List<Variable> variables);
}
