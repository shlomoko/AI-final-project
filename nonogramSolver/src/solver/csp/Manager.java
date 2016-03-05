package solver.csp;

import javafx.concurrent.Task;
import solver.csp.heuristics.value.ValueHeuristic;
import solver.csp.heuristics.variable.VariableHeuristic;
import solver.gui.Counters;
import solver.gui.UserInterface;

import java.util.List;

/**
 * Created by Zohar on 22/02/2016.
 */
public abstract class Manager extends Task<Void> {
    UserInterface ui;
    Integer rowAmount;
    Integer colAmount;
    CSPSolver solver;
    private boolean isRunning = false;

    public abstract void display();
    protected abstract List<? extends Variable> parserToVariable(NonogramParser parser);

    public Manager(NonogramParser parser, UserInterface ui, VariableHeuristic varHeur, ValueHeuristic valueHeur, ConstraintHandler handler){
        handler.setManager(this);
        this.ui = ui;
        this.rowAmount = parser.getRowAmount();
        this.colAmount = parser.getColAmount();
        this.solver = new CSPSolver(parserToVariable(parser), varHeur, valueHeur, handler, this);
    }

    public void report(String message){
        ui.report(message);
    }

    public CSPSolver getSolver(){
        return solver;
    }

    public int getRowAmount(){
        return rowAmount;
    }

    public int getColAmount(){
        return colAmount;
    }

    public UserInterface getUI(){
        return ui;
    }

    @Override
    protected Void call() throws Exception {
        try {
            isRunning = true;
            if (getSolver().solve()) {
                report("Success!");
            } else {
                report("Didn't find solution");
            }
            display();

            Counters counters = Counters.getInstance();
            for (String key : counters.keySetCount()){
                report(key + ": " + counters.getCount(key) + " times.");
            }
            for (String key : counters.keySetTick()){
                report(key + ": " + counters.getAverageTicks(key) + " miliseconds on average,");
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
