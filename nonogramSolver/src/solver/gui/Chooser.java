package solver.gui;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import javafx.scene.layout.Pane;
import org.reflections.Reflections;
import solver.annotations.Default;
import solver.annotations.LimitTo;
import solver.csp.Constraint;
import solver.csp.ConstraintHandler;
import solver.csp.Manager;
import solver.csp.heuristics.value.ValueHeuristic;
import solver.csp.heuristics.variable.VariableHeuristic;
import solver.csp.models.rowcol.RowColManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by tmrlvi on 04/03/2016.
 */
public class Chooser {
    private Set<Class<? extends Manager>> models;
    private Set<Class<? extends VariableHeuristic>> varHeur;
    private Set<Class<? extends ValueHeuristic>> valHeur;
    private Set<Class<? extends ConstraintHandler>> handler;

    public Chooser(){
        Reflections reflections = new Reflections("solver.csp");
        models = reflections.getSubTypesOf(Manager.class);
        varHeur = reflections.getSubTypesOf(VariableHeuristic.class);
        valHeur = reflections.getSubTypesOf(ValueHeuristic.class);
        handler = reflections.getSubTypesOf(ConstraintHandler.class);
    }

    public Set<Class<? extends Manager>> getModels(){
        return models;
    }

    public Class<? extends Manager> getModel(String name){
        return (Class<? extends Manager>) byName(getModels(), name);
    }

    public Set<Class<? extends ConstraintHandler>> getHandlers(){
        return handler;
    }

    public Class<? extends ConstraintHandler> getHandler(String name){
        return (Class<? extends ConstraintHandler>) byName(getHandlers(), name);
    }

    public Set<Class<? extends VariableHeuristic>> getVariableHeuristics(final Class<? extends Manager> manager){
        return new HashSet<Class<? extends VariableHeuristic>> (Collections2.filter(varHeur, new Predicate<Class>() {
            @Override
            public boolean apply(Class cls) {
                LimitTo annotation = (LimitTo) cls.getAnnotation(LimitTo.class);
                return annotation == null || annotation.value().equals(manager.getSimpleName());
            }
        }));
    }

    public Class<? extends VariableHeuristic> getVariableHeuristic(String name, final Class<? extends Manager> manager){
        return (Class<? extends VariableHeuristic>) byName(getVariableHeuristics(manager), name);
    }

    public Set<Class<? extends ValueHeuristic>> getValueHeuristics(final Class<? extends Manager> manager) {
        return new HashSet<Class<? extends ValueHeuristic>>(Collections2.filter(valHeur, new Predicate<Class>() {
            @Override
            public boolean apply(Class cls) {
                LimitTo annotation = (LimitTo) cls.getAnnotation(LimitTo.class);
                return annotation == null || annotation.value().equals(manager.getSimpleName());
            }
        }));
    }

    public Class<? extends ValueHeuristic> getValueHeuristic(String name, final Class<? extends Manager> manager){
        return (Class<? extends ValueHeuristic>) byName(getValueHeuristics(manager), name);
    }

    public Class<?> getDefault(Set<? extends Class<?>> items){
        return Collections.max(items, new Comparator<Class<? extends Object>>() {
            @Override
            public int compare(Class<? extends Object> a, Class<? extends Object> b) {
                Default aAnnotation = (Default) a.getAnnotation(Default.class);
                Default bAnnotation = (Default) b.getAnnotation(Default.class);
                if (aAnnotation == null){
                    return -1;
                } else if (bAnnotation == null){
                    return 1;
                }
                return aAnnotation.value() - bAnnotation.value();
            }
        });
    }

    public Class<?> byName(Set<? extends Class<?>> items, String name){
        for (Class<?> item : items){
            if (item.getSimpleName().equals(name)){
                return item;
            }
        }
        return null;
    }
}
