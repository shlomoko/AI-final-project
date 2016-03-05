package solver.csp.models.rowcol;

import javafx.scene.paint.Color;
import solver.annotations.Default;
import solver.csp.*;
import solver.csp.heuristics.value.ValueHeuristic;
import solver.csp.heuristics.variable.VariableHeuristic;
import solver.csp.models.rowcol.constraints.RowColConstraint;
import solver.gui.UserInterface;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by tmrlvi on 07/02/2016.
 */
@Default
public class RowColManager extends Manager {

    public RowColManager(NonogramParser parser, UserInterface ui, VariableHeuristic varHeur, ValueHeuristic valueHeur, ConstraintHandler handler) {
        super(parser, ui, varHeur, valueHeur, handler);
    }

    protected List<? extends Variable> parserToVariable(NonogramParser parser){
        List<RowColVariable> variables = new LinkedList<RowColVariable>();
        List<RowColVariable> rowVariables = new LinkedList<RowColVariable>();
        int i=0;
        for (List<Integer> block : parser.getRowsList()){
            RowColVariable var = new RowColVariable(block, true, i, parser.getColAmount());
            rowVariables.add(var);
            variables.add(var);
            i++;
        }
        List<RowColVariable> colVariables = new LinkedList<RowColVariable>();
        i = 0;
        for (List<Integer> block : parser.getColumnList()){
            RowColVariable var = new RowColVariable(block, false, i, parser.getRowAmount());
            colVariables.add(var);
            variables.add(var);
            i++;
        }


        for (RowColVariable rowVar : rowVariables){
            for (RowColVariable colVar : colVariables){
                RowColConstraint constraint = new RowColConstraint(rowVar, colVar);
                rowVar.addConstraint(constraint);
                colVar.addConstraint(constraint);
            }
        }
        return variables;
    }


    public void display(){
        Color[][] colors = new Color[getColAmount()][getRowAmount()];
        for (int i = 0; i < getColAmount(); i++){
            for (int j = 0; j < getRowAmount(); j++){
                colors[i][j] = Color.GRAY;
            }
        }
        for (Variable basicVar : getSolver().getVariables()){
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
        getUI().display(colors);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {}
    }

}
