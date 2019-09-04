import sim.engine.SimState;
import sim.field.network.Network;

public class Warhead extends Weapon {

    public Warhead(int weaponID) {
        super(weaponID);
        super.assembled = false;
    }
}
