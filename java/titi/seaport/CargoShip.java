package titi.seaport;
// Change
public class CargoShip extends Ship {
    private double cargoValue;
    private double cargoVolume;
    private double cargoWeight;

    public void setCargoValue(double v) {
        cargoValue = v;
    }
    public double getCargoValue() {
        return cargoValue;
    }

    public void setCargoVolume(double v) {
        cargoVolume = v;
    }
    public double getCargoVolume() {
        return cargoVolume;
    }

    public void setCargoWeight(double w) {
        cargoWeight = w;
    }
    public double getCargoWeight() {
        return cargoWeight;
    }

    @Override
    public void setAdditionalInformation() {
        super.setAdditionalInformation();
        this.info = this.info + " __ Cargo Weight: " + cargoWeight +
                " __ Cargo Volume: " +
                cargoVolume + " __ Cargo Value: " + cargoValue;
    }
}
