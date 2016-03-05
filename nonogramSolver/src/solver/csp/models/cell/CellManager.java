package solver.csp.models.cell;

import javafx.scene.paint.Color;
import solver.csp.*;
import solver.csp.heuristics.value.ValueHeuristic;
import solver.csp.heuristics.variable.VariableHeuristic;
import solver.csp.models.cell.constraints.AxisConstraint;
import solver.csp.NonogramParser;
import solver.gui.UserInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tmrlvi on 24/02/2016.
 */
public class CellManager extends Manager {

    public CellManager(NonogramParser parser, UserInterface ui, VariableHeuristic varHeur, ValueHeuristic valueHeur, ConstraintHandler handler) {
        super(parser, ui, varHeur, valueHeur, handler);
    }


    @Override
    protected List<? extends Variable> parserToVariable(NonogramParser parser) {
        CellVariable[][] variables = _assignConstraints(parser);
        List<Variable> variableList = new ArrayList<Variable>(parser.getRows()*parser.getColumns());
        for (int i=0; i<variables.length; i++){
            for (int j=0; j<variables[i].length; j++){
                variableList.add(variables[i][j]);
            }
        }
        return variableList;
    }


    private CellVariable[][] _assignConstraints(NonogramParser parser){
        // Create variables
        CellVariable[][] variables = new CellVariable[parser.getRows()][parser.getColumns()];
        for (int i=0; i < parser.getRows(); i++){
            for (int j=0; j < parser.getColumns(); j++){
                variables[i][j] = new CellVariable(i, j);
            }
        }
        // Create row constraints
        List<List<Integer>> rows = parser.getRowsList();
        for (int i=0; i < parser.getRows(); i++){
            List<CellVariable> rowVars = new ArrayList<CellVariable>();
            for (int j=0; j< parser.getColumns(); j++){
                rowVars.add(variables[i][j]);
            }
            Constraint rowConstraint = new AxisConstraint(rowVars, rows.get(i));
            for (CellVariable row : rowVars){
                row.addConstraint(rowConstraint);
            }
        }
        // Create column constraints
        List<List<Integer>> columns = parser.getColumnList();
        for (int j=0; j < parser.getColumns(); j++){
            List<CellVariable> colVars = new ArrayList<CellVariable>();
            for (int i=0; i< parser.getRows(); i++){
                colVars.add(variables[i][j]);
            }
            Constraint colConstraint = new AxisConstraint(colVars, columns.get(j));
            for (CellVariable col : colVars){
                col.addConstraint(colConstraint);
            }
        }
        return variables;
    }

    @Override
    public void display() {
        Color[][] colors = new Color[getColAmount()][getRowAmount()];
        for (Variable var : getSolver().getVariables()){
            if (var.getValue() == null) {
                colors[((CellVariable)var).getColumn()][((CellVariable)var).getRow()] = Color.GRAY;
            } else if ((Boolean) var.getValue()){
                colors[((CellVariable)var).getColumn()][((CellVariable)var).getRow()] = Color.BLACK;
            } else{
                colors[((CellVariable)var).getColumn()][((CellVariable)var).getRow()] = Color.WHITE;
            }
        }
        getUI().display(colors);

    }
}
