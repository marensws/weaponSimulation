import sim.engine.*;
import sim.field.continuous.Continuous2D;
import sim.util.*;
import java.util.Iterator;
import java.util.Random;


public class Weapon implements Steppable {
    private final int weaponID;
    public boolean assembled = true;
    private Station destination;
    private Double2D startingPoint;
    private int timeSpentInStation;
    private Stoppable stopper;
    private WeaponNetwork wn;
    final private int speed = 1;

    public Weapon(int weaponID) {
        this.weaponID = weaponID;
        wn = null;
        startingPoint = null;
    }
    public Weapon(int weaponID, WeaponNetwork wn) {
        this.weaponID = weaponID;
        this.wn = wn;
        startingPoint = null;
    }

    public void step(SimState state) {
        MapState mapState = (MapState) state;
        Continuous2D map = mapState.map;
        //if the object does not have a destination, set a random station as the destination
        if (destination == null) {
            setStationDestination(findRandomStation(mapState));
        }
        //if the object has not started its journey, the current location is the startingPoint
        if (startingPoint == null) {
            startingPoint = map.getObjectLocation(this);
        }

        //if the object is not assembled but an instance of the Weapon class, it is removed from the map
        if(!assembled && this.getClass()==Weapon.class) {
            removeAWeaponFromMap(mapState);
        }

        if (destinationReached(mapState)){
            if(timeSpentInStation>0){
                //if the object has reached its destination, it will randomly disassemble
                if (0.005 > mapState.random.nextDouble()) {
                    disassemble(mapState);
                }
                //if it does not, it will stay at least 5 steps in the station and then leave at a random time
                else if (mapState.random.nextDouble() * timeSpentInStation > 5) {
                    timeSpentInStation = 0;
                    Station station = getDestinationObject();
                    station.leave(this);
                    planNewRoute(mapState, findRandomStation(mapState));
                }
                else {
                    timeSpentInStation++;
                }
            }
            else if (!getDestinationObject().visit(this)){
                startingPoint = map.getObjectLocation(this);
                setStationDestination(findRandomStation(mapState));
            }
            else {
                timeSpentInStation++;
            }
        }
        else {
            Double2D station = getDestinationCoordinates(mapState);
            Double2D curLoc = map.getObjectLocation(this);
            double deltaX = station.x - startingPoint.x;
            double deltaY = station.y - startingPoint.y;
            double angle = Math.atan2(deltaY, deltaX);
            double newX = curLoc.x + (speed * Math.cos(angle));
            double newY = curLoc.y + (speed * Math.sin(angle));

            Double2D nextLoc = new Double2D(newX, newY);

            map.setObjectLocation(this, nextLoc);
        }

    }

    public Double2D getDestinationCoordinates(MapState ms) {
        return ms.map.getObjectLocation(destination);
    }

    public Station getDestinationObject() { return destination;}

    public void setStationDestination(Station station) {
        destination = station;
    }

    public void planNewRoute(MapState mapState, Station station) {
        destination = station;
        startingPoint = mapState.map.getObjectLocation(this);
    }

    private boolean destinationReached(MapState mapState) {
        Double2D stationXY = getDestinationCoordinates(mapState);
        Double deltaX = Math.abs(stationXY.x - mapState.map.getObjectLocation(this).x);
        Double deltaY = Math.abs(stationXY.y - mapState.map.getObjectLocation(this).y);
        if (deltaX <= 0.5 && deltaY <= 0.5) {
            mapState.map.setObjectLocation(this, stationXY);
            return true;

        } else {
            return false;
        }
    }

    public Station findRandomStation(MapState mapState) {
        Bag stations = mapState.stations.getAllNodes();
        stations.shuffle(new Random());
        Iterator it = stations.iterator();
        Station st = (Station)it.next();
        if(st != getDestinationObject()) {
            return st;
        }
        else {
            return (Station)it.next();
        }
    }

    private void disassemble(MapState mapState) {
        if (assembled) {
            Bag stations = mapState.stations.getAllNodes();
            stations.shuffle(new Random());
            Iterator it = stations.iterator();

            Warhead warhead = new Warhead(this.weaponID);
            addAnObjectToMap(mapState, warhead, it.next());
            Container container = new Container(this.weaponID);
            addAnObjectToMap(mapState, container, it.next());
            Platform platform = new Platform(this.weaponID);
            addAnObjectToMap(mapState, platform, it.next());
            DeliverySystem ds = new DeliverySystem(this.weaponID);
            addAnObjectToMap(mapState, ds, it.next());
            FissileComponent fc = new FissileComponent(this.weaponID);
            addAnObjectToMap(mapState, fc, it.next());
            this.wn.removeNode(this);
            this.assembled = false;
            this.stopper.stop();
            getDestinationObject().leave(this);
        }
    }
        private void addAnObjectToMap (MapState mapState, Weapon weapon, Object dest){
           weapon.planNewRoute(mapState, (Station)dest);

            mapState.map.setObjectLocation(weapon, mapState.map.getObjectLocation(this));
            planNewRoute(mapState, findRandomStation(mapState));
            mapState.weapons.addNode(weapon);
            weapon.stopper = mapState.schedule.scheduleRepeating(weapon);
            wn.add(weapon);
        }
        private void removeAWeaponFromMap(MapState mapState) {
            mapState.weapons.removeNode(this);
            mapState.map.remove(this);
        }
        public int getID() {
            return weaponID;
        }

        public void setStopper(Stoppable stopper) {
            this.stopper = stopper;
        }
    }


