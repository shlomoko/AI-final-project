package solver.csp.cspRowCol;

import javafx.scene.paint.Color;
import solver.csp.cspBlock.BlockParser;
import solver.csp.*;
import solver.csp.heuristics.value.ValueHeuristic;
import solver.csp.heuristics.variable.VariableHeuristic;
import solver.gui.Grid;

import java.io.File;
import java.util.List;

/**
 * Created by tmrlvi on 07/02/2016.
 */
public class RowColManager extends Manager {
    private Grid grid;
    private CSPSolver solver;

    public RowColManager(File file, Grid grid, VariableHeuristic varHeur, ValueHeuristic valueHeur, ConstraintHandler handler){
        handler.setManager(this);
        this.grid = grid;
        RowColParser parser = new RowColParser(file.getAbsolutePath());
        this.grid.setSize(parser.getColumns(), parser.getRows());
        this.grid.setHint(parser.getColumnHints(), parser.getRowHints());
        this.solver = new CSPSolver(parser.getVariables(), varHeur, valueHeur, handler, this);
    }

    public RowColManager(File file, Grid grid){
        this.grid = grid;
        BlockParser parser = new BlockParser(file.getAbsolutePath());
        this.grid.setSize(parser.getColumns(), parser.getRows());
        this.grid.setHint(parser.getColumnHints(), parser.getRowHints());

    }

    public void display(List<Variable> blockVariables){
        Color[][] colors = new Color[grid.getColumns()][grid.getRows()];
        for (int i = 0; i < grid.getColumns(); i++){
            for (int j = 0; j < grid.getRows(); j++){
                colors[i][j] = Color.GRAY;
            }
        }
        for (Variable basicVar : blockVariables){
            RowColVariable var = (RowColVariable) basicVar;
            if (var.getValue() != null) {
                if (var.isRow()) {
                    for (RowColVariable.Edges edge : (List<RowColVariable.Edges>)var.getValue()){
                        for (int i = edge.start; i < edge.end; i++) {
                            if (colors[i][var.getIndex()] != Color.GRAY) {
                                colors[i][var.getIndex()] = Color.BLACK;
                            } else {
                                colors[i][var.getIndex()] = Color.RED;
                            }
                        }
                    }
                } else {
                    for (RowColVariable.Edges edge : (List<RowColVariable.Edges>)var.getValue()){
                        for (int i = edge.start; i < edge.end; i++) {
                            if (colors[var.getIndex()][i] != Color.GRAY) {
                                colors[var.getIndex()][i] = Color.BLACK;
                            } else {
                                colors[var.getIndex()][i] = Color.GREEN;
                            }
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

    @Override
    public CSPSolver getSolver() {
        return solver;
    }

}
