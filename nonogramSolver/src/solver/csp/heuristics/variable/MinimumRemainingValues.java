package solver.csp.heuristics.variable;

import solver.annotations.Default;
import solver.csp.Variable;

import java.util.LinkedList;
import java.util.List;

@Default
public class MinimumRemainingValues implements VariableHeuristic {
    DegreeHeuristic deg;

    public MinimumRemainingValues(){
        deg = new DegreeHeuristic();
    }

    public Variable select(List<? extends Variable> Vars){
        Variable minVar = null;
        List<Variable> tie = new LinkedList<Variable>();
        for (Variable blockVariable : Vars) {
            if (minVar == null){
                minVar = blockVariable;
                tie.add(minVar);
            } else {
                if (blockVariable.getLegalValues().size() == minVar.getLegalValues().size()) {
                    tie.add(blockVariable);
                }
                if (blockVariable.getLegalValues().size() < minVar.getLegalValues().size()) {
                    minVar = blockVariable;
                    tie.clear();
                    tie.add(minVar);
                }
            }
        }
        if(tie.size() >= 1)
            minVar = deg.select(tie);
        return  minVar;
    }
}