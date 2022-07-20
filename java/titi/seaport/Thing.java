package titi.seaport;

public abstract class Thing {

    protected int index;
    protected String type;
    protected String name;
    protected int parent;
    protected String info;

    // get and set methods
    public void setName(String str) {
        name = str;
    }
    public String getName() {
        return name;
    }

    public void setIndex(int i) {
        index = i;
    }
    public int getIndex() {
        return index;
    }

    public void setParent(int p) {
        parent = p;
    }
    public int getParent() {
        return parent;
    }

    public void setType(int index) {
        if (index == 0) type = "Date";
        else if (index >= 10000 && index < 20000) type = "Port";
        else if (index >= 20000 && index < 30000) type = "Dock";
        else if (index >= 30000 && index < 40000) type = "Passenger Ship";
        else if (index >= 40000 && index < 50000) type = "Cargo Ship";
        else if (index >= 50000 && index < 60000) type = "Person";
        else if (index >= 60000 && index < 90000) type = "Job";
        else type = "Type not identified";
    }
    public String getType() {
        return type;
    }

    public String getInfo() { return info; }

    public abstract void setAdditionalInformation();


}
