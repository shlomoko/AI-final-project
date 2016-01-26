package solver.csp.heuristics;

import solver.csp.Variable;

import java.util.List;

public class MinimumRemainingValues implements  VariableHeuristic{
    public  Variable select(List<Variable> Vars){
        Variable minVar = null;
        for (Variable variable : Vars) {
            if (variable.getLegalValues().size() < minVar.getLegalValues().size())
                    minVar = variable;
        }
        return  minVar;
    }
}