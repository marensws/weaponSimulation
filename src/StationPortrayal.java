import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import java.awt.*;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

public class StationPortrayal extends SimplePortrayal2D {
    MapState mapState;
    protected Color color;

    public StationPortrayal(MapState mapState, Color color) {
        this.mapState = mapState;
        this.color = color;
    }

    public void draw(Object object, Graphics2D g, DrawInfo2D info) {
        double x = info.draw.x;
        double y = info.draw.y;
        double w = info.draw.width * 2.8D;

        Shape s = new Rectangle2D.Double(x, y, w, w);
        g.setColor(this.color);
        g.fill(s);

        super.draw(object, g, info);
    }
}


