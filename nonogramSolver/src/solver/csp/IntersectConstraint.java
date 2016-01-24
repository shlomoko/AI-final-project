package solver.csp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Intersection constraint - check if a cell has a specific color
 */
public class IntersectConstraint extends Constraint {

    public IntersectConstraint(Variable firstVar, Variable secondVar) {
        super(firstVar, secondVar);
    }

    @Override
    public boolean isViolated() {
        int firstBlockDist = secondVar.getIndex() - firstVar.getStartValue();
        int secondBlockDist = firstVar.getIndex() - secondVar.getStartValue();
        boolean isFirstInRange = 0 < firstBlockDist && firstBlockDist < firstVar.getLength();
        boolean isSecondInRange = 0 < secondBlockDist && secondBlockDist < secondVar.getLength();
        return (isFirstInRange && isSecondInRange) || (!isFirstInRange && !isSecondInRange);
    }
}
