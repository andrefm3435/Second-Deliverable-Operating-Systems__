package ur_os;

import java.util.ArrayList;
import java.util.Arrays;

public class PriorityQueue extends Scheduler {

    int currentScheduler;
    private ArrayList<Scheduler> schedulers;
    
    PriorityQueue(OS os){
        super(os);
        currentScheduler = -1;
        schedulers = new ArrayList();
    }
    
    PriorityQueue(OS os, Scheduler... s){ 
        this(os);
        schedulers.addAll(Arrays.asList(s));
        if(s.length > 0)
            currentScheduler = 0;
    }
    
    @Override
    public void addProcess(Process p){
        // Mantiene la prioridad original
        int prio = p.getPriority();
        if (prio >= 0 && prio < schedulers.size()) {
            schedulers.get(prio).addProcess(p);
        }
    }
    
    void defineCurrentScheduler(){
        currentScheduler = -1;
        for (int i = 0; i < schedulers.size(); i++) {
            if (!schedulers.get(i).isEmpty()) {
                currentScheduler = i;
                break;
            }
        }
    }
    
    @Override
    public void getNext(boolean cpuEmpty) {
        if (!cpuEmpty) {
            Process inCpu = os.getProcessInCPU();
            if (inCpu != null) {
                int cpuPrio = inCpu.getPriority();
                if (cpuPrio < schedulers.size()) {
                    schedulers.get(cpuPrio).getNext(false);
                }
            }
        }
        
        if (os.isCPUEmpty()) {
            defineCurrentScheduler();
            if (currentScheduler != -1) {
                schedulers.get(currentScheduler).getNext(true);
            }
        }
    }
    
    @Override
    public boolean isEmpty() {
        for (Scheduler s : schedulers) {
            if (!s.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < schedulers.size(); i++) {
            sb.append("Queue ").append(i).append(":\n").append(schedulers.get(i).toString());
        }
        return sb.toString();
    }
    
    @Override
    public void newProcess(boolean cpuEmpty) {} 

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {} 
}