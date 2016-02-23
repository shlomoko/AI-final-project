package solver.csp;

import java.util.List;
import java.util.Set;

/**
 * Created by Zohar on 22/02/2016.
 */
public abstract class Variable {
    public abstract void setValue(Object value);
    public abstract Object getValue();
    public abstract Set<Object> getLegalValues();
    public abstract void addLegalValue(Object value);
    public abstract void removeLegalValue(Object value);
    public abstract List<Constraint> getConstraints();
    public abstract void addConstraint(Constraint constraint);

    public boolean isLegalValue(Object value){
        Object oldValue = getValue();
        setValue(value);
        for (Constraint constr : getConstraints()){
            if (constr.isViolated()){
                setValue(oldValue);
                return false;
            }
        }
        setValue(oldValue);
        return true;
    }

    public int getConstraintNumber(){
        int sum = 0;
        for (Constraint cons : getConstraints()){
            sum = sum + cons.getAffectedVariables().size();
        }
        return sum;
    }
}
