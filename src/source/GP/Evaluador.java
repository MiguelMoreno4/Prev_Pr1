package source.GP;

import source.AST.Nodo;
import source.View.Mapa;
import source.View.Rover;

public class Evaluador {
    
    // Reglas estrictas del PDF
    public static final int MAX_TICKS = 150;
    
    // Semillas para los 3 escenarios. El PDF exige la 3000. 
    // Usamos la 3000, 3001 y 3002 para que el fitness sea robusto.
    public static final long[] SEMILLAS = {3000, 3001, 3002}; 
    
    // Coeficiente para castigar el tamaño del árbol (Bloating)
    // Ajústalo si ves que los árboles crecen demasiado.
    public static final double COEF_BLOATING = 0.5; 

    /**
     * Coge un árbol AST, lo simula en 3 mapas y devuelve su puntuación final.
     */
    public static double evaluarIndividuo(Nodo arbol) {
        double fitnessTotal = 0;

        // Probamos el mismo cerebro en los 3 mapas
        for (long semilla : SEMILLAS) {
            Mapa mapa = new Mapa(semilla);
            Rover rover = new Rover();

            int tick = 0;
            // El bucle de vida del Rover: Máximo 150 ticks o hasta que muera la batería
            while (tick < MAX_TICKS && !rover.estaApagado()) {
                
                // Leemos el árbol desde la raíz. 
                // Recordamos que si devuelve 'true' ejecutó algo físico, si devuelve 'false' fue un IF vacío.
                // En cualquier caso, el tick de reloj pasa.
                arbol.ejecutar(rover, mapa);
                
                tick++;
            }

            // Sumamos lo que haya conseguido en este mapa
            fitnessTotal += rover.calcularFitnessBase();
        }

        // 1. Calculamos la media de los 3 mapas
        double fitnessMedio = fitnessTotal / SEMILLAS.length;

        // 2. Calculamos el castigo por Bloating (Penalización por tamaño)
        int numeroDeNodos = arbol.contarNodos();
        double castigoBloating = numeroDeNodos * COEF_BLOATING;

        // 3. Fitness Final (No queremos que el fitness sea negativo, así que lo limitamos a 0)
        double fitnessFinal = fitnessMedio - castigoBloating;
        
        return Math.max(0, fitnessFinal);
    }
}