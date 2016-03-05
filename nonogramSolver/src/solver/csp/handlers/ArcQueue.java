package solver.csp.handlers;

import solver.csp.Constraint;
import solver.csp.Variable;

import java.util.*;

/**
 * Created by tmrlvi on 04/03/2016.
 */
public class ArcQueue {
    private Queue<Arc> queue;
    private HashSet<Arc> existingArcs;

    public class Arc{
        public final Variable source;
        public final Constraint constaint;

        public Arc(Variable source, Constraint constaint){
            this.source = source;
            this.constaint = constaint;
        }

        @Override
        public boolean equals(Object other){
            if (other instanceof Arc){
                Arc otherArc = (Arc) other;
                return source == otherArc.source && constaint == otherArc.constaint;
            }
            return false;
        }

        @Override
        public int hashCode(){
            return source.hashCode() * constaint.hashCode();
        }
    }

    public ArcQueue(){
        queue = new LinkedList<Arc>();
        existingArcs = new HashSet<Arc>();
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public boolean add(Variable var, Constraint constr) {
        Arc arc = new Arc(var, constr);
        if (!existingArcs.contains(arc)){
            existingArcs.add(arc);
            queue.add(arc);
        }
        return false;
    }

    public Arc remove() {
        Arc chosen = queue.remove();
        existingArcs.remove(chosen);
        return chosen;
    }

}
