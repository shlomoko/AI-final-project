package solver.cspBlock.heuristics.variable;

import solver.Variable;
import solver.VariableHeuristic;
import solver.cspBlock.BlockVariable;

import java.util.LinkedList;
import java.util.List;

public class MinimumRemainingValues implements VariableHeuristic {
    DegreeHeuristic deg;

    public MinimumRemainingValues(){
        deg = new DegreeHeuristic();
    }

    public Variable select(List<Variable> Vars){
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