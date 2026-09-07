package ur_os;

import java.util.ArrayList;
import java.util.Arrays;

public class MFQ extends Scheduler {

    int currentScheduler;
    private ArrayList<Scheduler> schedulers;
    
    MFQ(OS os){
        super(os);
        currentScheduler = -1;
        schedulers = new ArrayList();
    }
    
    MFQ(OS os, Scheduler... s){ 
        this(os);
        schedulers.addAll(Arrays.asList(s));
        for(Scheduler sch : schedulers){
            if(sch instanceof RoundRobin){
                ((RoundRobin)sch).multiqueue = true;
            }
        }
        if(s.length > 0) currentScheduler = 0;
    }
    
    @Override
    public void addProcess(Process p){

        if (p.getState() == ProcessState.NEW || p.getState() == ProcessState.IO) {
            p.setPriority(0);
        } else {

            int currentPrio = p.getPriority();
            if (currentPrio < schedulers.size() - 1) {
                p.setPriority(currentPrio + 1);
            }
        }
        
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