package solver.csp;

/**
 * Created by Zohar on 24/01/2016.
 */
public class OrderConstraint implements Constraint {
    Variable firstVar;
    Variable secondVar;

    public OrderConstraint(Variable firstVar, Variable secondVar) {
        this.firstVar = firstVar;
        this.secondVar = secondVar;
    }

    @Override
    public boolean isViolated() {
        return !(firstVar.getStartValue() == null || secondVar.getStartValue() == null) &&
                !(firstVar.getStartValue() + firstVar.getLength() < secondVar.getStartValue());
    }
}