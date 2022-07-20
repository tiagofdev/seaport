package titi.seaport;

import java.util.ArrayList;

public class World extends Thing {
    private ArrayList<Port> ports;
    private int time;

    public World() {
        ports = new ArrayList<>();
    }

    public void addPort(Port port) {
        ports.add(port);
    }

    public void setTime(int time) {
        this.time = time;
    }
    public int getTime() {
        return time;
    }

    public void setAdditionalInformation() { }
}
