package source.Camaras;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class FabricaDrones {

    public static List<Dron> crearFlota(int n) {

        List<Dron> base = new ArrayList<>();

        base.add(new Dron(1.5, Color.RED));      // Dron 1 - Veloz
        base.add(new Dron(1.0, Color.BLUE));     // Dron 2 - Estándar
        base.add(new Dron(0.7, Color.GREEN));    // Dron 3 - Pesado
        base.add(new Dron(1.2, Color.ORANGE));   // Dron 4 - Ágil
        base.add(new Dron(0.5, Color.MAGENTA));  // Dron 5 - Tanque

        if (n < 1) n = 1;
        if (n > 5) n = 5;

        return new ArrayList<>(base.subList(0, n));
    }
}