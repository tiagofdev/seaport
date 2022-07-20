package titi.seaport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static java.lang.Thread.sleep;


@RequiredArgsConstructor
@org.springframework.stereotype.Service
@Transactional
@Slf4j
public class Implementation implements Service {

    File file;

    Map<Integer, Thing> things = new HashMap<>(1100);
    HashMap<Integer, Port> ports = new HashMap<> ();
    HashMap<Integer, Dock> docks = new HashMap<> ();
    Map<Integer, Ship> ships = new HashMap<> ();
    HashMap<Integer, PassengerShip> pships = new HashMap<> ();
    HashMap<Integer, CargoShip> cships = new HashMap<> ();
    HashMap<Integer, Person> people = new HashMap<> ();
    HashMap<Integer, Job> jobs = new HashMap<> ();
    World world = new World();
    ArrayList<Thread> threads = new ArrayList<>();

    @Override
    public Map<Integer, Thing> loadFile() throws IOException, NoSuchElementException {
        file = new File("src/main/java/aSPac.txt");
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                // Trimming and removing leading and trailing white spaces
                line = line.trim();
                // Replacing double space with single space in between data
                while (line.contains("  "))
                    line = line.replaceAll(" {2}", " ");
                // Once the line is formatted, we tokenize its contents into an array of strings
                String[] tokens = line.split(" ");

                if (line.startsWith("port")) {
                    // Creating new instances
                    Port port = new Port();
                    port.setName(tokens[1]);
                    port.setIndex(Integer.parseInt(tokens[2]));
                    port.setParent(Integer.parseInt(tokens[3]));
                    port.setType(Integer.parseInt(tokens[2]));
                    // Adding to corresponding HashMap structure
                    ports.put(port.getIndex(), port);
                    world.addPort(port);
                }

                else if (line.startsWith("dock")) {
                    Dock dock = new Dock();
                    dock.setName(tokens[1]);
                    dock.setIndex(Integer.parseInt(tokens[2]));
                    dock.setParent(Integer.parseInt(tokens[3]));
                    dock.setType(Integer.parseInt(tokens[2]));
                    docks.put(dock.getIndex(), dock);
                    // Once a new dock is instantiated, it is added to the
                    // list of docks in its parent port
                    Port port = ports.get(dock.getParent());
                    port.addDock(dock);

                    // Is this really necessary to update the dock info
                    // ports.put(port.getIndex(), port); Why ???????
                }

                else if (line.startsWith("pship")) {
                    PassengerShip ship = new PassengerShip();
                    ship.setName(tokens[1]);
                    ship.setIndex(Integer.parseInt(tokens[2]));
                    ship.setParent(Integer.parseInt(tokens[3]));
                    ship.setType(Integer.parseInt(tokens[2]));
                    ship.setWeight(Double.parseDouble(tokens[4]));
                    ship.setLength(Double.parseDouble(tokens[5]));
                    ship.setWidth(Double.parseDouble(tokens[6]));
                    ship.setDraft(Double.parseDouble(tokens[7]));
                    ship.setNumberOfPassengers(Integer.parseInt(tokens[8]));
                    ship.setNumberOfRooms(Integer.parseInt(tokens[9]));
                    ship.setNumberOfOccupiedRooms(Integer.parseInt(tokens[10]));

                    // Some PASSENGER ships have a port as a parent, others have a dock as
                    // parent.
                    // If parent is a dock, the ship can only dock at that specific dock
                    // If parent is a port, the ship can dock at any available dock
                    // Checking Ship parent
                    if (ship.getParent() >= 20000) {
                        // Once instantiated, the ship is referenced to dock and
                        // and its corresponding port
                        Dock dock = docks.get(ship.getParent());
                        Port port = ports.get(dock.getParent());
                        port.addQueue(ship);
                        //ports.put(port.getIndex(), port);
                        ship.setDock(dock.getName());
                    }
                    else {
                        // Ship is added to the list of ships in Port class
                        // via method addShip()
                        Port port = ports.get(ship.getParent());
                        ship.setDock("Any: ");
                        port.addQueue(ship);
                        // Not necessary to add again to map as these are all references
                        //ports.put(port.getIndex(), port);
                    }
                    pships.put(ship.getIndex(), ship);
                }

                else if (line.startsWith("cship")) {
                    CargoShip ship = new CargoShip();
                    ship.setName(tokens[1]);
                    ship.setIndex(Integer.parseInt(tokens[2]));
                    ship.setParent(Integer.parseInt(tokens[3]));
                    ship.setType(Integer.parseInt(tokens[2]));
                    ship.setWeight(Double.parseDouble(tokens[4]));
                    ship.setLength(Double.parseDouble(tokens[5]));
                    ship.setWidth(Double.parseDouble(tokens[6]));
                    ship.setDraft(Double.parseDouble(tokens[7]));
                    ship.setCargoWeight(Double.parseDouble(tokens[8]));
                    ship.setCargoVolume(Double.parseDouble(tokens[9]));
                    ship.setCargoValue(Double.parseDouble(tokens[10]));
                    ship.setDock("Any: ");
                    cships.put(ship.getIndex(), ship);
                    // Linking Ship to its parent
                    // All CARGO ships have a port as parent
                    Port port = ports.get(ship.getParent());
                    port.addQueue(ship);
                    //ports.put(port.getIndex(), port);
                }

                else if (line.startsWith("person")) {
                    Person person = new Person();
                    person.setName(tokens[1]);
                    person.setIndex(Integer.parseInt(tokens[2]));
                    person.setParent(Integer.parseInt(tokens[3]));
                    person.setType(Integer.parseInt(tokens[2]));
                    person.setSkill(tokens[4]);
                    people.put(person.getIndex(), person);
                    // Linking Person to its parent
                    Port port = ports.get(person.getParent());
                    port.addPerson(person);
                    port.setAdditionalInformation();
                    ports.put(port.getIndex(), port);
                }

                else if (line.startsWith("job")) {
                    Job job;
                    Ship ship;
                    Dock dock;
                    Port port;

                    int parent = Integer.parseInt(tokens[3]);
                    // Finding parent ship of the job
                    if (parent >= 40000) {
                        ship = cships.get(parent);
                        //cships.put(ship.getIndex(), (CargoShip) ship);
                    }
                    else {
                        ship = pships.get(parent);
                        //pships.put(ship.getIndex(), (PassengerShip) ship);
                    }

                    // Finding dock/port of the job based on parent Ship
                    if (ship.getParent() >= 20000) {
                        dock = docks.get(ship.getParent());
                        port = ports.get(dock.getParent());
                    }
                    else {
                        port = ports.get(ship.getParent());
                        dock = null;
                    }

                    // Gathering data to instantiate job
                    String name = tokens[1];
                    int index = Integer.parseInt(tokens[2]);
                    double duration = Double.parseDouble(tokens[4]);
                    ArrayList<String> requirements = new ArrayList<>();
                    for (int i = 5; i < tokens.length; i++) {
                        requirements.add(tokens[i]);
                    }

                    // Instantiating Job passing parameters
                    job = new Job(index, name, parent, duration, requirements, ship, dock, port);
                    // Adding job to HashMap
                    jobs.put(job.getIndex(), job);
                    // Adding job to list of jobs in parent Ship
                    ship.addJob(job);

                }

                else if (line.startsWith("// Date:")) {
                    world.setIndex(0);
                    String name = "";
                    name = name.concat(tokens[2] + " " + tokens[3] + " " + tokens[4]);
                    world.setName(name);
                    String[] hourMin = tokens[5].split(":");
                    int hour = Integer.parseInt(hourMin[0]);
                    int mins = Integer.parseInt(hourMin[1]);
                    int hoursInMins = hour * 60;
                    int time;
                    time = hoursInMins + mins;
                    world.setTime(time);
                    world.setType(0);
                    things.put(world.getIndex(), world);
                }

            } // End of while

            // this counts all skills(resources) in each port
            ports.forEach((key, value) -> value.countSkills());
            ships.putAll(pships);
            ships.putAll(cships);
            // For ships that have no jobs, we're going to create standard jobs so the ships can dock, be
            // processed and depart
            ships.forEach((key, value) -> {
                if (value.jobs.size() == 0) {
                    Job job;
                    if (value.getDock().equals("Any: "))
                        job = new Job(value.getIndex() + 40000, "Standard Job", value.getIndex(), 20.0,
                                new ArrayList<String>(), value, null, ports.get(value.getParent()));
                    else
                        job = new Job(value.getIndex() + 40000, "Standard Job", value.getIndex(), 20.0,
                                new ArrayList<String>(), value, docks.get(value.getParent()), ports.get(docks.get(value.getParent()).getParent()));
                    value.addJob(job);
                    jobs.put(value.getIndex() + 40000, job);
                }
            });

            things.putAll(ports);
            things.putAll(docks);
            things.putAll(ships);
            things.putAll(people);
            things.putAll(jobs);
            things.forEach((key, value) -> value.setAdditionalInformation());

            return things;
        }
    }

    private String result = "Waiting";
    private int counter = 1;

    @Override
    public String start() {
        // A thread can only be executed once, if I want to restart the process, I have to create new instances
        // of thread, for each run.
        jobs.forEach((key,value) -> {
            Thread thread = new Thread(value, value.getName());
            thread.start();
        });
        return "Started";
    }

    @Override
    public String cancelAll() {
        jobs.forEach((key,value) -> {
            value.cancelJob();
        });
        System.out.println("Did I cancel?");
        return "Canceled All";
    }

    @Override
    public String pauseAll() {
        jobs.forEach((key,value) -> {
            value.pauseJob();
        });
        System.out.println("Did I pause?");
        return "Paused All";
    }

    @Override
    public String cancel(int index) {
        jobs.get(index).cancelJob();
        return "Canceled job";
    }

    @Override
    public String pause(int index) {
        jobs.get(index).pauseJob();
        return "Paused job";
    }

    @Override
    public String download() throws IOException {
        Path path = Path.of("src/main/java/aSPac.txt");
        StringBuilder builder = new StringBuilder();
        try (Stream<String> stream = Files.lines(path, StandardCharsets.UTF_8)) {
            stream.forEach(s -> builder.append(s).append("\n"));
        }
        catch (IOException e) { e.printStackTrace(); }

        String content = builder.toString();
        return content;
    }

    /*@Override
    public String start() {

        //Implementation im = new Implementation();
        //im.run();
        counter = 1;
        result = "Waiting";
        System.out.println("waiting");
        try {
            sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("processing");
        result = "Processing";
        try {
            sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        result = "Completed";
        System.out.println("Result: " + result);
        return "Start Finished";
    }*/

    @Override
    public Map getStatus() {
        return ships;
    }



    // data file c has a bit less than 1000 items, so when creating an arrayList,
    // specify the initial size of the object to 1000 like this: new ArrayList<String>(1000);


    /* For search and sorting purposes, I believe an array of objects ArrayList<Thing> and
    array of Thing in typescript will suffice to manipulate the data.
    Data to show is:
        Index
        Type
        Name
        Parent
        ... Additional Information
        Size
        Weight
        Profession
        etc

    For the simulation, a map with an index
    Data to show is:
        Name
        Cargo or Passenger
        dock assigned and/or Port
        Jobs
        Progress
    * */

}
