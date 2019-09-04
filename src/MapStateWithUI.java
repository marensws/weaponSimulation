
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import sim.portrayal.*;
import sim.display.*;
import sim.engine.*;
import sim.portrayal.continuous.*;
import sim.portrayal.simple.*;
import javax.swing.*;
import sim.display.GUIState;
import sim.portrayal.network.*;
import sim.util.Bag;
import org.jfree.data.xy.XYSeries;
import sim.util.media.chart.TimeSeriesChartGenerator;

public class MapStateWithUI extends GUIState {
    public Display2D display;
    public JFrame displayFrame;
    ContinuousPortrayal2D yardPortrayal = new ContinuousPortrayal2D();
    StationPortrayal stationPortrayal;
    NetworkPortrayal2D weaponPortrayal = new NetworkPortrayal2D();
    NetworkPortrayal2D stationsPortrayal = new NetworkPortrayal2D();
    ContinuousPortrayal2D agentPortrayal = new ContinuousPortrayal2D();
    ContinuousPortrayal2D trailsPortrayal = new ContinuousPortrayal2D();
    public int selectedID = 0;
    public double doubleID;
    XYSeries series;    // the data series we'll add to
    sim.util.media.chart.TimeSeriesChartGenerator chart;
    JFrame chartFrame;
    Bag seriesList = new Bag();
    Bag stations = new Bag();



    public static void main(String[] args) {
        new MapStateWithUI().createController();
    }

    public MapStateWithUI() {
        super(new MapState(System.currentTimeMillis()));
    }

    public MapStateWithUI(SimState state) {
        super(state);
    }

    public static String getName() {
        return "Nuclear Weapon Distribution";
    }

    public Object getSimulationInspectedObject() {
        return this.state;
    }


    public void start() {
        super.start();
        setUpPortrayals();
    }

    public void load(SimState state) {
        super.load(state);
        setUpPortrayals();
    }

    public void setUpPortrayals() {
        MapState mapState = (MapState)this.state;
        this.yardPortrayal.setField(mapState.map);
        this.trailsPortrayal.setField(mapState.map);
        Bag weapons = mapState.weapons.getAllNodes();


         // goes through all weapons stored on the map and adds an imagePortrayal for the assembled weapons and
         // removes the items of Weapon.class and removes the Weapons which are not assembled.

        for(int x = 0; x < weapons.numObjs; x++) {
            Weapon wp = (Weapon) weapons.get(x);
            if(wp.assembled) {
                yardPortrayal.setPortrayalForObject(wp, new ImagePortrayal2D(this.getClass(), "images/AssembledWeapon.png", 3.0D));
            }
            else if(wp.getClass()==Weapon.class) {
                mapState.map.remove(wp);
            }
        }
        //add an imagePortrayal for each subclass of Weapon using images saved in the src folder with corresponding file names.
        yardPortrayal.setPortrayalForClass(Warhead.class, new ImagePortrayal2D(this.getClass(), "images/Warhead.png", 2.0D));
        yardPortrayal.setPortrayalForClass(Container.class, new ImagePortrayal2D(this.getClass(), "images/Container.png", 2.0D));
        yardPortrayal.setPortrayalForClass(DeliverySystem.class, new ImagePortrayal2D(this.getClass(), "images/DeliverySystem.png", 2.0D));
        yardPortrayal.setPortrayalForClass(Platform.class, new ImagePortrayal2D(this.getClass(), "images/Platform.png", 2.0D));
        yardPortrayal.setPortrayalForClass(FissileComponent.class, new ImagePortrayal2D(this.getClass(), "images/FissileComponent.png", 2.0D));
        yardPortrayal.setPortrayalForClass(Station.class,  new ImagePortrayal2D(this.getClass(), "images/Station.png", 3.0D));


        //alter the WeaponPortrayal class and uncomment the following lines to alter the appearance of the icons of the weapons
//        yardPortrayal.setPortrayalForClass(Warhead.class, new WeaponPortrayal());
//        yardPortrayal.setPortrayalForClass(Container.class, new WeaponPortrayal());
//        yardPortrayal.setPortrayalForClass(FissileComponent.class, new WeaponPortrayal());
//        yardPortrayal.setPortrayalForClass(DeliverySystem.class, new WeaponPortrayal());
//        yardPortrayal.setPortrayalForClass(Platform.class, new WeaponPortrayal());

        //highlights weapons with same ID to show the path the weapon and its subcomponents take
        highlightObjectsWithSameID(mapState);

        addDataToChart(mapState);

        this.display.reset();
        this.display.repaint();
    }

    public void init(Controller c) {
        super.init(c);
        display = new Display2D(800, 800, this);

        displayFrame = display.createFrame();
        displayFrame.setLayout(new BorderLayout());
        displayFrame.setSize(815, 820);
        displayFrame.setTitle("Weapon Display");

        JCheckBox track = new JCheckBox("Track a random weapon");
        track.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()==1) {
                    doubleID = guirandom.nextDouble();
                    setUpPortrayals();
                    display.repaint();
                }
                else{
                    doubleID = 0;
                    selectedID = 0;
                }
            }
        });

        display.header.add(track);
        displayFrame.setLocationByPlatform(true);
        displayFrame.setVisible(true);
        display.attach(weaponPortrayal, "All weapons and stations");
        display.attach(yardPortrayal, "Selected weapons");
        c.registerFrame(displayFrame);

        chart = new TimeSeriesChartGenerator();
        chart.setTitle("Station Histogram");
        chart.setRangeAxisLabel("No Items");
        chart.setDomainAxisLabel("Time");
        chartFrame = chart.createFrame();
        chartFrame.setVisible(true);
        chartFrame.pack();
        c.registerFrame(chartFrame);
    }

    public void highlightObjectsWithSameID(MapState mapState) {
        if (doubleID > 0 && selectedID == 0) {
            selectedID = (int) Math.round(doubleID * (MapState.weaponID-1));
            Bag networks = mapState.weaponNetworks;
            for(int x = 0; x <networks.numObjs; x++) {
                WeaponNetwork network = (WeaponNetwork) networks.get(x);
                if(network.getID() == selectedID) {
                    weaponPortrayal.setField( new SpatialNetwork2D( mapState.map, network));
                    weaponPortrayal.setPortrayalForAll(new SimpleEdgePortrayal2D());
                    SimplePortrayal2D basic = new WeaponPortrayal(Color.red);
                    Bag weapons = network.allNodes;

                    for(int y = 0; y<weapons.numObjs; y++){
                        Weapon wp = (Weapon) weapons.get(y);
                        if(wp.assembled) {
                            yardPortrayal.setPortrayalForObject(wp, new AdjustablePortrayal2D(new MovablePortrayal2D(basic)));
                        }
                    }
                }
            }
        }
    }

    public void addDataToChart(MapState mapState) {
        chart.removeAllSeries();
        stations = mapState.stations.getAllNodes();
        for(int x = 0; x < stations.numObjs; x++) {
            XYSeries thisSeries = new XYSeries(
                    x,
                    false);
            Station st = (Station) stations.get(x);
            thisSeries.add(st.weaponsStored.numObjs, mapState.schedule.getSteps(), true);
            seriesList.add(thisSeries);
            chart.addSeries(thisSeries, null);
        }
        scheduleRepeatingImmediatelyAfter(new Steppable() {
            public void step(SimState state)
            {
                double x = state.schedule.time();
                if (x >= state.schedule.EPOCH && x < state.schedule.AFTER_SIMULATION) {
                    for (int i = 0; i < mapState.stations.getAllNodes().numObjs; i++) {
                        series = (XYSeries) seriesList.get(i);
                        Station st = (Station) stations.get(i);
                        Double y = (double) st.weaponsStored.numObjs;
                        series.add(x, y, true);
                        chart.updateChartLater(state.schedule.getSteps());
                    }
                }
            }
        });
    }

    public void quit() {
        super.quit();
        if (this.displayFrame != null) {
            this.displayFrame.dispose();
        }
        this.displayFrame = null;
        this.display = null;
    }
}