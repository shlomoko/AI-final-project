package solver.csp;

import java.util.List;
import java.util.Map;

/**
 * Created by tmrlvi on 23/02/2016.
 */
public interface ConstraintHandler {
    void setManager(Manager manager);
    boolean initialize(List<? extends Variable> unassigned);

    Map<? extends Variable, List<Object>> checkConstraints(Variable assigned);

    void restoreValue(Map<? extends Variable, List<Object>> removedValues);
}
