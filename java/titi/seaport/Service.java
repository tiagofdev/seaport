package titi.seaport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.NoSuchElementException;

public interface Service {
    Map<Integer, Ship> getStatus();
    Map<Integer, Thing> loadFile() throws IOException, NoSuchElementException;
    String start();
    String cancelAll();
    String pauseAll();
    String cancel(int job);
    String pause(int job);
    String download() throws IOException;
}
