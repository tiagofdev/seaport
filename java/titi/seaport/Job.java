package titi.seaport;

import java.util.ArrayList;

/**
 * File: Job.java
 * Author: Tiago Feitosa
 * Date Last Update: 07/01/22
 * Instances of the Job class will implement the Runnable Interface.
 * Each job will be executed by a different thread.
 * Will return the duration of the job, the status of the job, whether it has requirements, and whether
 * it has found or not those necessary requirements.
 *
 */
public class Job extends Thing implements Runnable {

    // Can a ship have more than one job ??? Yes
    // Can a ship have no jobs at all ??? Originally, yes.
    // Modified the project so that each ship has at least one standard job.
    // Can a job have no requirements ??? Yes, standard jobs have no requirements
    // Should I dock the ship at all if the port does not have people to perform the job ??? Yes

    // Class fields
    private double duration;
    private ArrayList<String> requirements;
    private int progress;
    private Status jobStatus = Status.WAITING;
    static final Dock cancelDock = new Dock(); // Dummy dock

    // Parameters
    /**
     * These parameters are references of the Object structures in the main implementation.
     * They are used to synchronize and coordinate the processing of the jobs.
     */
    protected Dock dock; // null or pre-assigned // busy
    private final Ship ship;
    private final Port port;

    // Flags
    boolean goFlag = true; // Pause
    boolean cancelFlag = false; // User triggered Cancel
    boolean proceedFlag = true; // Port lack of people Cancel
    boolean hasAllSkillsFlag = true;
    boolean done = false;
    /**
     * This list will hold the people that have been assigned to the job, and that the port is holding.
     * It will later be passed to the port(monitor) to release these people(resources).
     */
    ArrayList<Person> acquiredPeople;

    // Constructor
    /**
     * Constructor
     * @param index initialization
     * @param name initialization
     * @param parent initialization
     * @param duration initialization
     * @param requirements initialization
     * @param ship reference
     * @param port reference
     * @param dock reference | null
     */
    public Job(int index, String name, int parent, double duration, ArrayList<String> requirements,
               Ship ship, Dock dock, Port port) {
        this.index = index;
        this.name = name;
        this.parent = parent;
        this.type = "Job";
        this.duration = duration;
        this.requirements = requirements;
        this.ship = ship;
        this.dock = dock;
        this.port = port;
        acquiredPeople = new ArrayList<>();
        this.progress = 0;
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
    }

    // Getters and Setters

    /**
     * Getters and Setters
     * @return duration
     */
    public double getDuration() { return this.duration; }
    public void setDuration(Double dur) { this.duration = dur; }

    public int getProgress() { return this.progress; }
    public Status getJobStatus() { return this.jobStatus; }

    public ArrayList<String> getRequirements() { return this.requirements; }
    public void setRequirements(ArrayList<String> req) { this.requirements = req; }

    /**
     * This method cancels this job running time, wherever the status of its progress is.
     */
    public void cancelJob() {
        cancelFlag = true;
        goFlag = false;
        proceedFlag = false;
        // If a job with a null dock is cancelled before finding an free dock,
        // a dummy static instance is assigned to this dock instead, in order to avoid errors in the final
        // synchronization steps of this run method, where dock cannot be null.
        if (dock == null)
            dock = cancelDock;
        jobStatus = Status.CANCELED;
    }

    /**
     * This method pauses this job running time, during the progress stage or even before it has started.
     */
    public void pauseJob() {
        goFlag = !goFlag;
        if (!goFlag) jobStatus = Status.PAUSED;
    }

    /**
     * This is the main method of the class.
     * It runs in parallel with other jobs, running in different threads.
     */
    @Override
    public void run() {
        // The following local variables will hold the time elapsed to perform the job and calculate the progress %
        long time = System.currentTimeMillis();
        long startTime = time;
        long stopTime = time + (long)(1000 * duration);
        double timeElapsed = stopTime - time;
        // First, we should check if port has all the people with the specific skills necessary to perform this job
        // If so, then we proceed with the queue
        // Otherwise, this job is immediately canceled.
        // Call function to do check.
        portHasRequirements();

        // Port is used as a monitor ( or lock ) because it holds the resources(people) to be shared
        // By convention, a thread that needs exclusive and consistent access to an object's fields has to acquire the
        // object's intrinsic lock before accessing them, and then release the intrinsic lock when it's done with them.
        synchronized (port) {
            // First, we synchronized ships to dock
            // While ship is not docked yet, either keep it in queue or find a free dock if it can dock anywhere.
            // dock is a reference which is passed in the constructor.
            // If it is null, that means that the ship can dock at any pier in the port.
            // If ship.dock is "Any" then var dock is null
            // If ship.dock is "Pier_13" then var dock holds a reference to that dock.
            // Notice that the proceedFlag is always checked at several checkpoints in order to allow the job to
            // proceed.

            // While Loop #1
            // Case if ship.dock is null
            while ( dock == null && proceedFlag ) {
                // If dock is null, then we should look for a free dock
                dock = port.findFreeDock();
                if ( dock == null ) {
                    // If dock is still null, that means, a free dock was not found. The ship will remain in queue
                    // and WAIT until a dock freed.
                    // Updating Ship Status
                    ship.shipStatus = Status.ASSIGNING;
                    // Thread waits until another thread notifies a dock has been freed.
                    try { port.wait(); }
                    catch (InterruptedException e) { e.printStackTrace(); }
                }
                // If a free dock was found, we update OTHER JOBS, which belong to the same SHIP, which now has
                // found a dock. We do this because each JOB THREAD that previously had a NULL DOCK, they did not
                // work as a reference.
                else {
                    for (Job job : ship.jobs) {
                        job.dock = dock;
                    }
                    ship.dock = "Found: " + dock.getName();
                }
            }
            // If dock was not initially null, that means the ship needs to dock at a specific pier in the port
            // After this thread found a free dock for this ship. it still holds the lock, and it proceeds to the
            // next step. Notice that it has found a free dock, but it has not docked yet.
            // ship.dock says which dock to dock
            // ship.docked says if ship has docked or not
            // Both any-dock ships and pre-assigned-dock ships proceed.
            ship.shipStatus = Status.DOCKING;
            // While Loop #2
            // Check if ship is docked
            while ( ship.docked == false && proceedFlag ) {

                // Check if desired dock is free, that is if dock has a docked ship. dock.getShip()
                // This is also a preventive step
                // Pre-assigned-dock ships were few and were read first in the file so as their threads are
                // created first in the list, they usually reach a free dock first and don't have to wait.
                // Any-dock ships which were waiting, they wake up and have just found a free dock,
                // their thread still holds the lock, so they proceed right away to this step. This works as
                // a double check to make sure the dock is still free when assigning, thus avoiding
                // any race conditions.
                if ( dock.getShip() == null ) {
                    // Assign ship to dock
                    dock.setShip(ship);
                    // Update docked boolean in Ship
                    ship.docked = true;
                } else {
                    // If there is a ship already at this dock, let's check if this job's ship is the same as the
                    // one which is already docked.
                    if ( dock.getShip() != ship ) {
                        // If this is a different ship, then this job/ship will have to wait
                        try { port.wait(); }
                        catch (InterruptedException e) { e.printStackTrace(); }
                    }
                    // Otherwise, the ship for this job is has already been docked by another job.
                    // this job can then proceed.
                    else
                        ship.docked = true;
                }
            }
            ship.shipStatus = Status.DOCKED;
            // If dock is originally null, we can assert that at this point, it will not be null because
            // it would be impossible to leave the While Loop #1 being null.
            if (dock == null) throw new AssertionError();

            // While Loop #3
            // This is optional. This is not a feature implemented in the original requirements.
            // This while loop will check if the dock is busy, meaning each job can only be done one at a time,
            // Otherwise, if this check is not present, all jobs for that ship will be done at the same time.
            while ( dock.isBusy() && proceedFlag ) {
                if (dock.isBusy()) {
                    if (goFlag)
                        jobStatus = Status.WAITING;
                    else
                        jobStatus = Status.PAUSED;
                    try { port.wait(); }
                    catch (InterruptedException e) { e.printStackTrace(); }
                }
            }
            // Once a job reaches this point, it will have the right of way and set the dock busy for itself
            dock.setBusy(true);
            // The following flag tells if the port has the necessary people, and enough people to perform the job.
            // If the flag is false, the job will skip this whole Loop #4.
            if (hasAllSkillsFlag) {
                // This flag remains true as long as the necessary people have not been assigned
                boolean waitingToProcess = true;
                // While Loop #4
                // the port has all the skills, but are they available ???
                // This while loop will check so
                while ( waitingToProcess && proceedFlag ) {
                    // This boolean will tell if all necessary people are available. We're assuming from start True
                    boolean allAvailable = true;
                    // For each required skill, let's find an available person
                    // Some jobs have no people requirements, so the following for loop would be skipped
                    for (String skill : requirements) {
                        // This person is a placeholder meaning there are no available people
                        Person person = null;
                        try {
                            // The monitor port will return a person if semaphore has available permits
                             person = port.findPerson(skill);
                        }
                        catch (InterruptedException ex) { ex.printStackTrace(); }
                        // If any of the necessary people was not found, it is safe to release all people
                        // that have been held. The job can only proceed if all people are available.
                        // If a person was not found
                        if (person ==  null) {
                            // We won't hold any resources, so we'll release any that were on hold
                            port.releasePeople(acquiredPeople);
                            // Flag is set to false
                            allAvailable = false;
                            acquiredPeople.clear();
                            // Breaking out of the For loop
                            // Waiting loop is still true
                            break;
                        }
                        else {
                            // If a person was returned, it was added to the list of acquired people
                            acquiredPeople.add(person);
                        }
                    }
                    // At the end of the For Loop, if the flag has not been set to false,
                    // all necessary people have been found.
                    if (allAvailable) {
                        // Set flag to false in order to exit Loop #4 and proceed to job execution.
                        waitingToProcess = false;
                    }
                    // Otherwise, have the job wait in line, until people are released by other jobs.
                    // The downside of this implementation is that, if people are not found for this job,
                    // it will still be in priority queue, and will not allow other jobs of the same ship to
                    // execute first.
                    else {
                        try { port.wait(); }
                        catch (InterruptedException e) { e.printStackTrace(); }
                    }
                }
            }
            // If port does not have necessary/enough workers for this job, this job will skip the progress step,
            // and pass through to final synchronization checks.
            else {
                jobStatus = Status.UNAVAILABLE;
                proceedFlag = false;
            }
        }

        // This is a non-synchronized While Loop
        // Once, ship is docked, dock is not busy, resources are found, the job is finally ready to progress.
        // time = current time
        // stopTime = current time + duration
        while (time < stopTime && proceedFlag) {
            // The sleep method puts the thread to sleep for 100 milliseconds, thus simulating job execution.
            try { Thread.sleep (100); }
            catch (InterruptedException e) { e.printStackTrace(); }
            // goFlag tells if job is paused or not
            if (goFlag) {
                jobStatus = Status.PROCESSING;
                // The current time is added 400 seconds.
                // Notice that time holds an arbitrary and not realistic amount of passed which is
                // different from the actual time that has passed which is 100 milliseconds in sleep
                time += 400;
                // Then,  we calculate the percentage of job progress based on how much time has been elapsed
                progress = (int) ( ( (time - startTime) / timeElapsed ) * 100 );
            // If paused, time will not increment
            } else {
                // This allows job to be cancelled even if it is paused
                if (cancelFlag) jobStatus = Status.CANCELED;
                else jobStatus = Status.PAUSED;
            }
        }
        // If job has not been canceled for lack of people, then we can set status to finished
        if (proceedFlag) {
            progress = 100;
            jobStatus = Status.FINISHED;
        }
        else {
            // This avoids the status being overwritten in case it was previously set to UNAVAILABLE
            if (cancelFlag) jobStatus = Status.CANCELED;
        }

        // Final synchronization checks, this is where the job will notify other jobs when it is done.
        synchronized (port) {
            // Job has been done
            done = true;
            // After processing, release people
            port.releasePeople(acquiredPeople);
            acquiredPeople.clear();
            // Set dock to not busy, allows other jobs to use this dock
            dock.setBusy(false);

            // This flag tells if other jobs of the same ship have been done or not
            boolean allDone = true;
            // For each job of the same ship
            for (Job job : ship.jobs) {
                // If any job has not been completed yet
                if (job.done == false) {
                    // Then the flag will be set to false
                    allDone = false;
                    // Break out of the For Loop
                    break;
                }
            }
            // Then let's check if all jobs have been done or not
            // If so
            if (allDone) {
                // Set ship status to DEPARTED
                ship.shipStatus = Status.DEPARTED;
                // Release dock
                dock.setShip(null);
            }
            // Notify all other threads. This is done only once during entire synchronization.
            port.notifyAll();
        }
    }

    @Override
    public void setAdditionalInformation() {
        this.info = "Requirements: " + requirements.toString() + " __ Duration: " + duration;
    }

    // Counters
    /**
     * These are counters to each different requirement of this job.
     * the ArrayList<String> requirements has the list of requirements. This list is counted and the results
     * are stored in the following counters:
     */
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

    /**
     * For each skill in requirements, counters are incremented for each profession.
     * E.g.: If requirements list has : cleaner, painter, painter, mechanic.
     * Then the job will need 2 painters to be executed.
     */
    public void countRequirements() {
        requirements.forEach((requirement) -> {
            switch (requirement) {
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
        });
    }

    /**
     * This method checks if port has necessary/enough people to perform this job
     */
    public void portHasRequirements() {
        countRequirements();
        // For each requirement, check if port has a minimum number of people with that skill
        for (String skill : requirements) {
            switch (skill) {
                case "driver":
                    hasAllSkillsFlag = port.hasSkill(skill, driverC);
                    break;
                case "carpenter":
                    hasAllSkillsFlag = port.hasSkill(skill, carpenterC);
                    break;
                case "painter":
                    hasAllSkillsFlag = port.hasSkill(skill, painterC);
                    break;
                case "clerk":
                    hasAllSkillsFlag = port.hasSkill(skill, clerkC);
                    break;
                case "inspector":
                    hasAllSkillsFlag = port.hasSkill(skill, inspectorC);
                    break;
                case "captain":
                    hasAllSkillsFlag = port.hasSkill(skill, captainC);
                    break;
                case "mechanic":
                    hasAllSkillsFlag = port.hasSkill(skill, mechanicC);
                    break;
                case "stevedore":
                    hasAllSkillsFlag = port.hasSkill(skill, stevedoreC);
                    break;
                case "crew":
                    hasAllSkillsFlag = port.hasSkill(skill, crewC);
                    break;
                case "janitor":
                    hasAllSkillsFlag = port.hasSkill(skill, janitorC);
                    break;
                case "craneOperator":
                    hasAllSkillsFlag = port.hasSkill(skill, craneOperatorC);
                    break;
                case "electrician":
                    hasAllSkillsFlag = port.hasSkill(skill, electricianC);
                    break;
                case "cleaner":
                    hasAllSkillsFlag = port.hasSkill(skill, cleanerC);
                    break;
                case "mate":
                    hasAllSkillsFlag = port.hasSkill(skill, mateC);
                    break;
                case "engineer":
                    hasAllSkillsFlag = port.hasSkill(skill, engineerC);
                    break;
            }
            // This breaks out of the For Loop
            if (hasAllSkillsFlag == false) break;
        }
    }

}