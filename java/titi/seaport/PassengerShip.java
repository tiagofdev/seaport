package titi.seaport;

public class PassengerShip extends Ship {
    private int numberOfOccupiedRooms;
    private int numberOfPassengers;
    private int numberOfRooms;

    public void setNumberOfOccupiedRooms(int n) {
        numberOfOccupiedRooms = n;
    }
    public int getNumberOfOccupiedRooms() {
        return numberOfOccupiedRooms;
    }

    public void setNumberOfPassengers(int n) {
        numberOfPassengers = n;
    }
    public int getNumberOfPassengers() {
        return numberOfPassengers;
    }

    public void setNumberOfRooms(int n) {
        numberOfRooms = n;
    }
    public int getNumberOfRooms() {
        return numberOfRooms;
    }

    public void setAdditionalInformation() {
        super.setAdditionalInformation();
        this.info = this.info + " __ Number of Passengers: " + numberOfPassengers +
                " __ Number of Rooms: " + numberOfRooms + " __ Number of Occupied Rooms: " +
                numberOfOccupiedRooms;
    }



}
