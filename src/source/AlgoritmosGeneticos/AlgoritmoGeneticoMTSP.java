package source.AlgoritmosGeneticos;

import java.util.*;
import java.awt.Color;

import source.Camaras.Camara;
import source.Camaras.Dron;
import source.Individuos.IndividuoMTSP;
import source.View.AStar;
import source.View.Mapa;
import source.View.VentanaPrincipal;

public class AlgoritmoGeneticoMTSP {

    private Mapa mapa;
    private List<Camara> puntosControl;
    private int numDrones;
    private List<Dron> flota;
    private AStar aStar;
    private int tamPoblacion = 50;
    private Random rnd;
    private VentanaPrincipal ventana;

    public enum MetodoSeleccion { TORNEO, RULETA }
    public enum MetodoCruce { OX, PMX }
    public enum MetodoMutacion { INTERCAMBIO, INVERSION }

    private MetodoSeleccion metodoSeleccion = MetodoSeleccion.TORNEO;
    private MetodoCruce metodoCruce = MetodoCruce.OX;
    private MetodoMutacion metodoMutacion = MetodoMutacion.INTERCAMBIO;

    public AlgoritmoGeneticoMTSP(Mapa mapa,
                                 List<Camara> puntosControl,
                                 int numDrones,
                                 long semilla,
                                 VentanaPrincipal ventana) {
        this.mapa = mapa;
        this.puntosControl = puntosControl;
        this.numDrones = numDrones;
        this.ventana = ventana;
        this.rnd = new Random(semilla);
        this.aStar = new AStar(mapa);
        inicializarDrones();
    }

    private void inicializarDrones() {
        flota = new ArrayList<>();
        double[] velocidades = {1.5, 1.0, 0.7, 1.2, 0.5};
        for (int i = 0; i < numDrones; i++) {
            flota.add(new Dron(velocidades[i % velocidades.length], 
                Color.getHSBColor(i * 0.15f, 1f, 1f)));
        }
    }

    public IndividuoMTSP ejecutar(int generaciones, double probCruce, double probMutacion) {
        List<IndividuoMTSP> poblacion = crearPoblacionInicial();
        IndividuoMTSP mejorGlobal = obtenerMejor(poblacion).copiar();

        for (int g = 0; g < generaciones; g++) {
            IndividuoMTSP mejorGen = obtenerMejor(poblacion);
            if (mejorGen.fitness < mejorGlobal.fitness) {
                mejorGlobal = mejorGen.copiar();
            }

            double media = poblacion.stream().mapToDouble(i -> i.fitness).average().orElse(0);

            if (ventana != null) {
                ventana.actualizarGrafica(mejorGen.fitness, mejorGlobal.fitness, media);
            }

            List<IndividuoMTSP> nueva = new ArrayList<>();
            while (nueva.size() < tamPoblacion) {
                IndividuoMTSP p1 = seleccionar(poblacion);
                IndividuoMTSP hijo;

                if (rnd.nextDouble() < probCruce) {
                    IndividuoMTSP p2 = seleccionar(poblacion);
                    hijo = cruzar(p1, p2);
                } else {
                    hijo = p1.copiar();
                }

                if (rnd.nextDouble() < probMutacion) {
                    mutar(hijo);
                }

                hijo.fitness = calcularFitness(hijo);
                nueva.add(hijo);
            }
            poblacion = nueva;
        }
        return mejorGlobal;
    }

    private List<IndividuoMTSP> crearPoblacionInicial() {
        List<IndividuoMTSP> poblacion = new ArrayList<>();
        int nCamaras = puntosControl.size();  // N
        int nDrones = numDrones;              // D
        int totalGenes = nCamaras + nDrones - 1;

        for (int i = 0; i < tamPoblacion; i++) {
            List<Integer> crom = new ArrayList<>();

            // 1️⃣ Añadir todas las cámaras
            for (int j = 0; j < nCamaras; j++) {
                crom.add(j);
            }

            // 2️⃣ Añadir separadores (valores N..N+D-2)
            for (int j = 0; j < nDrones - 1; j++) {
                crom.add(nCamaras + j);
            }

            // 3️⃣ Mezclar cromosoma aleatoriamente
            // Usamos Fisher-Yates para no crear separadores consecutivos al principio
            boolean valido = false;
            while (!valido) {
                Collections.shuffle(crom, rnd);
                valido = esCromosomaValido(crom, nCamaras);
            }

            // 4️⃣ Crear individuo
            IndividuoMTSP ind = new IndividuoMTSP(crom);
            ind.fitness = calcularFitness(ind);
            poblacion.add(ind);
        }

        return poblacion;
    }

    // Verifica que no haya drones sin cámaras
    private boolean esCromosomaValido(List<Integer> crom, int nCamaras) {
        int nDrones = numDrones;
        int dronActual = 0;
        int count = 0;
        for (int gen : crom) {
            if (gen >= nCamaras) { // separador
                if (count == 0) return false; // dron sin cámara
                dronActual++;
                count = 0;
            } else {
                count++;
            }
        }
        return count > 0; // último dron tiene al menos 1 cámara
    }

    private double calcularFitness(IndividuoMTSP ind) {
        List<List<Integer>> rutas = decodificar(ind);
        double tiempoMax = 0;

        // crear set de cámaras para penalización
        Set<String> posicionesCamaras = new HashSet<>();
        for (Camara c : puntosControl) posicionesCamaras.add(c.x + "," + c.y);

        for (int d = 0; d < rutas.size(); d++) {
            Dron dron = flota.get(d);
            List<Integer> ruta = rutas.get(d);
            double tiempoTotalDron = 0;
            int xActual = dron.getBaseX();
            int yActual = dron.getBaseY();

            for (int idCam : ruta) {
                Camara destino = puntosControl.get(idCam);
                double coste = aStar.calcularCoste(xActual, yActual, destino.x, destino.y, posicionesCamaras);
                if (coste == Double.POSITIVE_INFINITY) return Double.MAX_VALUE;
                tiempoTotalDron += coste / dron.getVelocidad();
                xActual = destino.x;
                yActual = destino.y;
            }

            // vuelta a base
            double costeVuelta = aStar.calcularCoste(xActual, yActual, dron.getBaseX(), dron.getBaseY(), posicionesCamaras);
            if (costeVuelta == Double.POSITIVE_INFINITY) return Double.MAX_VALUE;
            tiempoTotalDron += costeVuelta / dron.getVelocidad();

            tiempoMax = Math.max(tiempoMax, tiempoTotalDron);
        }

        return tiempoMax;
    }

    public List<List<Integer>> decodificar(IndividuoMTSP ind) {
        List<List<Integer>> rutas = new ArrayList<>();
        for (int i = 0; i < numDrones; i++) rutas.add(new ArrayList<>());

        int dronActual = 0;
        int n = puntosControl.size();

        for (int gen : ind.cromosoma) {
            if (gen >= n) {
                dronActual++;
            } else {
                rutas.get(dronActual).add(gen);
            }
        }
        return rutas;
    }

    private IndividuoMTSP seleccionar(List<IndividuoMTSP> poblacion) {
        if (metodoSeleccion == MetodoSeleccion.TORNEO) {
            IndividuoMTSP a = poblacion.get(rnd.nextInt(poblacion.size()));
            IndividuoMTSP b = poblacion.get(rnd.nextInt(poblacion.size()));
            return (a.fitness < b.fitness) ? a : b;
        }

        // RULETA inversa
        double total = poblacion.stream().mapToDouble(i -> 1.0 / i.fitness).sum();
        double r = rnd.nextDouble() * total;
        double acum = 0;
        for (IndividuoMTSP i : poblacion) {
            acum += 1.0 / i.fitness;
            if (acum >= r) return i;
        }
        return poblacion.get(0);
    }

    private IndividuoMTSP cruzar(IndividuoMTSP p1, IndividuoMTSP p2) {
        return cruceOX(p1, p2);
    }

    private IndividuoMTSP cruceOX(IndividuoMTSP a, IndividuoMTSP b) {
        int size = a.cromosoma.size();
        int c1 = rnd.nextInt(size);
        int c2 = rnd.nextInt(size);
        if (c1 > c2) { int tmp = c1; c1 = c2; c2 = tmp; }

        List<Integer> hijo = new ArrayList<>(Collections.nCopies(size, -1));
        for (int i = c1; i <= c2; i++) hijo.set(i, a.cromosoma.get(i));

        int idx = (c2 + 1) % size;
        for (int i = 0; i < size; i++) {
            int val = b.cromosoma.get((c2 + 1 + i) % size);
            if (!hijo.contains(val)) {
                hijo.set(idx, val);
                idx = (idx + 1) % size;
            }
        }
        return new IndividuoMTSP(hijo);
    }

    private void mutar(IndividuoMTSP ind) {
        int i = rnd.nextInt(ind.cromosoma.size());
        int j = rnd.nextInt(ind.cromosoma.size());
        Collections.swap(ind.cromosoma, i, j);
    }

    private IndividuoMTSP obtenerMejor(List<IndividuoMTSP> poblacion) {
        return poblacion.stream().min(Comparator.comparingDouble(i -> i.fitness)).get();
    }
}