package solver.csp;

import javafx.scene.paint.Color;
import solver.csp.heuristics.ValueHeuristic;
import solver.csp.heuristics.VariableHeuristic;
import solver.gui.Grid;

import java.io.File;
import java.util.List;

/**
 * Created by tmrlvi on 07/02/2016.
 */
public class CSPManager {
    private Grid grid;
    private CSPSolver solver;
    private boolean running;

    public CSPManager(File file, Grid grid, VariableHeuristic varHeur, ValueHeuristic valueHeur){
        List<Variable> variables = new CSPParser().parser(file.getName());
        this.solver = new CSPSolver(variables, varHeur, valueHeur, this);
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
            if (var.isRow()){
                for (int i=0; i < var.getLength(); i++ ){
                    if (colors[var.getStartValue() + i][var.getIndex()] != Color.GRAY){
                        colors[var.getStartValue() + i][var.getIndex()] = Color.BLACK;
                    } else{
                        colors[var.getStartValue() + i][var.getIndex()] = Color.RED;
                    }
                }
            } else {
                for (int i=0; i < var.getLength(); i++ ){
                    if (colors[var.getIndex()][var.getStartValue() + i] != Color.GRAY){
                        colors[var.getIndex()][var.getStartValue() + i] = Color.BLACK;
                    } else{
                        colors[var.getIndex()][var.getStartValue() + i] = Color.GREEN;
                    }
                }
            }
        }
        //TODO: find a better way to implement pause
        while (!running){
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {}
        }
        grid.setColor(colors);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}
    }

    public void start(){
        running = true;
        if (this.solver.backtracking()){
            display(this.solver.getVariables());
        }
    }

    public void pause(){
        running = false;
    }

    public void resume(){
        running = true;
    }
}
