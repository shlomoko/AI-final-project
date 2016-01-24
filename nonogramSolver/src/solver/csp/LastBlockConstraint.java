package solver.csp;

/**
 * Created by Zohar on 24/01/2016.
 */
public class LastBlockConstraint extends Constraint {

    public LastBlockConstraint(Variable firstVar, Variable secondVar) {
        super(firstVar, secondVar);
    }

    @Override
    public boolean isViolated() {
        return false;
    }
}
