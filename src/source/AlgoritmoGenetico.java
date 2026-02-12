package source;

import java.util.*;

public class AlgoritmoGenetico {

    private Mapa mapa;
    private int rango;
    private int numCamaras;
    private Random rnd = new Random();
    private boolean modoPonderado = false;
    private int[][] importancia;
    
    public AlgoritmoGenetico(Mapa mapa, int rango, int numCamaras) {
        this.mapa = mapa;
        this.rango = rango;
        this.numCamaras = numCamaras;
    }
    public void setModoPonderado(boolean ponderado) {
        this.modoPonderado = ponderado;
    }
    public void setImportancia(int[][] importancia) {
        this.importancia = importancia;
    }
    // ===============================
    // EJECUTAR AG
    // ===============================
    public Individuo ejecutar(int generaciones, double probMutacion) {

        ArrayList<Individuo> poblacion = new ArrayList<>();

        // Población inicial
        for (int i = 0; i < 30; i++) {
            Individuo ind = crearAleatorio();
            //ind.fitness = calcularFitness(ind);
            ind.fitness = modoPonderado
                    ? calcularFitnessPonderado(ind)
                    : calcularFitness(ind);
            poblacion.add(ind);
        }

        // MEJOR GLOBAL ABSOLUTO
        Individuo mejorGlobal = copiarIndividuo(poblacion.get(0));

        for (int g = 0; g < generaciones; g++) {

            // Ordenar por fitness
           // poblacion.sort((a, b) -> b.fitness - a.fitness);
        	// Ordenar por fitness descendente (mayor a menor)
        	poblacion.sort((a, b) -> Double.compare(b.fitness, a.fitness));
            // Mejor de la generación
            Individuo elite = copiarIndividuo(poblacion.get(0));

            // ACTUALIZAR MEJOR GLOBAL (SOLO SI MEJORA)
            if (elite.fitness > mejorGlobal.fitness) {
                mejorGlobal = copiarIndividuo(elite);
            }

            ArrayList<Individuo> nueva = new ArrayList<>();
            nueva.add(copiarIndividuo(elite)); // elitismo real

            while (nueva.size() < poblacion.size()) {
                Individuo padre = torneo(poblacion);
                Individuo hijo = copiarIndividuo(padre);

                if (rnd.nextDouble() < probMutacion) {
                    mutar(hijo);
                }

                hijo.fitness = calcularFitness(hijo);
                nueva.add(hijo);
            }

            poblacion = nueva;
        }

        // 🔒 DEVOLVER SIEMPRE EL MEJOR GLOBAL
        return copiarIndividuo(mejorGlobal);
    }

    // ===============================
    // OPERADORES GENÉTICOS
    // ===============================
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
        for (Camara c : ind.camaras) {
            copia.camaras.add(new Camara(c.x, c.y));
        }
        copia.fitness = ind.fitness;
        return copia;
    }

    // ===============================
    // FITNESS (PARTE 1)
    // ===============================
    public int calcularFitness(Individuo ind) {

        HashSet<String> vigiladas = new HashSet<>();

        for (Camara c : ind.camaras) {

            if (mapa.esObstaculo(c.x, c.y)) continue;

            vigiladas.add(c.x + "," + c.y);

            int[][] dirs = { {0,-1}, {0,1}, {1,0}, {-1,0} };

            for (int[] d : dirs) {
                for (int i = 1; i <= rango; i++) {
                    int nx = c.x + d[0]*i;
                    int ny = c.y + d[1]*i;

                    if (nx < 0 || nx >= mapa.columnas ||
                        ny < 0 || ny >= mapa.filas) break;

                    if (mapa.esObstaculo(nx, ny)) break;

                    vigiladas.add(nx + "," + ny);
                }
            }
        }

        return vigiladas.size();
    }

    public int calcularFitnessPonderado(Individuo ind) {

        HashSet<String> vigiladas = new HashSet<>();
        int fitness = 0;

        for (Camara c : ind.camaras) {

            // Cámara en obstáculo → simplemente no aporta
            if (mapa.esObstaculo(c.x, c.y)) continue;

            // Celda propia
            String keyPropia = c.x + "," + c.y;
            if (!vigiladas.contains(keyPropia)) {
                vigiladas.add(keyPropia);
                fitness += importancia[c.y][c.x];
            }

            int[][] dirs = { {0,-1}, {0,1}, {1,0}, {-1,0} };

            for (int[] d : dirs) {
                for (int i = 1; i <= rango; i++) {

                    int nx = c.x + d[0] * i;
                    int ny = c.y + d[1] * i;

                    if (nx < 0 || nx >= mapa.columnas ||
                        ny < 0 || ny >= mapa.filas) break;

                    if (mapa.esObstaculo(nx, ny)) break;

                    String key = nx + "," + ny;
                    if (!vigiladas.contains(key)) {
                        vigiladas.add(key);
                        fitness += importancia[ny][nx];
                    }
                }
            }
        }

        return fitness;
    }
}
//AG funcional para solo mapa1
/*private void ejecutarAG() {

    // ===== MAPA =====
    Mapa mapa = new Mapa(10, 10, new int[][]{
        {0,0,0,0,0,1,0,0,0,0},
        {0,1,0,0,0,0,0,0,1,0},
        {0,0,0,1,0,0,1,0,0,0},
        {0,0,0,0,0,0,0,0,0,0},
        {1,0,0,0,1,0,0,0,0,0},
        {0,0,1,0,0,0,0,1,0,0},
        {0,0,0,0,0,0,0,0,0,0},
        {0,0,1,0,0,0,0,0,0,0},
        {0,0,0,0,0,1,0,0,0,0},
        {0,0,0,0,0,0,0,1,0,0}
    });

    // ===== EJECUTAR AG =====
    AlgoritmoGenetico ag = new AlgoritmoGenetico(mapa, 3, 4);
    Individuo candidato = ag.ejecutar(300, 0.15);

    int fitnessCandidato = candidato.fitness;

    // ===== COMPARACIÓN GLOBAL =====
    if (mejorAbsoluto == null || fitnessCandidato > mejorFitnessAbsoluto) {

        // 🔒 ACTUALIZAR MEJOR ABSOLUTO
        mejorFitnessAbsoluto = fitnessCandidato;
        mejorAbsoluto = copiarIndividuo(candidato);

        // ===== ACTUALIZAR INTERFAZ =====
        textAreaResultados.setText("");
        textAreaResultados.append("MEJOR SOLUCIÓN ENCONTRADA\n");
        textAreaResultados.append("=========================\n");
        textAreaResultados.append("Fitness = " + mejorFitnessAbsoluto + "\n\n");

        for (Camara c : mejorAbsoluto.camaras) {
            textAreaResultados.append("Cámara en (" + c.x + "," + c.y + ")\n");
        }

        panelMapa.setCamaras(mejorAbsoluto.camaras);

    } else {
        // ❌ NO MEJORA → NO SE TOCA NADA
        System.out.println(
            "Solución descartada (fitness = " + fitnessCandidato +
            ", mejor = " + mejorFitnessAbsoluto + ")"
        );
    }
}*/