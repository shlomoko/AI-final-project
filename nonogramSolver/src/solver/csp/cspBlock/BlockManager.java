package solver.csp.cspBlock;

import javafx.scene.paint.Color;
import solver.csp.*;
import solver.csp.heuristics.value.ValueHeuristic;
import solver.csp.heuristics.variable.VariableHeuristic;
import solver.gui.Grid;

import java.io.File;
import java.util.List;

/**
 * Created by tmrlvi on 07/02/2016.
 */
public class BlockManager extends Manager {
    private Grid grid;
    private CSPSolver solver;

    public BlockManager(File file, Grid grid, VariableHeuristic varHeur, ValueHeuristic valueHeur, ConstraintHandler handler){
        handler.setManager(this);
        this.grid = grid;
        BlockParser parser = new BlockParser(file.getAbsolutePath());
        this.grid.setSize(parser.getColumns(), parser.getRows());
        this.grid.setHint(parser.getColumnHints(), parser.getRowHints());
        this.solver = new CSPSolver(parser.getVariables(), varHeur, valueHeur, handler, this);
    }

    public BlockManager(File file, Grid grid){
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
            BlockVariable var = (BlockVariable) basicVar;
            if (((Integer) var.getValue()) != null) {
                if (var.isRow()) {
                    for (int i = 0; i < var.getLength() && ((Integer) var.getValue()) + i < grid.getColumns(); i++) {
                        if (colors[((Integer) var.getValue()) + i][var.getIndex()] != Color.GRAY) {
                            colors[((Integer) var.getValue()) + i][var.getIndex()] = Color.BLACK;
                        } else {
                            colors[((Integer) var.getValue()) + i][var.getIndex()] = Color.RED;
                        }
                    }
                } else {
                    for (int i = 0; i < var.getLength() && ((Integer) var.getValue()) + i < grid.getRows(); i++) {
                        if (colors[var.getIndex()][((Integer) var.getValue()) + i] != Color.GRAY) {
                            colors[var.getIndex()][((Integer) var.getValue()) + i] = Color.BLACK;
                        } else {
                            colors[var.getIndex()][((Integer) var.getValue()) + i] = Color.GREEN;
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
