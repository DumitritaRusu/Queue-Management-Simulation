package org.example.businesslogic;

import org.example.model.Task;
import org.example.gui.SimulationFrame;
import javax.swing.SwingUtilities;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class SimulationManager implements Runnable {

    private final int timeLimit;
    private final int maxProcessingTime;
    private final int minProcessingTime;
    private final int numberOfServers;
    private final int numberOfClients;
    private final int minArrivalTime;
    private final int maxArrivalTime;
    private final SelectionPolicy selectionPolicy;
    private final SimulationFrame frame;
    private final Scheduler scheduler;
    private final List<Task> generatedTasks;

    private static volatile int currentTime = 0;

    private int peakTime = 0;
    private int maxClientsInQueues = 0;

    private PrintWriter logWriter;

    public SimulationManager(int timeLimit,
                             int minProcessingTime,
                             int maxProcessingTime,
                             int numberOfServers,
                             int numberOfClients,
                             int minArrivalTime,
                             int maxArrivalTime,
                             SelectionPolicy selectionPolicy, SimulationFrame frame) {


        validateInput(timeLimit, minProcessingTime, maxProcessingTime,
                numberOfServers, numberOfClients, minArrivalTime, maxArrivalTime);

        this.timeLimit = timeLimit;
        this.minProcessingTime = minProcessingTime;
        this.maxProcessingTime = maxProcessingTime;
        this.numberOfServers = numberOfServers;
        this.numberOfClients = numberOfClients;
        this.minArrivalTime = minArrivalTime;
        this.maxArrivalTime = maxArrivalTime;
        this.selectionPolicy = selectionPolicy;
        this.frame = frame;
        this.scheduler = new Scheduler(numberOfServers, Integer.MAX_VALUE);
        this.scheduler.changeStrategy(selectionPolicy);

        this.generatedTasks = new ArrayList<>();
        generateNRandomTasks();

        try {
            logWriter = new PrintWriter(new FileWriter("log.txt"));
        } catch (IOException e) {
            throw new RuntimeException("Cannot open log file.", e);
        }
    }

    private void validateInput(int timeLimit,
                               int minProcessingTime,
                               int maxProcessingTime,
                               int numberOfServers,
                               int numberOfClients,
                               int minArrivalTime,
                               int maxArrivalTime) {

        if (timeLimit <= 0) {
            throw new IllegalArgumentException("Simulation time must be greater than 0.");
        }

        if (numberOfServers <= 0) {
            throw new IllegalArgumentException("Number of queues must be greater than 0.");
        }

        if (numberOfClients <= 0) {
            throw new IllegalArgumentException("Number of clients must be greater than 0.");
        }

        if (minArrivalTime < 0 || maxArrivalTime < 0) {
            throw new IllegalArgumentException("Arrival times must be non-negative.");
        }

        if (minArrivalTime > maxArrivalTime) {
            throw new IllegalArgumentException("Minimum arrival time cannot be greater than maximum arrival time.");
        }

        if (minProcessingTime <= 0 || maxProcessingTime <= 0) {
            throw new IllegalArgumentException("Service times must be greater than 0.");
        }

        if (minProcessingTime > maxProcessingTime) {
            throw new IllegalArgumentException("Minimum service time cannot be greater than maximum service time.");
        }

        if (timeLimit < maxArrivalTime) {
            throw new IllegalArgumentException("Simulation time cannot be smaller than maximum arrival time.");
        }
    }

    private void generateNRandomTasks() {
        Random random = new Random();

        for (int i = 1; i <= numberOfClients; i++) {
            int arrivalTime = random.nextInt(maxArrivalTime - minArrivalTime + 1) + minArrivalTime;
            int serviceTime = random.nextInt(maxProcessingTime - minProcessingTime + 1) + minProcessingTime;

            Task task = new Task(i, arrivalTime, serviceTime);
            generatedTasks.add(task);
        }

        generatedTasks.sort(Comparator.comparingInt(Task::getArrivalTime));
    }

    public static int getCurrentTime() {
        return currentTime;
    }

    public List<Task> getGeneratedTasks() {
        return generatedTasks;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public int getPeakTime() {
        return peakTime;
    }

    private void updatePeakTime() {
        int clientsNow = 0;
        for (Server server : scheduler.getServers()) {
            clientsNow += server.getQueueSize();
        }

        if (clientsNow > maxClientsInQueues) {
            maxClientsInQueues = clientsNow;
            peakTime = currentTime;
        }
    }

    private void dispatchArrivingClients() {
        Iterator<Task> iterator = generatedTasks.iterator();

        while (iterator.hasNext()) {
            Task task = iterator.next();

            if (task.getArrivalTime() == currentTime) {
                scheduler.dispatchTask(task);
                iterator.remove();
            } else {
                break;
            }
        }
    }

    private String buildWaitingClientsString() {
        if (generatedTasks.isEmpty()) {
            return "[]";
        }

        StringBuilder builder = new StringBuilder();
        for (Task task : generatedTasks) {
            builder.append(task).append(", ");
        }

        if (builder.length() >= 2) {
            builder.setLength(builder.length() - 2);
        }

        return builder.toString();
    }

    private void printSimulationState() {
        StringBuilder builder = new StringBuilder();

        builder.append("Time ").append(currentTime).append("\n");
        builder.append("Waiting clients:\n");
        builder.append(buildWaitingClientsString()).append("\n");

        List<Server> servers = scheduler.getServers();
        for (int i = 0; i < servers.size(); i++) {
            builder.append("Queue ").append(i + 1).append(": ");

            if (servers.get(i).isIdle()) {
                builder.append("closed");
            } else {
                builder.append(servers.get(i));
            }

            builder.append("\n");
        }

        builder.append("\n");

        String output = builder.toString();

        System.out.print(output);
        logWriter.print(output);
        logWriter.flush();
    }

    public double getAverageWaitingTime() {
        int servedClients = scheduler.getTotalServedClients();
        if (servedClients == 0) {
            return 0;
        }
        return (double) scheduler.getTotalWaitingTime() / servedClients;
    }

    public double getAverageServiceTime() {
        int servedClients = scheduler.getTotalServedClients();
        if (servedClients == 0) {
            return 0;
        }
        return (double) scheduler.getTotalServiceTime() / servedClients;
    }

    @Override
    public void run() {
        currentTime = 0;

        while (currentTime <= timeLimit) {
            dispatchArrivingClients();
            updatePeakTime();
            printSimulationState();
            updateFrame();
            if (generatedTasks.isEmpty() && scheduler.allServersEmpty()) {
                break;
            }

            for (Server server : scheduler.getServers()) {
                server.signalProcess();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            currentTime++;
        }

        scheduler.stopServers();

        StringBuilder finalStats = new StringBuilder();
        finalStats.append("Simulation finished.\n");
        finalStats.append("Average waiting time: ").append(getAverageWaitingTime()).append("\n");
        finalStats.append("Average service time: ").append(getAverageServiceTime()).append("\n");
        finalStats.append("Peak time: ").append(peakTime).append("\n");

        System.out.print(finalStats);
        logWriter.print(finalStats);
        logWriter.flush();
        logWriter.close();


        updateFinalResults();
    }

    private void updateFrame() {
        List<Task> waitingSnapshot = new ArrayList<>();
        for (Task task : generatedTasks) {
            waitingSnapshot.add(new Task(task));
        }

        List<List<Task>> queuesSnapshot = new ArrayList<>();
        for (Server server : scheduler.getServers()) {
            List<Task> oneQueue = new ArrayList<>();
            for (Task task : server.getTasks()) {
                oneQueue.add(new Task(task));
            }
            queuesSnapshot.add(oneQueue);
        }

        SwingUtilities.invokeLater(() -> {
            frame.setCurrentTime(currentTime);
            frame.updateSimulationPane(queuesSnapshot, waitingSnapshot);
        });
    }
    private void updateFinalResults() {
        String results = "Average waiting time: " + getAverageWaitingTime() + "\n"
                + "Average service time: " + getAverageServiceTime() + "\n"
                + "Peak time: " + peakTime;

        SwingUtilities.invokeLater(() -> frame.setResultsText(results));
    }

    public int getNumberOfServers() {
        return numberOfServers;
    }

    public SelectionPolicy getSelectionPolicy() {
        return selectionPolicy;
    }
}