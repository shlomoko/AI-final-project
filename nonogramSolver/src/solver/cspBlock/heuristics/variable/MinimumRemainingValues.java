package solver.cspBlock.heuristics.variable;

import solver.cspBlock.Variable;
import solver.cspBlock.heuristics.variable.DegreeHeuristic;
import solver.cspBlock.heuristics.variable.VariableHeuristic;

import java.util.LinkedList;
import java.util.List;

public class MinimumRemainingValues implements VariableHeuristic {
    DegreeHeuristic deg;

    public MinimumRemainingValues(){
        deg = new DegreeHeuristic();
    }

    public  Variable select(List<Variable> Vars){
        Variable minVar = null;
        List<Variable> tie = new LinkedList<Variable>();
        for (Variable variable : Vars) {
            if (minVar == null){
                minVar = variable;
                tie.add(minVar);
            } else {
                if (variable.getLegalValues().size() == minVar.getLegalValues().size()) {
                    tie.add(variable);
                }
                if (variable.getLegalValues().size() < minVar.getLegalValues().size()) {
                    minVar = variable;
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