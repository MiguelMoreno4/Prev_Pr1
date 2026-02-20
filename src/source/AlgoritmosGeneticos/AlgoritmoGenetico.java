package source.AlgoritmosGeneticos;

import java.util.*;
import source.Camaras.Camara;
import source.Individuos.Individuo;
import source.View.Mapa;
import source.View.VentanaPrincipal;

public class AlgoritmoGenetico {

    private Mapa mapa;
    private int rango;
    private int numCamaras;
    private Random rnd = new Random();
    private boolean modoPonderado = false;
    private int[][] importancia; 
    private VentanaPrincipal ventana;

    public AlgoritmoGenetico(Mapa mapa, int rango, int numCamaras, VentanaPrincipal ventana) {
        this.mapa = mapa;
        this.rango = rango;
        this.numCamaras = numCamaras;
        this.ventana = ventana;
    }

    public void setModoPonderado(boolean ponderado) {
        this.modoPonderado = ponderado;
    }

    public void setImportancia(int[][] importancia) {
        this.importancia = importancia;
    }

    public Individuo ejecutar(int generaciones, double probMutacion) {
        ArrayList<Individuo> poblacion = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            Individuo ind = crearAleatorio();
            ind.fitness = calcularFitness(ind, this.modoPonderado);
            poblacion.add(ind);
        }

        Individuo mejorGlobal = copiarIndividuo(poblacion.get(0));

        for (int g = 0; g < generaciones; g++) {
            poblacion.sort((a, b) -> Double.compare(b.fitness, a.fitness));
            
            double mejorGeneracion = poblacion.get(0).fitness;
            double mediaGen = 0;
            for (Individuo ind : poblacion) mediaGen += ind.fitness;
            mediaGen /= poblacion.size();

            Individuo elite = copiarIndividuo(poblacion.get(0));

            if (elite.fitness > mejorGlobal.fitness) {
                mejorGlobal = copiarIndividuo(elite);
            }

            if (ventana != null) {
                ventana.actualizarGrafica(mejorGeneracion, mejorGlobal.fitness, mediaGen);
                ventana.actualizarMapaEnTiempoReal(elite, g);
            }

            try { Thread.sleep(10); } catch (Exception e) {}

            ArrayList<Individuo> nueva = new ArrayList<>();
            nueva.add(copiarIndividuo(elite)); 

            while (nueva.size() < poblacion.size()) {
                Individuo padre = torneo(poblacion);
                Individuo hijo = copiarIndividuo(padre);

                if (rnd.nextDouble() < probMutacion) {
                    mutar(hijo);
                }

                hijo.fitness = calcularFitness(hijo, this.modoPonderado);
                nueva.add(hijo);
            }
            poblacion = nueva;
        }

        return mejorGlobal;
    }

    /**
     * CALCULO DE FITNESS MEJORADO
     * Ahora lee directamente el valor de la matriz de importancia.
     */
    public double calcularFitness(Individuo ind, boolean ponderado) {
        HashSet<String> vigiladas = obtenerCeldasVigiladas(ind);
        
        // CASO A: MODO NORMAL (Sin ponderar)
        // Simplemente devolvemos el número de celdas únicas en el HashSet.
        if (!ponderado) {
            return (double) vigiladas.size(); 
        }

        // CASO B: MODO PONDERADO
        double fitnessTotal = 0;
        for (String pos : vigiladas) {
            String[] coords = pos.split(",");
            int vx = Integer.parseInt(coords[0]);
            int vy = Integer.parseInt(coords[1]);
            
            // Obtenemos el valor de importancia (0, 1, 5, 10, 15, 20...)
            int valor = mapa.getValorImportancia(vx, vy);
            
            // IMPORTANTE: Si la celda es normal (valor 0 o 1), debe sumar 1 punto.
            // Si la celda es especial (valor > 1), sumamos su valor de importancia.
            if (valor > 1) {
                fitnessTotal += (double) valor;
            } else {
                fitnessTotal += 1.0; 
            }
        }
        
        return fitnessTotal;
    }

    private HashSet<String> obtenerCeldasVigiladas(Individuo ind) {
        HashSet<String> vigiladas = new HashSet<>();
        for (Camara c : ind.camaras) {
            int cx = (int) Math.floor(c.x);
            int cy = (int) Math.floor(c.y);
            if (mapa.esObstaculo(cx, cy)) continue;

            vigiladas.add(cx + "," + cy);

            int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
            for (int[] d : dirs) {
                // Usamos el rango definido en la clase o un valor alto por defecto
                int alcanceMax = (rango > 0) ? rango : Math.max(mapa.columnas, mapa.filas);
                for (int i = 1; i <= alcanceMax; i++) {
                    int nx = cx + (d[0] * i);
                    int ny = cy + (d[1] * i);

                    if (nx < 0 || nx >= mapa.columnas || ny < 0 || ny >= mapa.filas) break;
                    if (mapa.esObstaculo(nx, ny)) break;

                    vigiladas.add(nx + "," + ny);
                }
            }
        }
        return vigiladas;
    }

    private Individuo crearAleatorio() {
        Individuo ind = new Individuo();
        while (ind.camaras.size() < numCamaras) {
            int x = rnd.nextInt(mapa.columnas);
            int y = rnd.nextInt(mapa.filas);
            if (!mapa.esObstaculo(x, y)) {
                ind.camaras.add(new Camara(x, y));
            }
        }
        return ind;
    }

    private Individuo torneo(List<Individuo> poblacion) {
        Individuo a = poblacion.get(rnd.nextInt(poblacion.size()));
        Individuo b = poblacion.get(rnd.nextInt(poblacion.size()));
        return (a.fitness >= b.fitness) ? a : b;
    }

    private void mutar(Individuo ind) {
        if (ind.camaras.isEmpty()) return;
        int idx = rnd.nextInt(ind.camaras.size());
        Camara c = ind.camaras.get(idx);
        int x, y;
        do {
            x = rnd.nextInt(mapa.columnas);
            y = rnd.nextInt(mapa.filas);
        } while (mapa.esObstaculo(x, y));
        c.x = x;
        c.y = y;
    }

    private Individuo copiarIndividuo(Individuo ind) {
        Individuo copia = new Individuo();
        for (Camara c : ind.camaras) copia.camaras.add(new Camara(c.x, c.y));
        copia.fitness = ind.fitness;
        return copia;
    }
}