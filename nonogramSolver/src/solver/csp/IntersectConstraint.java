package solver.csp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Intersection constraint - check if a cell has a specific color
 */
public class IntersectConstraint implements Constraint {
    List<Variable> rowBlocks;
    List<Variable> columnBlocks;
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
    public IntersectConstraint(int row, int column, List<Variable> rowBlocks, List<Variable> columnBlocks){
        this.rowBlocks = rowBlocks;
        this.columnBlocks = columnBlocks;
        this.row = row;
        this.column = column;
    }

    // Binary search won't work - we have null value
    // Return the last block before the row, or null if all blocks are after
    private Variable _searchBlocks(List<Variable> block, int num){
        if (block.size() == 0){
            return null;
        }
        for (int i = 0; i < block.size(); i++){
            if (block.get(i).getStartValue() != null && block.get(i).getStartValue() > num){
                // We passed the relevant value
                if (i < 1)
                    return null;
                return block.get(i-1);
            }
        }
        return block.get(block.size()-1); //If not find - get last
    }

    private boolean _isMarked(Variable block, Integer cell){
        return (block != null) && (block.getStartValue() + block.getLength() > cell);
    }

    @Override
    public boolean isViolated() {
        Variable rowCandidate = _searchBlocks(rowBlocks, column);
        Variable columnCandidate = _searchBlocks(columnBlocks, row);
        // There is a block - but we don't know where it starts
        if ((rowCandidate != null && rowCandidate.getStartValue() == null) ||
                (columnCandidate != null && columnCandidate.getStartValue() == null)){
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
