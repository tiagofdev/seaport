package titi.seaport;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Port extends Thing {
    protected final ArrayList<Dock> docks;
    private final ArrayList<Person> people;
    private ArrayList<Ship> queuedShips;

    // Semaphores
    ArrayList<Boolean> used;
    Semaphore driver;
    Semaphore carpenter;
    Semaphore painter;
    Semaphore clerk;
    Semaphore inspector;
    Semaphore captain;
    Semaphore mechanic;
    Semaphore stevedore;
    Semaphore crew;
    Semaphore janitor;
    Semaphore craneOperator;
    Semaphore electrician;
    Semaphore cleaner;
    Semaphore mate;
    Semaphore engineer;

    // Max count of workers for each semaphores
    int driverC;
    int carpenterC;
    int painterC;
    int clerkC;
    int inspectorC;
    int captainC;
    int mechanicC;
    int stevedoreC;
    int crewC;
    int janitorC;
    int craneOperatorC;
    int electricianC;
    int cleanerC;
    int mateC;
    int engineerC;

    public Port() {
        this.engineerC = 0;
        this.mateC = 0;
        this.cleanerC = 0;
        this.electricianC = 0;
        this.craneOperatorC = 0;
        this.janitorC = 0;
        this.crewC = 0;
        this.stevedoreC = 0;
        this.mechanicC = 0;
        this.captainC = 0;
        this.inspectorC = 0;
        this.clerkC = 0;
        this.painterC = 0;
        this.carpenterC = 0;
        this.driverC = 0;
        docks = new ArrayList<>();
        people = new ArrayList<>();
        queuedShips = new ArrayList<>();
        used = new ArrayList<>();

    }

    public boolean hasSkill( String skill, int skillRequired ) {
        switch(skill) {
            case "driver": { if (skillRequired <= driverC) return true; } break;
            case "carpenter": { if (skillRequired <= carpenterC ) return true; } break;
            case "painter": { if (skillRequired <= painterC ) return true; } break;
            case "clerk": { if (skillRequired <= clerkC ) return true; } break;
            case "inspector": { if (skillRequired <= inspectorC ) return true; } break;
            case "captain": { if (skillRequired <= captainC ) return true; } break;
            case "mechanic": { if (skillRequired <= mechanicC ) return true; } break;
            case "stevedore": { if (skillRequired <= stevedoreC ) return true; } break;
            case "crew": { if (skillRequired <= crewC ) return true; } break;
            case "janitor": { if (skillRequired <= janitorC ) return true; } break;
            case "craneOperator": { if (skillRequired <= craneOperatorC ) return true; } break;
            case "electrician": { if (skillRequired <= electricianC ) return true; } break;
            case "cleaner": { if (skillRequired <= cleanerC ) return true; } break;
            case "mate": { if (skillRequired <= mateC ) return true; } break;
            case "engineer": { if (skillRequired <= engineerC) return true; } break;
        }
        return false;
    }

    public Person findPerson(String skill) throws InterruptedException {
        boolean found = false;
        // Skill desired
        switch(skill) {
            case "driver":
                found = driver.tryAcquire();
                    break;
            case "carpenter":
                    found = carpenter.tryAcquire();
                    break;
            case "painter":
                    found = painter.tryAcquire();
                    break;
            case "clerk":
                    found = clerk.tryAcquire();
                    break;
            case "inspector":
                    found = inspector.tryAcquire();
                    break;
            case "captain":
                    found = captain.tryAcquire();
                    break;
            case "mechanic":
                    found = mechanic.tryAcquire();
                    break;
            case "stevedore":
                    found = stevedore.tryAcquire();
                    break;
            case "crew":
                    found = crew.tryAcquire();
                    break;
            case "janitor":
                    found = janitor.tryAcquire();
                    break;
            case "craneOperator":
                    found = craneOperator.tryAcquire();
                    break;
            case "electrician":
                    found = electrician.tryAcquire();
                    break;
            case "cleaner":
                    found = cleaner.tryAcquire();
                    break;
            case "mate":
                    found = mate.tryAcquire();
                    break;
            case "engineer":
                    found = engineer.tryAcquire();
                    break;
            default: System.out.println("Error Reading Skill in finding method");
        }
        if (found) {
            return getNextAvailablePerson(skill);
        }
        else
            return null;
    }

    public Person getNextAvailablePerson(String skill) {
        int count = 0;

        for (Person person : people) {
            if (!used.get(count) && person.getSkill().equals(skill)) {
                used.set(count, true);
                return people.get(count);
            }
            count++;
        }
        return null;
    }

    public void releasePeople(ArrayList<Person> used) {
        for (Person p : used) {
            if (markAsUnused(p)) {
                switch (p.getSkill()) {
                    case "driver": driver.release(); break;
                    case "carpenter": carpenter.release(); break;
                    case "painter": painter.release(); break;
                    case "clerk": clerk.release(); break;
                    case "inspector": inspector.release(); break;
                    case "captain": captain.release(); break;
                    case "mechanic": mechanic.release(); break;
                    case "stevedore": stevedore.release(); break;
                    case "crew": crew.release(); break;
                    case "janitor": janitor.release(); break;
                    case "craneOperator": craneOperator.release(); break;
                    case "electrician": electrician.release(); break;
                    case "cleaner": cleaner.release(); break;
                    case "mate": mate.release(); break;
                    case "engineer": engineer.release(); break;
                    default: System.out.println("Error Reading Skill in releasing method");
                }
            }
        }
    }

    protected boolean markAsUnused(Person p) {
        for (int i = 0; i < people.size(); i++) {
            if (p == people.get(i)) {
                if (used.get(i)) {
                    used.set(i, false);
                    return true;
                }
                else
                    return false;
            }
        }
        return false;
    }

    public void addDock(Dock dock) {
        docks.add(dock);
    }

    public void addPerson(Person person) {
        people.add(person);
    }

    public void addQueue(Ship ship) {
        queuedShips.add(ship);
    }

    public void countSkills() {

        people.forEach((p) -> {
            switch (p.getSkill()) {
                case "driver":
                    driverC++;
                    break;
                case "carpenter":
                    carpenterC++;
                    break;
                case "painter":
                    painterC++;
                    break;
                case "clerk":
                    clerkC++;
                    break;
                case "inspector":
                    inspectorC++;
                    break;
                case "captain":
                    captainC++;
                    break;
                case "mechanic":
                    mechanicC++;
                    break;
                case "stevedore":
                    stevedoreC++;
                    break;
                case "crew":
                    crewC++;
                    break;
                case "janitor":
                    janitorC++;
                    break;
                case "craneOperator":
                    craneOperatorC++;
                    break;
                case "electrician":
                    electricianC++;
                    break;
                case "cleaner":
                    cleanerC++;
                    break;
                case "mate":
                    mateC++;
                    break;
                case "engineer":
                    engineerC++;
                    break;
                default:
                    System.out.println("Error Reading Skill in counting method");
            }
            used.add(false);
        });
        // Initializing semaphores
        driver = new Semaphore(driverC, true);
        carpenter = new Semaphore(carpenterC, true);
        painter = new Semaphore(painterC, true);
        clerk = new Semaphore(clerkC, true);
        inspector = new Semaphore(inspectorC, true);
        captain = new Semaphore(captainC, true);
        mechanic = new Semaphore(mechanicC, true);
        stevedore = new Semaphore(stevedoreC, true);
        crew = new Semaphore(crewC, true);
        janitor = new Semaphore(janitorC, true);
        craneOperator = new Semaphore(craneOperatorC, true);
        electrician = new Semaphore(electricianC, true);
        cleaner = new Semaphore(cleanerC, true);
        mate = new Semaphore(mateC, true);
        engineer = new Semaphore(engineerC, true);
    }

    public Dock findFreeDock() {
        for (Dock dock : docks) {
            if (dock.getShip() == null)
                return dock;
        }
        return null;
    }

    public void setAdditionalInformation() {
        this.info = "Number of docks: " + docks.size() + " __ Number of workers: " + people.size();
    }
}
