import sim.engine.*;
import sim.field.continuous.Continuous2D;
import sim.util.*;
import sim.field.network.*;

public class MapState extends SimState {
    public Continuous2D map = new Continuous2D(1.0,100,100);
    public int numWeapons=50;
    public int numStations = 7; //must be 6 or more
    public Bag weaponNetworks = new Bag();
    public Network weapons = new Network(false);
    public Network stations = new Network (false);
    public static int weaponID = 1;

    public MapState(long seed) {
        super(seed);
    }

    public void start() {
        super.start();
        map.clear();
        //add stations to the simulation
        for(int i = 0; i < numStations; i++) {
            Station station = new Station();
            stations.addNode(station);
            map.setObjectLocation(station, new Double2D(map.getWidth() * random.nextDouble(),
                    random.nextDouble()*map.getHeight()));
        }
        //add weapons to the simulation
        Bag stationList = stations.getAllNodes();
        for(int i = 0; i < numWeapons; i++) {
            WeaponNetwork wn = new WeaponNetwork(weaponID);
            weaponNetworks.add(wn);
            Weapon weapon = new Weapon(weaponID, wn);
            wn.add(weapon);
            weaponID++;
            Object station = weapon.findRandomStation(this);
            map.setObjectLocation(weapon,
                    map.getObjectLocation(station));
            weapon.planNewRoute(this, weapon.findRandomStation(this));
            weapons.addNode(weapon);
            weapon.setStopper(schedule.scheduleRepeating(weapon));
        }
    }

    public void finish() {
        super.finish();
        map.remove(map.getAllObjects());
        map.clear();
    }

    public static void main(String[] args) {
        doLoop(MapState.class, args);
        System.exit(0);
    }
}

