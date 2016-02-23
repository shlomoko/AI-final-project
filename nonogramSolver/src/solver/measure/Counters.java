package solver.measure;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by tmrlvi on 24/02/2016.
 */
public class Counters {
    private static Counters _instance = new Counters();
    private Map<String, Long> counters;
    private Map<String, Long> ticks;
    private Map<String, Long> ticksAmount;
    private Map<String, Long> totalTicks;

    private Counters(){
        counters = new HashMap<String, Long>();
        ticks = new HashMap<String, Long>();
        totalTicks = new HashMap<String, Long>();
        ticksAmount = new HashMap<String, Long>();
    }

    public synchronized void addCount(String name, Long amount){
        Long count;
        if (counters.containsKey(name))
            count = counters.get(name);
        else
            count = 0L;
        counters.put(name, count + amount);
    }

    public synchronized void tick(String name){
        ticks.put(name, System.currentTimeMillis());
        totalTicks.put(name, 0L);
        ticksAmount.put(name, 0L);
    }

    public synchronized Long tock(String name){
        Long time = System.currentTimeMillis() - ticks.get(name);
        totalTicks.put(name, totalTicks.get(name)+time);
        ticksAmount.put(name, ticksAmount.get(name)+1);
        return time;
    }

    public synchronized Long getCount(String name){
        if (!counters.containsKey(name))
            return 0L;
        return counters.get(name);
    }

    public synchronized Long getAverageTicks(String name){
        if (!totalTicks.containsKey(name))
            return 0L;
        return totalTicks.get(name) / ticksAmount.get(name);
    }

    public synchronized Set<String> keySetCount(){
        return counters.keySet();
    }

    public synchronized Set<String> keySetTick(){
        return ticksAmount.keySet();
    }

    public static Counters getInstance(){
        return _instance;
    }
}
