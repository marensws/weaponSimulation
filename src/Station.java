import sim.util.Bag;
public class Station {
private final int MAX_CAPACITY = 50;
public Bag weaponsStored = new Bag();
private boolean isFull() {
    return (weaponsStored.numObjs == MAX_CAPACITY);
}
public boolean visit(Weapon wp){
    if(!isFull()) {
        weaponsStored.add(wp);
        return true;
    }
    else {
        return false;
    }
}
public void leave(Weapon wp) {
    weaponsStored.removeNondestructively(wp);
}
}
