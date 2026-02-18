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

        // Población inicial
        for (int i = 0; i < 30; i++) {
            Individuo ind = crearAleatorio();
            ind.fitness = modoPonderado
                    ? calcularFitnessPonderado(ind)
                    : calcularFitness(ind);
            poblacion.add(ind);
        }

        Individuo mejorGlobal = copiarIndividuo(poblacion.get(0));

        for (int g = 0; g < generaciones; g++) {

            poblacion.sort((a, b) -> Double.compare(b.fitness, a.fitness));
            
            //para la grafica
            double mejorGeneracion = poblacion.get(0).fitness;

            double mediaGen = 0;
            for (Individuo ind : poblacion)
                mediaGen += ind.fitness;
            mediaGen /= poblacion.size();
            //sigue 
            Individuo elite = copiarIndividuo(poblacion.get(0));

            if (elite.fitness > mejorGlobal.fitness) {
                mejorGlobal = copiarIndividuo(elite);
            }

            // 🔵 ENVIAR DATOS A LA GRÁFICA
            if (ventana != null) {
                ventana.actualizarGrafica(
                        mejorGeneracion,
                        mejorGlobal.fitness,
                        mediaGen
                );

                ventana.actualizarMapaEnTiempoReal(elite, g);
            }
            try { Thread.sleep(10); } catch (Exception e) {}

            ArrayList<Individuo> nueva = new ArrayList<>();
            nueva.add(copiarIndividuo(elite));

            while (nueva.size() < poblacion.size()) {
                Individuo padre = torneo(poblacion);
                Individuo hijo = copiarIndividuo(padre);

                if (rnd.nextDouble() < probMutacion)
                    mutar(hijo);

                hijo.fitness = modoPonderado
                        ? calcularFitnessPonderado(hijo)
                        : calcularFitness(hijo);

                nueva.add(hijo);
            }

            poblacion = nueva;
        }

        return copiarIndividuo(mejorGlobal);
    }

    private Individuo crearAleatorio() {
        Individuo ind = new Individuo();

        while (ind.camaras.size() < numCamaras) {
            int x = rnd.nextInt(mapa.columnas);
            int y = rnd.nextInt(mapa.filas);

            if (!mapa.esObstaculo(x, y))
                ind.camaras.add(new Camara(x, y));
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
        for (Camara c : ind.camaras)
            copia.camaras.add(new Camara(c.x, c.y));
        copia.fitness = ind.fitness;
        return copia;
    }

    public int calcularFitness(Individuo ind) {
        HashSet<String> vigiladas = new HashSet<>();

        for (Camara c : ind.camaras) {

            if (mapa.esObstaculo(c.x, c.y))
                continue;

            vigiladas.add(c.x + "," + c.y);

            int[][] dirs = { {0,-1}, {0,1}, {1,0}, {-1,0} };

            for (int[] d : dirs) {
                for (int i = 1; i <= rango; i++) {
                    int nx = c.x + d[0]*i;
                    int ny = c.y + d[1]*i;

                    if (nx < 0 || nx >= mapa.columnas ||
                        ny < 0 || ny >= mapa.filas)
                        break;

                    if (mapa.esObstaculo(nx, ny))
                        break;

                    vigiladas.add(nx + "," + ny);
                }
            }
        }

        return vigiladas.size();
    }

    public int calcularFitnessPonderado(Individuo ind) {
        return calcularFitness(ind); // simplificado aquí
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