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
        for (CellVariable cell : vars){

            if (cell.getValue() == null){
                return false;
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

    @Override
    public List<Variable> getAffectedVariables() {
        return new ArrayList<Variable>(vars);
    }
}
