package source.Camaras;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class FabricaDrones {

	public static List<Dron> crearFlota(int n, int baseX, int baseY) {
	    List<Dron> base = new ArrayList<>();

	    Dron d1 = new Dron(1.5, Color.RED);     d1.setBaseX(baseX); d1.setBaseY(baseY);
	    Dron d2 = new Dron(1.0, Color.BLUE);    d2.setBaseX(baseX); d2.setBaseY(baseY);
	    Dron d3 = new Dron(0.7, Color.GREEN);   d3.setBaseX(baseX); d3.setBaseY(baseY);
	    Dron d4 = new Dron(1.2, Color.ORANGE);  d4.setBaseX(baseX); d4.setBaseY(baseY);
	    Dron d5 = new Dron(0.5, Color.MAGENTA); d5.setBaseX(baseX); d5.setBaseY(baseY);

	    base.add(d1); base.add(d2); base.add(d3); base.add(d4); base.add(d5);

	    if (n < 1) n = 1;
	    if (n > 5) n = 5;
	    return new ArrayList<>(base.subList(0, n));
	}
}