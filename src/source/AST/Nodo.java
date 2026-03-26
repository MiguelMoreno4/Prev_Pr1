package source.AST;

import source.View.Mapa;
import source.View.Rover;

public interface Nodo {
    // Devuelve TRUE si el Rover ha realizado una acción física en este tick
    boolean ejecutar(Rover rover, Mapa mapa);
    
    // Necesario para la mutación y para imprimir el código final
    Nodo clonar();
    String imprimir(String tab);
    int contarNodos(); // Vital para calcular el castigo por Bloating
}