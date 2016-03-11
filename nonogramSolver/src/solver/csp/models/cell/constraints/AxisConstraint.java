package solver.csp.models.cell.constraints;

import javafx.scene.layout.Pane;
import solver.csp.Constraint;
import solver.csp.Variable;
import solver.csp.models.cell.CellVariable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by tmrlvi on 24/02/2016.
 */
public class AxisConstraint extends Constraint {
    List<CellVariable> vars;
    List<Integer> blocks;


    public AxisConstraint(List<CellVariable> vars, List<Integer> blocks){
        this.vars = new ArrayList<>(vars);
        this.blocks = blocks;
    }

    @Override
    public boolean checkConstraintFails() {
        Iterator<Integer> blocksIter = blocks.iterator();
        // Skip the zero
        if (blocks.size() > 0 && blocks.get(0) == 0){
            blocksIter.next();
        }
        Integer currentBlock = 0;
        boolean inBlock = false;
        List<Integer> comitted = this.isCommited();
        for(int index : comitted){
            if(vars.get(index).getValue()!= null && !((Boolean) (vars.get(index).getValue()))){
                return true;
            }
        }
        for (CellVariable cell : vars){

            if (cell.getValue() == null){
                if (inBlock){
                    if (currentBlock == 0) {
                        inBlock = false;
                    } else {
                        currentBlock--;
                    }
                } else if (blocksIter.hasNext()) {
                    return false;
                }
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

    /*private List<List<Integer>> getPossibleBlocks(){
        LinkedList<List<Integer>> lastLayer = new LinkedList<>(); // Blocks that are closed (need
        LinkedList<List<Integer>> currentLayer = new LinkedList<>(); // Blocks that are openned
        int currentStart=-1;
        boolean inBlock = false;
        boolean inNull = false;
        for (int i=0; i < vars.size(); i++){
            if (vars.get(i).getValue() == null){
                if (inBlock){
                    if (!inNull){
                        lastLayer = currentLayer;
                        currentLayer = new LinkedList<>();
                    }
                    for (List<Integer> possibility : lastLayer){
                        List<Integer> newPossibility = new LinkedList<>(possibility);
                        newPossibility.add(i - currentStart);
                        currentLayer.add(newPossibility);
                    }
                }
                inNull = true;
            }
            else if ((Boolean) vars.get(i).getValue()){
                if (inNull && currentLayer.size() == 0){
                    currentLayer = lastLayer; //NULL that didn't affect
                }
                if (!inBlock){
                    currentStart = i;
                    inBlock = true;
                } else {
                    for (List<Integer> possibility : currentLayer){
                        currentLayer.set(currentLayer.get(currentLayer.size()-1)
                    }
                }
                inNull = false;
            } else {
                if (inNull && currentLayer.size() == 0){
                    currentLayer = lastLayer; //NULL that didn't affect
                }
                if (inBlock){
                    inBlock = false;

                }
                inNull = false;
            }
        }
        return possiblilities;
    }*/
    @Override
    public List<Variable> getAffectedVariables() {
        return new ArrayList<Variable>(vars);
    }
}
