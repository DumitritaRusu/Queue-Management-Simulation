package org.example.businesslogic;

import org.example.model.Task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable {

    private final BlockingQueue<Task> tasks;
    private final AtomicInteger waitingPeriod;
    private volatile boolean running; // flag care spune daca simularea continua sau se opreste

    private int servedClientsCount;
    private int totalWaitingTime;
    private int totalServiceTime;

    private final Object lock = new Object();
    private boolean shouldProcess;

    public Server() {
        this.tasks = new LinkedBlockingQueue<>();
        this.waitingPeriod = new AtomicInteger(0);
        this.running = true;
        this.servedClientsCount = 0;
        this.totalWaitingTime = 0;
        this.totalServiceTime = 0;
        this.shouldProcess = false;
    }

    public void addTask(Task newTask) {
        tasks.add(newTask);
        waitingPeriod.addAndGet(newTask.getServiceTime());
    }

    public void signalProcess() {
        synchronized (lock) {
            shouldProcess = true;
            lock.notify();
        }
    }

    public void stop() {
        running = false;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public int getQueueSize() {
        return tasks.size();
    }

    public int getWaitingPeriod() {
        return waitingPeriod.get();
    }

    public int getServedClientsCount() {
        return servedClientsCount;
    }

    public int getTotalWaitingTime() {
        return totalWaitingTime;
    }

    public int getTotalServiceTime() {
        return totalServiceTime;
    }

    public boolean isIdle() {
        return tasks.isEmpty();
    }

    public BlockingQueue<Task> getTasks() {
        return tasks;
    }

    @Override
    public void run() {
        while (running ) {
            try {
                synchronized (lock) {
                    while (!shouldProcess && running) {
                        lock.wait(); //e pe wait pana cand primeste semnal prin notify()
                    }
                    shouldProcess = false;
                }

                if (!running && tasks.isEmpty()) {  //
                    break;
                }

                Task currentTask = tasks.peek();  // take next task from queue
                if (currentTask == null) {
                    continue;
                }

                if (currentTask.getStartServiceTime() == -1) { // seteaza timpul cand a inceput sa fie servit
                    currentTask.setStartServiceTime(SimulationManager.getCurrentTime());
                }

                currentTask.decrementServiceTime(); //decrement serviceTime and waiting period
                waitingPeriod.decrementAndGet();

                if (currentTask.isCompleted()) {  // daca e gata scoatem din coada
                    Task finishedTask = tasks.poll();
                    if (finishedTask != null) {
                        finishedTask.setFinishTime(SimulationManager.getCurrentTime() + 1);
                        servedClientsCount++; // scos din coada inseamna servit
                        totalWaitingTime += finishedTask.getWaitingTime(); // adauga la waiting time daca a fost servit
                        totalServiceTime += finishedTask.getInitialServiceTime(); // adauga la service time daca a fost servit
                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @Override
    public String toString() {
        if (tasks.isEmpty()) {
            return "closed";
        }

        StringBuilder result = new StringBuilder();
        for (Task task : tasks) {
            result.append(task).append("; ");
        }

        if (result.length() >= 2) {
            result.setLength(result.length() - 2);
        }

        return result.toString();
    }
}