package ur_os;

public class RoundRobin extends Scheduler {

    int q;
    int cont;
    boolean multiqueue;
    
    RoundRobin(OS os){
        super(os);
        q = 5;
        cont = 0;
    }
    
    RoundRobin(OS os, int q){
        this(os);
        this.q = q;
    }

    RoundRobin(OS os, int q, boolean multiqueue){
        this(os);
        this.q = q;
        this.multiqueue = multiqueue;
    }
    
    void resetCounter(){
        cont = 0;
    }
   
    @Override
    public void getNext(boolean cpuEmpty) {
        if (cpuEmpty) {
            // El padre (o el sistema) ordenó llenar la CPU
            if (!processes.isEmpty()) {
                Process p = processes.remove(0);
                os.interrupt(InterruptType.SCHEDULER_RQ_TO_CPU, p);
                resetCounter();
            }
        } else {
            // La CPU está ocupada, se evalúa el quantum
            if (cont >= (q - 1)) {
                Process current = os.getProcessInCPU();
                os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, current);
                resetCounter();
                
                // Solo se auto-despacha si es Standalone. En MFQ/Priority, el padre decide.
                if (!multiqueue && !processes.isEmpty()) {
                    Process next = processes.remove(0);
                    os.interrupt(InterruptType.SCHEDULER_RQ_TO_CPU, next);
                    resetCounter();
                }
            } else {
                cont++;
            }
        }
    }
    
    @Override
    public void newProcess(boolean cpuEmpty) {} 

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {} 
}