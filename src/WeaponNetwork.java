import sim.field.network.Edge;
import sim.field.network.Network;
import sim.util.Bag;

public class WeaponNetwork extends Network {
    private int id;
    private Bag nodes;
    public WeaponNetwork(int id) {
        this.id = id;
    }
    public void add(Weapon wp) {
        nodes = this.getAllNodes();
        this.addNode(wp);
        for(int x = 0; x < nodes.numObjs; x++) {
            this.addEdge(new Edge(nodes.get(x), wp, ""));
        }
    }
    public int getID() {
        return id;
    }
}
