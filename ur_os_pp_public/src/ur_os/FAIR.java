package ur_os;
import java.util.HashMap;

/**
 * FAIR: a fair-share scheduler.
 *
 * Idea: every process should receive roughly the same amount of CPU time
 * over the life of the simulation. To do this, the scheduler keeps track
 * of how many CPU ticks each process (by pid) has already consumed.
 *
 * - When the CPU is idle, it dispatches the ready process with the LEAST
 *   accumulated CPU time so far (ties are resolved with the inherited
 *   tieBreaker()).
 * - While a process runs, its accumulated CPU time is updated every tick.
 * - Every "someValue" ticks (the fairness quantum), the scheduler checks
 *   whether some ready process has received less CPU time than the one
 *   currently running; if so, it preempts the running process in favor
 *   of the less-served one.
 */
public class FAIR extends Scheduler {
    private int someValue;      // fairness quantum: how often we re-check for a fairer process
    private int cont;
    private HashMap<Integer, Integer> cpuTimeUsed;

    public FAIR(OS os) {
        this(os, 5); // default fairness quantum when none is specified
    }

    public FAIR(OS os, int someValue) {
        super(os);
        this.someValue = someValue;
        this.cont = 0;
        this.cpuTimeUsed = new HashMap<>();
    }

    private int getUsedTime(Process p) {
        return cpuTimeUsed.getOrDefault(p.getPid(), 0);
    }

    private void resetCounter() {
        cont = 0;
    }

    /** Returns the ready process with the least accumulated CPU time. */
    private Process pickFairest() {
        Process fairest = null;
        for (Process p : processes) {
            if (fairest == null) {
                fairest = p;
            } else if (getUsedTime(p) < getUsedTime(fairest)) {
                fairest = p;
            } else if (getUsedTime(p) == getUsedTime(fairest)) {
                fairest = tieBreaker(fairest, p);
            }
        }
        return fairest;
    }

    @Override
    public void newProcess(boolean cpuEmpty) {
        // Fairness is enforced by quantum expiry in getNext(); nothing
        // extra to do the instant a new process arrives.
    }

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {
        // Same as above: handled by the periodic fairness check in getNext().
    }

    @Override
    public void getNext(boolean cpuEmpty) {
        if (cpuEmpty) {
            if (!processes.isEmpty()) {
                Process next = pickFairest();
                processes.remove(next);
                os.interrupt(InterruptType.SCHEDULER_RQ_TO_CPU, next);
                resetCounter();
            }
            return;
        }

        // CPU is busy: charge the running process for this tick.
        Process current = os.getProcessInCPU();
        if (current != null) {
            cpuTimeUsed.merge(current.getPid(), 1, Integer::sum);
        }
        cont++;

        // Every "someValue" ticks, see if a less-served process should
        // take over to keep CPU time fairly distributed.
        if (!processes.isEmpty() && cont >= someValue) {
            Process fairest = pickFairest();
            if (current != null && getUsedTime(fairest) < getUsedTime(current)) {
                os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, current);
                processes.remove(fairest);
                os.interrupt(InterruptType.SCHEDULER_RQ_TO_CPU, fairest);
            }
            resetCounter();
        }
    }
}