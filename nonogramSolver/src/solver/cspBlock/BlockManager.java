package solver.cspBlock;

import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import solver.*;
import solver.gui.Grid;

import java.io.File;
import java.util.List;

/**
 * Created by tmrlvi on 07/02/2016.
 */
public class BlockManager extends Manager {
    private Grid grid;
    private CSPSolver solver;
    private boolean running;

    public BlockManager(File file, Grid grid, VariableHeuristic varHeur, ValueHeuristic valueHeur){
        this.grid = grid;
        BlockParser parser = new BlockParser(file.getAbsolutePath());
        this.grid.setSize(parser.getColumns(), parser.getRows());
        this.grid.setHint(parser.getColumnHints(), parser.getRowHints());
        this.solver = new CSPSolver(parser.getVariables(), varHeur, valueHeur, this);
        running = false;
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

    public void pause(){
        running = false;
    }

    public void resume(){
        running = true;
    }


    @Override
    protected Void call() throws Exception {
        try {
            //while (true) {
                if (this.solver.backtracking(true)) {
                    System.out.println("Success!");
                }
                display(this.solver.getVariables());
                System.out.println("done");
                //this.solver.restart();
            //}
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
