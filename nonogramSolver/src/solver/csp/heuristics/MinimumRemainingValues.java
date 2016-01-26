package solver.csp.heuristics;

import solver.csp.Variable;

import java.util.List;

public class MinimumRemainingValues implements  VariableHeuristic{
    DegreeHeuristic deg;

    public  Variable select(List<Variable> Vars){
        Variable minVar = null;
        List<Variable> tie = null;
        for (Variable variable : Vars) {
            if(variable.getLegalValues().size() == minVar.getLegalValues().size())
                tie.add(variable);
            if (variable.getLegalValues().size() < minVar.getLegalValues().size()) {
                minVar = variable;
                tie = null;
                tie.add(minVar);
            }
        }
        if(tie.size() > 1)
            minVar = deg.select(tie);
        return  minVar;
    }
}