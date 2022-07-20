package titi.seaport;

public class Dock extends Thing {
    private Ship dockedShip;

    private boolean busy = false;

    public Dock () {
        this.dockedShip = null;
    }

    public void setShip(Ship s) {
        dockedShip = s;
    }
    public Ship getShip() {
        return dockedShip;
    }

    public Boolean isBusy() { return busy; }
    public void setBusy(Boolean busy) { this.busy = busy; }

    public void setAdditionalInformation() {

    }

}
