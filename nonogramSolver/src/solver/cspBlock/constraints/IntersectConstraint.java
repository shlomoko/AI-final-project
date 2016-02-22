package solver.cspBlock.constraints;

import solver.Variable;
import solver.cspBlock.BlockVariable;
import solver.Constraint;

import java.util.ArrayList;
import java.util.List;

/**
 * Intersection constraint - check if a cell has a specific color
 */
public class IntersectConstraint implements Constraint {
    List<BlockVariable> rowBlocks;
    List<BlockVariable> columnBlocks;
    Integer row;
    Integer column;

    /**
     * Create a new intersection constraint - checks if block intersect between row
     * and column
     * @param row the row index of the constraint
     * @param column the column index of the constraint
     * @param rowBlocks the blocks in the specified row
     * @param columnBlocks the columns in the specified columns
     */
    public IntersectConstraint(int row, int column, List<BlockVariable> rowBlocks, List<BlockVariable> columnBlocks){
        this.rowBlocks = rowBlocks;
        this.columnBlocks = columnBlocks;
        this.row = row;
        this.column = column;
    }

    // Binary search won't work - we have null value
    // Return the last block before the row, or null if all blocks are after
    private BlockVariable _searchBlocks(List<BlockVariable> block, int num){
        if (block.size() == 0){
            return null;
        }
        for (int i = 0; i < block.size(); i++){
            if (block.get(i).getValue() != null && (Integer) block.get(i).getValue() > num){
                // We passed the relevant value
                if (i < 1)
                    return null;
                return block.get(i-1);
            }
        }
        return block.get(block.size()-1); //If not find - get last
    }

    private boolean _isMarked(BlockVariable block, Integer cell){
        return (block != null) && ((Integer) block.getValue() + block.getLength() > cell);
    }

    @Override
    public boolean isViolated() {
        BlockVariable rowCandidate = _searchBlocks(rowBlocks, column);
        BlockVariable columnCandidate = _searchBlocks(columnBlocks, row);
        // There is a block - but we don't know where it starts
        if ((rowCandidate != null && rowCandidate.getValue() == null) ||
                (columnCandidate != null && columnCandidate.getValue() == null)){
            return false;
        }
        boolean isRowMarked = _isMarked(rowCandidate, column);
        boolean isColumnMarked = _isMarked(columnCandidate, row);
        return (isRowMarked && !isColumnMarked) || (!isRowMarked && isColumnMarked);
    }

    @Override
    public List<Variable> getAffectedVariables() {
        List<Variable> result = new ArrayList<Variable>();
        result.addAll(rowBlocks);
        result.addAll(columnBlocks);
        return result;
    }
}
