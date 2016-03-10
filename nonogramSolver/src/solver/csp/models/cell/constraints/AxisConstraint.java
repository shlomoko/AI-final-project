package solver.csp.models.cell.constraints;

import solver.csp.Constraint;
import solver.csp.Variable;
import solver.csp.models.cell.CellVariable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by tmrlvi on 24/02/2016.
 */
public class AxisConstraint extends Constraint {
    List<CellVariable> vars;
    List<Integer> blocks;


    public AxisConstraint(List<CellVariable> vars, List<Integer> blocks){
        this.vars = vars;
        this.blocks = blocks;
    }

    @Override
    public boolean checkConstraintFails() {
        Iterator<Integer> blocksIter = blocks.iterator();
        Integer currentBlock = 0;
        boolean inBlock = false;
        List<Integer> comitted = this.isCommited();
        for(int index : comitted){
            if(vars.get(index).getValue()!= null &&(Boolean)(vars.get(index).getValue())  == false){
                return true;
            }
        }
        for (CellVariable cell : vars){

            if (cell.getValue() == null){
                if (blocksIter.hasNext()) {
                    return false;
                }
                inBlock = false;
            }
            //Finished a block
            else if (inBlock) {
                if (currentBlock == 0) {
                    if ((Boolean) cell.getValue())
                        return true;
                    inBlock = false;
                }
                else if ((Boolean) cell.getValue()) {
                    currentBlock--;
                } else {
                    return true;
                }
            } else {
                if ((Boolean) cell.getValue()) {
                    inBlock = true;
                    if (!blocksIter.hasNext())
                        return true;
                    currentBlock = blocksIter.next()-1;
                }
            }
        }
        //TODO: can infer from middle pieces or from the end
        return (inBlock && currentBlock > 0) || blocksIter.hasNext();
    }
    private List<Integer> isCommited() {
        List<Integer> commited = new ArrayList<Integer>();
        for (int bl:blocks) {
            int spare= bl-(vars.size() / 2);
            if (spare > 0 && vars.size()%2 ==0) {
                for (int i=(vars.size() / 2)-spare;i<=(vars.size() / 2)+spare-1;i++) {
                    commited.add(i);
                }
            }
            if (spare > 0 && vars.size()%2 ==1){
                for (int i=(vars.size() / 2)-spare+1;i<=(vars.size() / 2)+spare-1;i++) {
                    commited.add(i);
                }
            }
        }
        return commited;
    }
    @Override
    public List<Variable> getAffectedVariables() {
        return new ArrayList<Variable>(vars);
    }
}
