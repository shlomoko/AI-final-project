package solver.csp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zohar on 24/01/2016.
 */
public class OrderConstraint extends Constraint {

    public OrderConstraint(Variable firstVar, Variable secondVar) {
        super(firstVar, secondVar);
    }

    @Override
    public boolean isViolated() {
        return !(firstVar.getStartValue() == null || secondVar.getStartValue() == null) &&
                !(firstVar.getStartValue() + firstVar.getLength() < secondVar.getStartValue());
    }
}