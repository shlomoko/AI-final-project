package solver.cspBlock.heuristics.variable;

import solver.cspBlock.Variable;
import solver.cspBlock.heuristics.variable.VariableHeuristic;

import java.util.List;

public class DegreeHeuristic implements VariableHeuristic {
    public Variable select(List<Variable> Potentials){
        Variable theChosenOne = null;
        for(Variable var : Potentials) {
            if (theChosenOne == null) {
                theChosenOne = var;
            } else {
                if (var.getConstraintNumber() > theChosenOne.getConstraintNumber())
                    theChosenOne = var;
            }
        }
        return theChosenOne;
    }
}