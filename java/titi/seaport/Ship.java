package titi.seaport;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Ship extends Thing {
    int arrivalTime, dockTime;
    double draft, length, weight, width;
    ArrayList<Job> jobs;
    String dock;
    String port = "";
    boolean docked = false;
    Status shipStatus = Status.DOCKING;


    public Ship() {
        jobs = new ArrayList<>();
    }

    public void setDraft(double d) {
        draft = d;
    }
    public double getDraft() {
        return draft;
    }

    public void setLength(double l) {
        length = l;
    }
    public double getLength() {
        return length;
    }

    public void setWeight(double w) {
        weight = w;
    }
    public double getWeight() {
        return weight;
    }

    public void setWidth(double w) {
        width = w;
    }
    public double getWidth() {
        return width;
    }

    public void setDock(String dock) {
        this.dock = dock;
    }
    public String getDock() {
        return this.dock;
    }

    public void addJob(Job job) {
        jobs.add(job);
    }

    // These get and set methods are necessary to interact with the client
    // Without this get method, typescript does not receive the value for the field shipStatus
    public Status getShipStatus() { return this.shipStatus; }

    public ArrayList<Job> getJobs() {
        return jobs;
    }




    public void setAdditionalInformation() {
        this.info = "Weight: " + weight + " __ Length: " + length +
                " __ Width: " + width + " __ Draft: " + draft;
    }
}
