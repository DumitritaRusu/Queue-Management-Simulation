package org.example.businesslogic;
import org.example.model.Task;
import java.util.List;
public class ShortestTimeStrategy implements Strategy{
    @Override
    public void addTask(List<Server> servers, Task task) {
        if (servers == null || servers.isEmpty() || task == null) {
            return;
        }
        Server bestServer = servers.get(0);
        for (Server server : servers) {
            if (server.getWaitingPeriod() < bestServer.getWaitingPeriod()) {
                bestServer = server;
            }
        }
        bestServer.addTask(task);
    }
}