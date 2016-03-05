package solver.csp;

import solver.gui.Counters;

import java.util.List;

/**
 * Created by Zohar on 24/01/2016.
 */
public abstract class Constraint {

    public boolean isViolated(){
        Counters counters = Counters.getInstance();
        Long varAmount = 0L;
        Long varDomainAmount = 0L;
        counters.addCount("Constraints", 1L);
        for (Variable var : getAffectedVariables()){
            varAmount++;
            varDomainAmount += var.getLegalValues().size();
        }
        counters.addCount("Constraints times amount of variables", varAmount);
        counters.addCount("Constraints times amount of variables times domain", varDomainAmount);

        return checkConstraintFails();
    }

    public abstract boolean checkConstraintFails();
    public abstract List<Variable> getAffectedVariables();
}
