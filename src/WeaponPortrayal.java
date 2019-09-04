import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class WeaponPortrayal extends SimplePortrayal2D{
    protected Color color;

    public WeaponPortrayal(Color color) {
        this.color = color;
    }

    public void draw(Object object, Graphics2D g, DrawInfo2D info) {
        double x = info.draw.x;
        double y = info.draw.y;
        double w = info.draw.width * 2.0D;

        Shape s = new Rectangle2D.Double(x, y, w, w);

        if(object.getClass() == Warhead.class) {
            color = Color.red;
        }
        if(object.getClass() == Container.class){
            color = Color.blue;
        }
        if(object.getClass() == Platform.class){
            color = Color.magenta;
        }
        if(object.getClass() == DeliverySystem.class) {
            color = Color.green;
        }
        if(object.getClass() == FissileComponent.class) {
            color = Color.yellow;
        }

        g.setColor(color);
        g.fill(s);
        super.draw(object, g, info);
    }
}
