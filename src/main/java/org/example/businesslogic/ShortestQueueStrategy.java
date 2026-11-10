package org.example.businesslogic;

import org.example.model.Task;

import java.util.List;

public class ShortestQueueStrategy implements Strategy{
    @Override
    public void addTask(List<Server> servers, Task task) {
        if (servers == null || servers.isEmpty() || task == null) {
            return;
        }

        Server bestServer = servers.get(0);

        for (Server server : servers) {
            if (server.getQueueSize() < bestServer.getQueueSize()) {
                bestServer = server;
            }
        }

        bestServer.addTask(task);
    }
}
