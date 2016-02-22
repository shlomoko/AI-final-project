package solver;

import java.util.List;
import java.util.Set;

/**
 * Created by Zohar on 22/02/2016.
 */
public interface Variable {
    public void setValue(Object value);
    public Object getValue();
    public Set<Object> getLegalValues();
    public void addLegalValue(Object value);
    public void removeLegalValue(Object value);
    public boolean isLegalValue(Object value);
    public List<Constraint> getConstraints();
    public int getConstraintNumber();
}
