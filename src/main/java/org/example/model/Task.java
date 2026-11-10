package org.example.model;

public class Task {
    private final int id;
    private final int arrivalTime;
    private int serviceTime;
    private final int initialServiceTime;  //pt average trb sa stim cat era la inceput pt ca service time scade in simulare


    private int startServiceTime = -1; // pt waiting,momentul efectiv cand incepe sa fie servit
    private int finishTime = -1; // sa stim cand a iesit din coada ca sa fie considerat servit

    public Task(int id, int arrivalTime, int serviceTime) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.serviceTime = serviceTime;
        this.initialServiceTime = serviceTime;
    }
    public int getId() {
        return id;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getServiceTime() {
        return serviceTime;
    }

    public int getInitialServiceTime() {
        return initialServiceTime;
    }

    public int getStartServiceTime() {
        return startServiceTime;
    }

    public int getFinishTime() {
        return finishTime;
    }
    public void setStartServiceTime(int startServiceTime) {
        if (this.startServiceTime == -1) {
            this.startServiceTime = startServiceTime;
        }
    }

    public void setFinishTime(int finishTime) {
        this.finishTime = finishTime;
    }

    public void decrementServiceTime() {
        if (serviceTime > 0) {
            serviceTime--;
        }
    }

    public int getWaitingTime() {
        if (startServiceTime == -1) {
            return 0;
        }
        return startServiceTime - arrivalTime;
    }

    public boolean isCompleted() {
        return serviceTime == 0;
    }

    public Task(Task other) {
        this.id = other.id;
        this.arrivalTime = other.arrivalTime;
        this.serviceTime = other.serviceTime;
        this.initialServiceTime = other.initialServiceTime;
        this.startServiceTime = other.startServiceTime;
        this.finishTime = other.finishTime;
    }



    @Override
    public String toString() {
        return "(" + id + ", " + arrivalTime + ", " + serviceTime + ")";
    }

}
