/****************************
    Cpu scheduling algorithms
*****************************/



import java.util.*;

class Process {
  protected String name;
  protected int arrivalTime;
  protected int burstTime;
  protected int priority;

  public Process(String name, int arrivalTime, int burstTime, int priority) {
    this.name = name;
    this.arrivalTime = arrivalTime;
    this.burstTime = burstTime;
    this.priority = priority;
  }

  public Process(Process process) {
    this.name = process.name;
    this.arrivalTime = process.arrivalTime;
    this.burstTime = process.burstTime;
    this.priority = process.priority;
  }

  public String getName() {
    return name;
  }

  public int getArrivalTime() {
    return arrivalTime;
  }

  public int getBurstTime() {
    return burstTime;
  }

  public void setBurstTime(int burstTime) {
    this.burstTime = burstTime;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public void decreaseBurstTime() {
    if (this.burstTime > 0) {
      this.burstTime -= 1;
    }
  }

  public void increasePriority() {
    if (this.priority > 1) {
      this.priority -= 1;
    }
  }

  public boolean isFinished() {
    return this.burstTime == 0;
  }

  @Override
  public String toString() {
    return this.name + " " + this.arrivalTime + " " + this.burstTime + " " + this.priority;
  }

}

class SjfProcess extends Process implements Comparable<SjfProcess> {

  public SjfProcess(Process process) {
    super(process);
  }

  @Override
  public int compareTo(SjfProcess p) {
    int compare = Integer.compare(this.burstTime, p.burstTime);
    if (compare == 0) {
      return Integer.compare(this.arrivalTime, p.arrivalTime);
    }
    return compare;
  }
}

class SrtfProcess extends Process implements Comparable<SrtfProcess>  {

  public SrtfProcess(Process process) {
    super(process);
  }

  @Override
  public int compareTo(SrtfProcess p) {
    if (this.priority == 1 && p.priority != 1) {
      return -1;
    } else if (this.priority != 1 && p.priority == 1) {
      return 1;
    }
    int compare = Integer.compare(this.burstTime, p.burstTime);
    if (compare == 0) {
      return Integer.compare(this.arrivalTime, p.arrivalTime);
    }
    return compare;
  }
}

class PriorityProcess extends Process implements Comparable<PriorityProcess> {

  public PriorityProcess(Process process) {
    super(process);
  }

  @Override
  public int compareTo(PriorityProcess p) {
    int compare = Integer.compare(this.priority, p.priority);
    if (compare == 0) {
      return Integer.compare(this.arrivalTime, p.arrivalTime);
    }
    return compare;
  }

}

class RoundRobinProcess extends Process implements Comparable<RoundRobinProcess> {
  private int quantum;
  private int ag;

  private void setAgFactor() {
    int random = (int) (Math.random() * 20);
    if (random < 10) {
      this.ag = random + this.arrivalTime + this.burstTime;
    } else if (random > 10) {
      this.ag = 10 + this.arrivalTime + this.burstTime;
    } else {
      this.ag = priority + this.arrivalTime + this.burstTime;
    }
  }

  public RoundRobinProcess(Process process, int quantum) {
    super(process);
    this.quantum = quantum;
    setAgFactor();
  }

  public int getQuantum() {
    return quantum;
  }

  public void setQuantum(int quantum) {
    this.quantum = quantum;
  }

  @Override
  public int compareTo(RoundRobinProcess o) {
    int compare = Integer.compare(this.ag, o.ag);
    if (compare == 0) {
      return Integer.compare(this.arrivalTime, o.arrivalTime);
    }
    return compare;
  }

}

class SchedulerFactory {

  public static ArrayList<Scheduler> getAllSchedulers(int quantum, int contextSwitchingTime) {
    ArrayList<Scheduler> schedulers = new ArrayList<>();
    schedulers.add(new SjfScheduler(contextSwitchingTime));
    schedulers.add(new SrtfScheduler());
    schedulers.add(new PriorityScheduler());
    schedulers.add(new RoundRobinScheduler(quantum));
    return schedulers;
  }

}

abstract class Scheduler {
  protected TreeMap<Integer, ArrayList<Process>> arrivalTimeProcesses; // ArrivalTime -> Processes

  public Scheduler() {
    arrivalTimeProcesses = new TreeMap<>();
  }

  public void setProcesses(ArrayList<Process> processes) {
    arrivalTimeProcesses = new TreeMap<>();
    for (Process process : processes) {
      int arrivalTime = process.getArrivalTime();
      arrivalTimeProcesses.computeIfAbsent(arrivalTime, k -> new ArrayList<>());
      arrivalTimeProcesses.get(arrivalTime).add(process);
    }
  }

  public abstract ArrayList<Cluster> schedule();
}

class SjfScheduler extends Scheduler {
  private PriorityQueue<SjfProcess> readyProcesses;
  private int contextSwitchingTime;

  public SjfScheduler(int contextSwitchingTime) {
    super();
    this.contextSwitchingTime = contextSwitchingTime;
    this.readyProcesses = new PriorityQueue<>();
  }

  private void addArrivedProcessesAt(int time) {
    if (arrivalTimeProcesses.containsKey(time)) {
      ArrayList<Process> li = arrivalTimeProcesses.get(time);
      for (Process process : li) {
        SjfProcess sjfProcess = new SjfProcess(process);
        readyProcesses.add(sjfProcess);
      }
    }
  }

  private boolean isAllProcessesFinished(int t, SjfProcess runningProcess) {
    int lastArrivalTime = arrivalTimeProcesses.lastKey();
    return t > lastArrivalTime && readyProcesses.isEmpty() && runningProcess == null;
  }

  @Override
  public ArrayList<Cluster> schedule() {
    ArrayList<Cluster> clusters = new ArrayList<>();
    Cluster cluster = null;
    SjfProcess runningProcess = null;
    int t = -1;
    // Run all processes
    while (!isAllProcessesFinished(t, runningProcess)) {
      t += 1;
      if (runningProcess != null) {
        runningProcess.decreaseBurstTime();
        if (runningProcess.isFinished()) {
          runningProcess = null;
          cluster.setEndTime(t);
          clusters.add(cluster);
        }
      }
      addArrivedProcessesAt(t);
      if (runningProcess == null && !readyProcesses.isEmpty()) {
        runningProcess = readyProcesses.poll(); // runningProcess = shortestProcess
        for (int i = 0; i < contextSwitchingTime; ++i) {
          t += 1;
          addArrivedProcessesAt(t);
        }
        cluster = new Cluster(runningProcess, t);
      }
    }
    return clusters;
  }

}

class SrtfScheduler extends Scheduler {
  private PriorityQueue<SrtfProcess> readyProcesses;
  private static final int AGE = 20;

  public SrtfScheduler() {
    super();
    readyProcesses = new PriorityQueue<>();
  }

  private boolean addArrivedProcessesAt(int time) {
    if (arrivalTimeProcesses.containsKey(time)) {
      ArrayList<Process> li = arrivalTimeProcesses.get(time);
      for (Process process : li) {
        SrtfProcess srtfProcess = new SrtfProcess(process);
        srtfProcess.setPriority(10);
        readyProcesses.add(srtfProcess);
      }
      return true;
    }
    return false;
  }

  private boolean isAllProcessesFinished(int t, SrtfProcess runningProcess) {
    int lastArrivalTime = arrivalTimeProcesses.lastKey();
    return t > lastArrivalTime && readyProcesses.isEmpty() && runningProcess == null;
  }

  private void increaseOldProcessesPriority(int t) {
    ArrayList<SrtfProcess> temp = new ArrayList<>();
    readyProcesses.forEach((process) -> {
      int diff = t - process.getArrivalTime();
      if (diff % AGE == 0 && diff != 0) {
        process.increasePriority();
        temp.add(process);
      }
    });
    readyProcesses.removeAll(temp);
    readyProcesses.addAll(temp);
  }

  public ArrayList<Cluster> schedule() {
    ArrayList<Cluster> clusters = new ArrayList<>();
    Cluster cluster = null;
    SrtfProcess runningProcess = null;
    int t = -1;
    // Run all processes
    while (!isAllProcessesFinished(t, runningProcess)) {
      t += 1;
      increaseOldProcessesPriority(t);
      if (runningProcess != null) {
        runningProcess.decreaseBurstTime();
        if (runningProcess.isFinished()) {
          runningProcess = null;
          cluster.setEndTime(t);
          clusters.add(cluster);
        }
      }
      boolean isNewProcessesArrived = addArrivedProcessesAt(t);
      if (isNewProcessesArrived || runningProcess == null) {
        if (readyProcesses.isEmpty()) { continue; }
        if (runningProcess == null) {
          runningProcess = readyProcesses.poll(); // runningProcess = shortestProcess
          cluster = new Cluster(runningProcess, t);
        } else if (readyProcesses.peek().compareTo(runningProcess) < 0) {
          SrtfProcess shortestProcess = readyProcesses.poll();
          readyProcesses.add(runningProcess);
          runningProcess = shortestProcess;
          // Cluster part
          cluster.setEndTime(t);
          clusters.add(cluster);
          cluster = new Cluster(runningProcess, t);
        }
      }
    }
    return clusters;
  }

}

class PriorityScheduler extends Scheduler {
  private PriorityQueue<PriorityProcess> readyProcesses;

  private static final int AGE = 30;

  public PriorityScheduler() {
    super();
    readyProcesses = new PriorityQueue<>();
  }

  private void addArrivedProcessesAt(int time) {
    if (arrivalTimeProcesses.containsKey(time)) {
      ArrayList<Process> li = arrivalTimeProcesses.get(time);
      for (Process process : li) {
        PriorityProcess priorityProcess = new PriorityProcess(process);
        readyProcesses.add(priorityProcess);
      }
    }
  }

  private void increaseOldProcessesPriority(int t) {
    ArrayList<PriorityProcess> temp = new ArrayList<>();
    readyProcesses.forEach((process) -> {
      int diff = t - process.getArrivalTime();
      if (diff % AGE == 0 && diff != 0) {
        process.increasePriority();
        temp.add(process);
      }
    });
    readyProcesses.removeAll(temp);
    readyProcesses.addAll(temp);
  }

  public ArrayList<Cluster> schedule() {
    int lastArrivalTime = arrivalTimeProcesses.lastKey();
    Process runningProcess = null;
    ArrayList<Cluster> chart = new ArrayList<>();
    Cluster cluster = null;
    for (int t = 0; ; ++t) {
      // Stop if there is no process to run
      if (t > lastArrivalTime && readyProcesses.isEmpty() && runningProcess == null) {
        break;
      }
      // Process the current process
      if (runningProcess != null) {
        runningProcess.decreaseBurstTime();
        if (runningProcess.isFinished()) {
          cluster.setEndTime(t);
          chart.add(cluster);
          runningProcess = null;
        }
      }
      increaseOldProcessesPriority(t);
      addArrivedProcessesAt(t);
      // Run new process if there is no running process
      if (runningProcess == null) {
        runningProcess = readyProcesses.poll();
        cluster = new Cluster(runningProcess, t);
      }
    }
    return chart;
  }

}

class RoundRobinScheduler extends Scheduler {
  private LinkedList<RoundRobinProcess> readyProcesses;
  private LinkedList<RoundRobinProcess> dieProcesses;
  private PriorityQueue<RoundRobinProcess> minAgProcesses;
  private int initialQuantum;

  public RoundRobinScheduler(int quantum) {
    super();
    initialQuantum = quantum;
    readyProcesses = new LinkedList<>();
    dieProcesses = new LinkedList<>();
    minAgProcesses = new PriorityQueue<>();
  }

  private void addArrivedProcessesAt(int time) {
    if (arrivalTimeProcesses.containsKey(time)) {
      ArrayList<Process> li = arrivalTimeProcesses.get(time);
      for (Process process : li) {
        RoundRobinProcess roundRobinProcess = new RoundRobinProcess(process, initialQuantum);
        readyProcesses.add(roundRobinProcess);
        minAgProcesses.add(roundRobinProcess);
      }
    }
  }

  private boolean isAllProcessesFinished(int t, RoundRobinProcess runningProcess) {
    int lastArrivalTime = arrivalTimeProcesses.lastKey();
    return t > lastArrivalTime && readyProcesses.isEmpty() && runningProcess == null;
  }

  private double getMeanOfQuantum() {
    int sum = 0;
    for (RoundRobinProcess process : readyProcesses) {
      sum += process.getQuantum();
    }
    return sum / (double) readyProcesses.size();
  }

  private void finishRunningProcess(RoundRobinProcess runningProcess) {
    if (runningProcess == null) { return; }
    runningProcess.setQuantum(0);
    readyProcesses.remove(runningProcess);
    minAgProcesses.remove(runningProcess);
    dieProcesses.add(runningProcess);
  }

  public ArrayList<Cluster> schedule() {
    ArrayList<Cluster> clusters = new ArrayList<>();
    ClusterQ cluster = null;
    RoundRobinProcess runningProcess = null;
    int t = -1;
    // Stand on the first process
    while (runningProcess == null) {
      addArrivedProcessesAt(++t);
      runningProcess = minAgProcesses.peek();
      readyProcesses.remove(runningProcess);
    }
    // Run all processes
    while (!isAllProcessesFinished(t, runningProcess)) {
      // Cluster part
      if (cluster != null) {
        cluster.setEndTime(t);
        clusters.add(cluster);
      }
      cluster = new ClusterQ(runningProcess, t, runningProcess.getQuantum());
      int cnt = 0;
      // Non-Preemptive part
      for (; cnt < Math.ceil(runningProcess.getQuantum() / 2.0); ++cnt) {
        t += 1;
        addArrivedProcessesAt(t);
        runningProcess.decreaseBurstTime();
        if (runningProcess.isFinished()) {
          finishRunningProcess(runningProcess);
          runningProcess = readyProcesses.poll();
          cluster.setEndQ(0);
          cnt = -1;
          break;
        }
      }
      if (cnt == -1) { continue; }
      // Preemptive part
      for (; cnt < runningProcess.getQuantum(); ++cnt) {
        if (minAgProcesses.peek().compareTo(runningProcess) < 0) {
          int newQuntum = (int) Math.ceil(2 * runningProcess.getQuantum() - cnt);
          runningProcess.setQuantum(newQuntum);
          readyProcesses.add(runningProcess);
          runningProcess = minAgProcesses.peek();
          readyProcesses.remove(runningProcess);
          cluster.setEndQ(newQuntum);
          cnt = -1;
          break;
        }
        t += 1;
        addArrivedProcessesAt(t);
        runningProcess.decreaseBurstTime();
        if (runningProcess.isFinished()) {
          finishRunningProcess(runningProcess);
          runningProcess = readyProcesses.poll();
          cluster.setEndQ(0);
          cnt = -1;
          break;
        }
      }
      if (cnt == -1) { continue; }
      readyProcesses.add(runningProcess);
      int newQuntum = runningProcess.getQuantum() + (int) Math.ceil(0.1 * getMeanOfQuantum());
      runningProcess.setQuantum(newQuntum);
      runningProcess = readyProcesses.poll();
      cluster.setEndQ(newQuntum);
    }
    cluster.setEndTime(t);
    clusters.add(cluster);
    return clusters;
  }

}

class Cluster {
  protected Process process;
  protected int startTime;
  protected int endTime;

  public Cluster(Process process, int startTime) {
    this.process = process;
    this.startTime = startTime;
    this.endTime = -1;
  }

  public Process getProcess() {
    return process;
  }

  public int getEndTime() {
    return endTime;
  }

  public void setEndTime(int endTime) {
    this.endTime = endTime;
  }

  public int getTurnaroundTime() {
    return this.endTime - this.startTime;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('|');
    sb.append(startTime);
    // append half turnaround spaces
    for (int i = 0; i < Math.ceil(getTurnaroundTime() / 2.0); i++) {
      sb.append(" ");
    }
    sb.append(process.getName());
    // append the remaining spaces
    for (int i = 0; i < Math.ceil(getTurnaroundTime() / 2.0); i++) {
      sb.append(" ");
    }
    sb.append(endTime);
    sb.append('|');
    return sb.toString();
  }

}

class ClusterQ extends Cluster {
  private int startQ;
  private int endQ;

  public ClusterQ(Process p, int startTime, int startQ) {
    super(p, startTime);
    this.startQ = startQ;
    this.endQ = -1;
  }

  public void setEndQ(int endQ) {
    this.endQ = endQ;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('|');
    sb.append(startTime);
    // append half turnaround spaces
    for (int i = 0; i < Math.ceil(getTurnaroundTime() / 2.0); i++) {
      sb.append(" ");
    }
    sb.append(startQ).append("->").append(process.getName()).append("->").append(endQ);
    // append the remaining spaces
    for (int i = 0; i < Math.ceil(getTurnaroundTime() / 2.0); i++) {
      sb.append(" ");
    }
    sb.append(endTime);
    sb.append('|');
    return sb.toString();
  }
}

class Chart {
  private ArrayList<Cluster> clusters;
  private Map<Process, Integer> turnAroundTime;
  private Map<Process, Integer> waitingTime;

  public Chart(ArrayList<Cluster> clusters) {
    this.clusters = clusters;
    this.turnAroundTime = new HashMap<>();
    this.waitingTime = new HashMap<>();
    HashMap<Process, Cluster> processClusterMap = new HashMap<>();
    for (Cluster cluster : clusters) {
      Process process = cluster.getProcess();
      process.setBurstTime(process.getBurstTime() + cluster.getTurnaroundTime()); // recompute burst time
      processClusterMap.putIfAbsent(process, new Cluster(process, process.getArrivalTime()));
      processClusterMap.get(process).setEndTime(cluster.getEndTime());
    }
    for (Cluster processCluster : processClusterMap.values()) {
      Process process = processCluster.getProcess();
      this.turnAroundTime.put(process, processCluster.getTurnaroundTime());
      this.waitingTime.put(process, processCluster.getTurnaroundTime() - process.getBurstTime());
    }
  }

  public double getAvgTurnAroundTime() {
    double sum = 0;
    for (int turnAroundTime : this.turnAroundTime.values()) {
      sum += turnAroundTime;
    }
    return sum / this.turnAroundTime.size();
  }

  public double getAvgWaitingTime() {
    double sum = 0;
    for (int waitingTime : this.waitingTime.values()) {
      sum += waitingTime;
    }
    return sum / this.waitingTime.size();
  }

  public void print() {
    for (Cluster cluster : clusters) {
      System.out.print(cluster);
    }
    System.out.println();
    System.out.println("Average turnaround time: " + getAvgTurnAroundTime());
    System.out.println("Average waiting time: " + getAvgWaitingTime());
    System.out.println("Turnaround time:");
    for (var entry : turnAroundTime.entrySet()) {
      System.out.println(entry.getKey().getName() + ": " + entry.getValue());
    }
    System.out.println("Waiting time:");
    for (var entry : waitingTime.entrySet()) {
      System.out.println(entry.getKey().getName() + ": " + entry.getValue());
    }
    System.out.println("====================================");
  }

}

public class Main {
  private static Scanner in = new Scanner(System.in);
  private static int IntInput(String msg) {
    System.out.println(msg);
    return in.nextInt();
  }

  private static String StringInput(String msg) {
    System.out.println(msg);
    return in.next();
  }

  public static void main(String[] args) {
    int n = IntInput("Number of processes: ");
    int q = IntInput("Round Robin Time quantum: ");
    int c = IntInput("Context Switching Time: ");
    ArrayList<Process> processes = new ArrayList<>();
    for (int i = 0; i < n; ++i) {
      System.out.println("Enter process " + (i + 1) + ":");
      String name = StringInput("Process name: ");
      int arrivalTime = IntInput("Arrival time: ");
      int burstTime = IntInput("Burst time: ");
      int priority = IntInput("Priority: ");
      processes.add(new Process(name, arrivalTime, burstTime, priority));
    }
//    Test Case
//    int n = 4;
//    int q = 4;
//    int c = 2;
//    ArrayList<Process> processes = new ArrayList<>();
//    processes.add(new Process("P1", 0, 17, 4));
//    processes.add(new Process("P2", 3, 6, 9));
//    processes.add(new Process("P3", 4, 10, 2));
//    processes.add(new Process("P4", 29, 4, 8));
    ArrayList<Scheduler> schedulers = SchedulerFactory.getAllSchedulers(q, c);
    for (Scheduler scheduler : schedulers) {
      System.out.println(scheduler.getClass().getSimpleName());
      scheduler.setProcesses(processes);
      ArrayList<Cluster> clusters = scheduler.schedule();
      Chart chart = new Chart(clusters);
      chart.print();
    }
  }
}

