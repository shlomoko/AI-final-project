package solver.csp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Intersection constraint - check if a cell has a specific color
 */
public class IntersectConstraint implements Constraint {
    ArrayList<Variable> rowBlocks;
    ArrayList<Variable> columnBlocks;
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
    public IntersectConstraint(int row, int column, ArrayList<Variable> rowBlocks, ArrayList<Variable> columnBlocks){
        this.rowBlocks = rowBlocks;
        this.columnBlocks = columnBlocks;
        this.row = row;
        this.column = column;
    }

    // Binary search won't work - we have null value
    private Variable _searchBlocks(ArrayList<Variable> block, int num){
        for (int i = 0; i < block.size(); i++){
            if (block.get(i).getStartValue() > num){
                // We passed the relevant value
                if (i == 1)
                    return null;
                return block.get(i-1);
            }
        }
        return null;
    }

    @Override
    public boolean isViolated() {
        Variable rowCandidate = _searchBlocks(rowBlocks, row);
        Variable columnCandidate = _searchBlocks(rowBlocks, row);
        if (rowCandidate == null || columnCandidate == null)
            return false;
        boolean isRowMarked = rowCandidate.getStartValue() + rowCandidate.getLength() > row;
        boolean isColumnMarked = columnCandidate.getStartValue() + columnCandidate.getLength() > column;
        return (isRowMarked && isColumnMarked) || (!isRowMarked && !isColumnMarked);
    }

    @Override
    public List<Variable> getAffectedVariables() {
        List<Variable> result = new ArrayList<Variable>();
        result.addAll(rowBlocks);
        result.addAll(columnBlocks);
        return result;
    }
}
