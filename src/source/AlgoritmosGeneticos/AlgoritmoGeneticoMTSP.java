package source.AlgoritmosGeneticos;

import java.util.*;
import java.awt.Color;

import source.Camaras.Camara;
import source.Camaras.Dron;
import source.Camaras.FabricaDrones;
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
    private VentanaPrincipal ventana;
    private Random rnd;

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
        this.flota = FabricaDrones.crearFlota(numDrones);
    }

    public List<Dron> getFlota() {
        return this.flota;
    }

    /**
     * Convierte una lista de cámaras en un IndividuoMTSP
     */
    public IndividuoMTSP convertirACromosomaMTSP(List<Camara> camaras) {
        List<Integer> cromosoma = new ArrayList<>();
        int nCamaras = camaras.size();
        int separadores = numDrones - 1;
        for (int i = 0; i < nCamaras; i++) {
            cromosoma.add(i);
        }
        // Añadir separadores al final del cromosoma
        for (int i = 0; i < separadores; i++) {
            cromosoma.add(nCamaras + i);
        }
        return new IndividuoMTSP(cromosoma);
    }

    /**
     * Decodifica un IndividuoMTSP en rutas reales de drones (lista de listas de índices)
     */
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

    /**
     * Calcula el "fitness" de un individuo MTSP (tiempo máximo entre drones)
     */
    public double calcularFitness(IndividuoMTSP ind) {
        List<List<Integer>> rutas = decodificar(ind);
        double tiempoMax = 0;
        Set<String> posicionesCamaras = new HashSet<>();
        for (Camara c : puntosControl)
            posicionesCamaras.add(c.x + "," + c.y);

        for (int d = 0; d < rutas.size(); d++) {
            Dron dron = flota.get(d);
            List<Integer> ruta = rutas.get(d);
            double tiempoTotal = 0;
            int xActual = dron.getBaseX();
            int yActual = dron.getBaseY();

            for (int idCam : ruta) {
                Camara destino = puntosControl.get(idCam);
                double coste = aStar.calcularCoste(xActual, yActual, destino.x, destino.y, posicionesCamaras);
                if (coste == Double.POSITIVE_INFINITY) return Double.MAX_VALUE;
                tiempoTotal += coste / dron.getVelocidad();
                xActual = destino.x;
                yActual = destino.y;
            }

            // vuelta a base
            double costeVuelta = aStar.calcularCoste(xActual, yActual, dron.getBaseX(), dron.getBaseY(), posicionesCamaras);
            if (costeVuelta == Double.POSITIVE_INFINITY) return Double.MAX_VALUE;
            tiempoTotal += costeVuelta / dron.getVelocidad();

            tiempoMax = Math.max(tiempoMax, tiempoTotal);
        }
        return tiempoMax;
    }

    /**
     * Método simple de prueba para ejecutar MTSP con rutas de AG
     */
    public IndividuoMTSP ejecutarSimulacion() {
        // Generamos un IndividuoMTSP a partir de las cámaras
        IndividuoMTSP ind = convertirACromosomaMTSP(puntosControl);
        ind.fitness = calcularFitness(ind);
        return ind;
    }
}