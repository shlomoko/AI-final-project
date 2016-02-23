//package solver.cspBlock;
//
//import solver.Constraint;
//import solver.ValueHeuristic;
//import solver.VariableHeuristic;
//
//import java.util.*;
//
///**
// * Solves the CSP at hand using ArcConsistency and backtracking.
// */
//public class BlockSolver {
//    private class Arc{
//        public BlockVariable source;
//        public Constraint constaint;
//
//        public Arc(BlockVariable source, Constraint constaint){
//            this.source = source;
//            this.constaint = constaint;
//        }
//    }
//
//    List<BlockVariable> blockVariableList;
//    List<BlockVariable> unassigned;
//    VariableHeuristic variableHeuristic; // Which variable to choose next
//    ValueHeuristic valueHeuristic; // At what order we assign the values to the variable (startValues)
//    BlockManager manager;
//
//    //constructor
//    public BlockSolver(List<BlockVariable> blockVariables, VariableHeuristic variableHeur, ValueHeuristic valueHeur, BlockManager manager){
//        this.manager = manager;
//        this.variableHeuristic = variableHeur;
//        this.valueHeuristic = valueHeur;
//        this.blockVariableList = blockVariables;
//        this.unassigned = new ArrayList<BlockVariable>(blockVariables); // A shallow copy - all items point to the original
//    }
//
//    private void _undoRemoveInconsistentValues(Map<BlockVariable, List<Integer>> map){
//        if (map == null) return;
//        for (Map.Entry<BlockVariable, List<Integer>> neighbor : map.entrySet()){
//            for (Integer value : neighbor.getValue()){
//                neighbor.getKey().addLegalValue(value);
//            }
//        }
//    }
//
//    private boolean _couldSatisfyConstraint(Constraint cons, Queue<BlockVariable> remainingBlockVariables){
//        if (remainingBlockVariables.size() == 0)
//            return !cons.isViolated(); // The constraint is not violated!
//        //Copy, so we don't ruin;
//        remainingBlockVariables = new LinkedList<BlockVariable>(remainingBlockVariables);
//        BlockVariable next_var = remainingBlockVariables.remove();
//        Object originalValue = next_var.getValue();
//        for (Object value : next_var.getLegalValues()){
//            next_var.setValue(value);
//            if (_couldSatisfyConstraint(cons, remainingBlockVariables)) {
//                // Return to the original value
//                next_var.setValue(originalValue);
//                return true;
//            }
//        }
//        // Return to the original value
//        next_var.setValue(originalValue);
//        return false;
//    }
//
//    // For every value in the domain, checks if there is any assignment s.t. the constraint would not be violated.
//    private List<Integer> _removeInconsistentValue(BlockVariable source, Constraint cons){
//        Queue<BlockVariable> others = new LinkedList<BlockVariable>(cons.getAffectedVariables());
//        others.remove(source);
//        Object previousX = source.getValue();
//        List<Object> removed = new LinkedList<Object>();
//        List<Object> legalValues = new LinkedList<Object>(source.getLegalValues());
//        for (Object x : legalValues){
//            // Note that there is not harm in resetting the variables - legal values is the only value if only
//            // such value possible.
//            source.setValue(x);
//            if (!_couldSatisfyConstraint(cons, others)){
//                removed.add(x);
//                source.removeLegalValue(x);
//            }
//        }
//        source.setValue(previousX);
//        return removed;
//    }
//
//    // Returns a map of removed values, if detected inconsistency, returns null.
//    private Map<BlockVariable, List<Integer>> generalizedArcConsistency(){
//        Map<BlockVariable, List<Integer>> inconsistentValues = new HashMap<BlockVariable, List<Integer>>();
//        Queue<Arc> arcs = new LinkedList<Arc>();
//        // TODO: should arc consistency be only on changed nodes? (Then we get as variable the relevant variable?)
//        for (BlockVariable first : blockVariableList){
//            for (Constraint cons : first.getConstraints())
//                arcs.add(new Arc(first, cons));
//        }
//
//        while (!arcs.isEmpty()){
//            Arc arc = arcs.remove();
//
//            // Go over different instantiation and check if the relevant constraint is violated
//            List<Integer> values = _removeInconsistentValue(arc.source, arc.constaint);
//
//            // arc.source changed, so we need to add all others:
//            if (values.size() > 0) {
//                if (inconsistentValues.containsKey(arc.source)){
//                    inconsistentValues.get(arc.source).addAll(values);
//                } else {
//                    inconsistentValues.put(arc.source, values);
//                }
//
//                // If an arc remained with no domain, we've lost.
//                if (arc.source.getLegalValues().size() == 0) {
//                    // Return all nodes lost so far
//                    _undoRemoveInconsistentValues(inconsistentValues);
//                    return null;
//                }
//                // We removed stuff - we need to take care of the neighbors
//                for (Constraint cons : arc.source.getConstraints())
//                    for (BlockVariable first : cons.getAffectedVariables())
//                        if (first != arc.source)
//                            arcs.add(new Arc(first, cons));
//            }
//        }
//        return inconsistentValues;
//    }
//
//
//    /**
//     * Implementing backtracking recursively to solve CSV, using the variable heuristic and
//     * constraint heuristic.
//     * @return whether the assignment succeeded or not
//     */
//    public boolean backtracking(boolean arcConsistent){
//        if (unassigned.size() == 0) return true;
//        BlockVariable to_assign = variableHeuristic.select(unassigned);
//        System.out.println(to_assign);
//        unassigned.remove(to_assign);
//        for (int num : valueHeuristic.order(to_assign)){
//            // No need to check if variable is consistent - arc consistency is taking care of it
//            if (to_assign.isLegalValue(num)) {
//                to_assign.setValue(num);
//                manager.display(getVariables());
//                // We assigned - we need to check for consistency
//                Map<BlockVariable, List<Integer>> inconsistentValues = null;
//                if (arcConsistent)
//                    inconsistentValues = generalizedArcConsistency();
//                // Arc consistency found legal assignments
//                if (!arcConsistent || inconsistentValues != null) {
//                    // Recursively continue the enumeration
//                    if (backtracking(arcConsistent))
//                        return true;
//                }
//                // Undo arcConsistency - after coming back from the backtracking
//                _undoRemoveInconsistentValues(inconsistentValues);
//            }
//        }
//        to_assign.setValue(null);
//        unassigned.add(to_assign);
//        return false;
//    }
//
//    List<BlockVariable> getVariables(){
//        // TODO: maybe we want a deep copy?
//        return blockVariableList;
//    }
//
//    public void restart() {
//        for (BlockVariable var : blockVariableList){
//            var.setValue(null);
//        }
//        unassigned = new ArrayList<BlockVariable>(blockVariableList);
//    }
//}
