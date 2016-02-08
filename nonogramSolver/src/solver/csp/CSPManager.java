package solver.csp;

import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import solver.csp.heuristics.ValueHeuristic;
import solver.csp.heuristics.VariableHeuristic;
import solver.gui.Grid;

import java.io.File;
import java.util.List;

/**
 * Created by tmrlvi on 07/02/2016.
 */
public class CSPManager extends Task<Void>{
    private Grid grid;
    private CSPSolver solver;
    private boolean running;

    public CSPManager(File file, Grid grid, VariableHeuristic varHeur, ValueHeuristic valueHeur){
        this.grid = grid;
        CSPParser parser = new CSPParser(file.getAbsolutePath());
        this.grid.setSize(parser.getColumns(), parser.getRows());
        this.grid.setHint(parser.getColumnHints(), parser.getRowHints());
        this.solver = new CSPSolver(parser.getVariables(), varHeur, valueHeur, this);
        running = false;
    }

    public void display(List<Variable> variables){
        Color[][] colors = new Color[grid.getColumns()][grid.getRows()];
        for (int i = 0; i < grid.getColumns(); i++){
            for (int j = 0; j < grid.getRows(); j++){
                colors[i][j] = Color.GRAY;
            }
        }
        for (Variable var : variables){
            if (var.getStartValue() != null) {
                if (var.isRow()) {
                    for (int i = 0; i < var.getLength() && var.getStartValue() + i < grid.getColumns(); i++) {
                        if (colors[var.getStartValue() + i][var.getIndex()] != Color.GRAY) {
                            colors[var.getStartValue() + i][var.getIndex()] = Color.BLACK;
                        } else {
                            colors[var.getStartValue() + i][var.getIndex()] = Color.RED;
                        }
                    }
                } else {
                    for (int i = 0; i < var.getLength() && var.getStartValue() + i < grid.getRows(); i++) {
                        if (colors[var.getIndex()][var.getStartValue() + i] != Color.GRAY) {
                            colors[var.getIndex()][var.getStartValue() + i] = Color.BLACK;
                        } else {
                            colors[var.getIndex()][var.getStartValue() + i] = Color.GREEN;
                        }
                    }
                }
            }
        }
        //TODO: implemenet pause
        grid.setColor(colors);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {}
    }

    public void pause(){
        running = false;
    }

    public void resume(){
        running = true;
    }

    @Override
    protected Void call() throws Exception {
        try {
            if (this.solver.backtracking(true)) {
                System.out.println("Success!");
            }
            display(this.solver.getVariables());
            System.out.println("done");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
