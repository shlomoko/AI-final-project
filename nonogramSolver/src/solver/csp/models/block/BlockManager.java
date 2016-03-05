package solver.csp.models.block;

import javafx.scene.paint.Color;
import solver.csp.*;
import solver.csp.heuristics.value.ValueHeuristic;
import solver.csp.heuristics.variable.VariableHeuristic;
import solver.csp.models.block.constraints.IntersectConstraint;
import solver.csp.models.block.constraints.OrderConstraint;
import solver.gui.UserInterface;

import java.util.*;

/**
 * Created by tmrlvi on 07/02/2016.
 */
public class BlockManager extends Manager {
    private List<List<BlockVariable>> rowVariables;
    private List<List<BlockVariable>> colVariables;

    public BlockManager(NonogramParser parser, UserInterface ui, VariableHeuristic varHeur, ValueHeuristic valueHeur, ConstraintHandler handler) {
        super(parser, ui, varHeur, valueHeur, handler);
    }

    @Override
    protected List<? extends Variable> parserToVariable(NonogramParser parser) {
        List<BlockVariable> variables = new LinkedList<BlockVariable>();
        rowVariables = new ArrayList<List<BlockVariable>>();
        colVariables = new ArrayList<List<BlockVariable>>();

        int[][] hints = parser.getRowHints();
        for (int i = 0; i < hints.length; i++){
            rowVariables.add(new ArrayList<BlockVariable>());
            for (int j = 0; j < hints[i].length; j++) {
                if (hints[i][j] > 0) {
                    BlockVariable var = new BlockVariable(hints[i][j], true, i, getColAmount() - hints[i][j] + 1);
                    variables.add(var);
                    rowVariables.get(i).add(var);
                }
            }
        }

        hints = parser.getColumnHints();
        for (int i = 0; i < hints.length; i++){
            colVariables.add(new ArrayList<BlockVariable>());
            for (int j = 0; j < hints[i].length; j++) {
                if (hints[i][j] > 0) {
                    BlockVariable var = new BlockVariable(hints[i][j], false, i, getRowAmount() - hints[i][j] + 1);
                    variables.add(var);
                    colVariables.get(i).add(var);
                }
            }
        }
        addOrderConstraint(true);
        addOrderConstraint(false);
        addRowSum(true);
        addRowSum(false);
        addIntersectConstraint();
        return variables;
    }

    private void addOrderConstraint(boolean isRow){
        int dim = isRow ? getRowAmount() : getColAmount();
        List<List<BlockVariable>> variableList = isRow ? rowVariables : colVariables;
        for (int i = 0; i< dim; i++){
            List<BlockVariable> currRow = variableList.get(i);
            for(int j=0; j<currRow.size()-1; j++){
                Constraint constraint = new OrderConstraint(currRow.get(j), currRow.get(j+1));
                currRow.get(j).addConstraint(constraint);
                currRow.get(j+1).addConstraint(constraint);
            }
        }
    }

    private void addRowSum(boolean isRow){
        final Map<Integer, Integer> rowMap = new HashMap<Integer, Integer>();
        List<Integer> rowSum = new ArrayList<Integer>();
        int dim = isRow ? getRowAmount() : getColAmount();
        int otherDim = isRow ? getColAmount() : getRowAmount();
        List<List<BlockVariable>> variableList = isRow ? rowVariables : colVariables;
        List<List<BlockVariable>> otherVariableList = isRow ? colVariables : rowVariables;
        for (int i = 0; i< dim; i++){
            List<BlockVariable> currRow = variableList.get(i);
            int sum = 0;
            for (BlockVariable var : currRow){
                sum += var.getLength();
            }
            rowMap.put(i,sum);
            rowSum.add(i);
        }
        Collections.sort(rowSum, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return rowMap.get(o2) - rowMap.get(o1);
            }
        });

        for (int i=0; i<otherDim; i++) {
            List<BlockVariable> currRow = otherVariableList.get(i);
            for (BlockVariable var : currRow) {
                var.addRowSum(rowSum);
            }
        }
    }

    private void addIntersectConstraint(){
        for(int i = 0; i< getRowAmount(); i++){
            for (int j = 0; j< getColAmount(); j++){
                List<BlockVariable> currRow = rowVariables.get(i);
                List<BlockVariable> currCol = colVariables.get(j);
                Constraint constraint = new IntersectConstraint(i, j, currRow, currCol);
                for(BlockVariable rowVar : currRow){
                    rowVar.addConstraint(constraint);
                }
                for(BlockVariable colVar :currCol){
                    colVar.addConstraint(constraint);
                }
            }
        }
    }

    @Override
    public void displayVariableState(){
        Color[][] colors = new Color[getColAmount()][getRowAmount()];
        for (int i = 0; i < getColAmount(); i++){
            for (int j = 0; j < getRowAmount(); j++){
                colors[i][j] = Color.GRAY;
            }
        }
        for (Variable basicVar : getSolver().getVariables()){
            BlockVariable var = (BlockVariable) basicVar;
            if (((Integer) var.getValue()) != null) {
                if (var.isRow()) {
                    for (int i = 0; i < var.getLength() && ((Integer) var.getValue()) + i < getColAmount(); i++) {
                        if (colors[((Integer) var.getValue()) + i][var.getIndex()] != Color.GRAY) {
                            colors[((Integer) var.getValue()) + i][var.getIndex()] = Color.BLACK;
                        } else {
                            colors[((Integer) var.getValue()) + i][var.getIndex()] = Color.RED;
                        }
                    }
                } else {
                    for (int i = 0; i < var.getLength() && ((Integer) var.getValue()) + i < getRowAmount(); i++) {
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
        getUI().display(colors);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {}
    }


}
