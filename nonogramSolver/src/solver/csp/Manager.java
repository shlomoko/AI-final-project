package solver.csp;

import javafx.concurrent.Task;
import solver.measure.Counters;

import java.util.List;

/**
 * Created by Zohar on 22/02/2016.
 */
public abstract class Manager extends Task<Void> {
    private boolean isRunning = false;

    public abstract void display(List<Variable> variables);
    public abstract CSPSolver getSolver();

    @Override
    protected Void call() throws Exception {
        try {
            isRunning = true;
            if (getSolver().solve()) {
                System.out.println("Success!");
            }
            display(getSolver().getVariables());
            System.out.println("done");

            Counters counters = Counters.getInstance();
            for (String key : counters.keySetCount()){
                System.out.println(key + ": " + counters.getCount(key) + " times.");
            }
            for (String key : counters.keySetTick()){
                System.out.println(key + ": " + counters.getAverageTicks(key) + " miliseconds on average,");
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
        isRunning = false;
        return null;
    }

    public boolean isStopped() {
        return !isRunning;
    }

    public void askToStop() {
        isRunning = false;
    }

}
