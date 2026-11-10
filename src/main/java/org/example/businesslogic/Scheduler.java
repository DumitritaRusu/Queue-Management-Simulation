package org.example.businesslogic;

import org.example.model.Task;

import java.util.ArrayList;
import java.util.List;

public class Scheduler {
    private final List<Server> servers;
    private final List<Thread> serverThreads;
    private final int maxNoServers;
    private final int maxTasksPerServer;
    private Strategy strategy;

    public Scheduler(int maxNoServers, int maxTasksPerServer) {
        this.maxNoServers = maxNoServers;
        this.maxTasksPerServer = maxTasksPerServer;
        this.servers = new ArrayList<>();
        this.serverThreads = new ArrayList<>();

        for (int i = 0; i < maxNoServers; i++) { // create server object and thread with the object for maxNoServers
            Server server = new Server();
            servers.add(server);

            Thread thread = new Thread(server);
            serverThreads.add(thread);
            thread.start();
        }
    }

    public void changeStrategy(SelectionPolicy policy) {
        if (policy == SelectionPolicy.SHORTEST_QUEUE) {
            strategy = new ShortestQueueStrategy();
        } else if (policy == SelectionPolicy.SHORTEST_TIME) {
            strategy = new ShortestTimeStrategy();
        }
    }

    public void dispatchTask(Task task) {
        if (strategy != null) {  // call strategy addTask method
            strategy.addTask(servers, task);
        }
    }

    public List<Server> getServers() {
        return servers;
    }

    public List<Thread> getServerThreads() {
        return serverThreads;
    }

    public void stopServers() {
        for (Server server : servers) {
            server.stop();
        }

        for (Thread thread : serverThreads) {
            thread.interrupt();
        }

        for (Thread thread : serverThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public boolean allServersEmpty() {
        for (Server server : servers) {
            if (!server.isIdle()) {
                return false;
            }
        }
        return true;
    }

    public int getTotalServedClients() {
        int total = 0;
        for (Server server : servers) {
            total += server.getServedClientsCount();
        }
        return total;
    }

    public int getTotalWaitingTime() {
        int total = 0;
        for (Server server : servers) {
            total += server.getTotalWaitingTime();
        }
        return total;
    }

    public int getTotalServiceTime() {
        int total = 0;
        for (Server server : servers) {
            total += server.getTotalServiceTime();
        }
        return total;
    }


}
