package source;

import java.util.*;

public class AlgoritmoGeneticoReal {

    private Mapa mapa;
    private int rango; // alcance máximo de visión
    private int numCamaras;
    private double anguloApertura; // en grados
    private boolean modoPonderado = false;
    private int[][] importancia;
    private Random rnd = new Random();

    public AlgoritmoGeneticoReal(Mapa mapa, int rango, int numCamaras, double anguloApertura) {
        this.mapa = mapa;
        this.rango = rango;
        this.numCamaras = numCamaras;
        this.anguloApertura = anguloApertura;
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
    public IndividuoReal ejecutar(int generaciones, double probMutacion) {

        ArrayList<IndividuoReal> poblacion = new ArrayList<>();

        // Población inicial
        for (int i = 0; i < 30; i++) {
            IndividuoReal ind = crearAleatorioReal();
            ind.fitness = calcularFitness(ind);
            poblacion.add(ind);
        }

        IndividuoReal mejorGlobal = copiarIndividuo(poblacion.get(0));

        for (int g = 0; g < generaciones; g++) {

            // Ordenar por fitness descendente
            poblacion.sort((a, b) -> Double.compare(b.fitness, a.fitness));

            IndividuoReal elite = copiarIndividuo(poblacion.get(0));

            if (elite.fitness > mejorGlobal.fitness) {
                mejorGlobal = copiarIndividuo(elite);
            }

            ArrayList<IndividuoReal> nueva = new ArrayList<>();
            nueva.add(copiarIndividuo(elite)); // elitismo

            while (nueva.size() < poblacion.size()) {
                IndividuoReal padre = torneo(poblacion);
                IndividuoReal hijo = copiarIndividuo(padre);

                if (rnd.nextDouble() < probMutacion) {
                    mutar(hijo);
                }

                hijo.fitness = calcularFitness(hijo);
                nueva.add(hijo);
            }

            poblacion = nueva;
        }

        return copiarIndividuo(mejorGlobal);
    }

    // ===============================
    // OPERADORES GENÉTICOS
    // ===============================
    private IndividuoReal crearAleatorioReal() {
        IndividuoReal ind = new IndividuoReal();

        while (ind.camaras.size() < numCamaras) {
            double x, y, theta;
            int xCell, yCell;
            do {
                x = rnd.nextDouble() * mapa.columnas;
                y = rnd.nextDouble() * mapa.filas;

                xCell = (int)x;
                yCell = (int)y;

                // ✅ Comprobamos celda y un margen de seguridad
            } while (mapa.esObstaculo(xCell, yCell) || xCell >= mapa.columnas || yCell >= mapa.filas);

            theta = rnd.nextDouble() * 360;
            ind.camaras.add(new CamaraReal(x, y, theta));
        }

        return ind;
    }

    private void mutar(IndividuoReal ind) {
        int idx = rnd.nextInt(ind.camaras.size());
        CamaraReal c = ind.camaras.get(idx);

        double x, y, theta;
        int xCell, yCell;

        do {
            x = c.x + rnd.nextGaussian() * 0.5;
            y = c.y + rnd.nextGaussian() * 0.5;
            theta = (c.theta + rnd.nextGaussian() * 10) % 360;
            if (theta < 0) theta += 360;

            // limitar dentro del mapa
            x = Math.max(0, Math.min(x, mapa.columnas - 0.01));
            y = Math.max(0, Math.min(y, mapa.filas - 0.01));

            xCell = (int)x;
            yCell = (int)y;

        } while (mapa.esObstaculo(xCell, yCell));

        c.x = x;
        c.y = y;
        c.theta = theta;
    }
    private IndividuoReal torneo(List<IndividuoReal> poblacion) {
        IndividuoReal a = poblacion.get(rnd.nextInt(poblacion.size()));
        IndividuoReal b = poblacion.get(rnd.nextInt(poblacion.size()));
        return (a.fitness >= b.fitness) ? a : b;
    }

    private IndividuoReal copiarIndividuo(IndividuoReal ind) {
        IndividuoReal copia = new IndividuoReal();
        for (CamaraReal c : ind.camaras) {
            copia.camaras.add(new CamaraReal(c.x, c.y, c.theta));
        }
        copia.fitness = ind.fitness;
        return copia;
    }

    // ===============================
    // FITNESS CON VISIÓN CÓNICA
    // ===============================
    public double calcularFitness(IndividuoReal ind) {

        HashSet<String> vigiladas = new HashSet<>();
        double fitness = 0.0;

        for (CamaraReal c : ind.camaras) {

            int minX = (int)Math.max(0, Math.floor(c.x - rango));
            int maxX = (int)Math.min(mapa.columnas - 1, Math.ceil(c.x + rango));
            int minY = (int)Math.max(0, Math.floor(c.y - rango));
            int maxY = (int)Math.min(mapa.filas - 1, Math.ceil(c.y + rango));

            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {

                    if (mapa.esObstaculo(x, y)) continue;

                    // DISTANCIA
                    double dx = x + 0.5 - c.x;
                    double dy = y + 0.5 - c.y;
                    double dist = Math.sqrt(dx*dx + dy*dy);
                    if (dist > rango) continue;

                    // ÁNGULO
                    double ang = Math.toDegrees(Math.atan2(dy, dx));
                    double diff = Math.abs(ang - c.theta);
                    diff = Math.min(diff, 360 - diff);
                    if (diff > anguloApertura/2) continue;

                    // LÍNEA DE VISIÓN (raycast simple)
                    if (!lineaVision(c.x, c.y, x + 0.5, y + 0.5)) continue;

                    String key = x + "," + y;
                    if (!vigiladas.contains(key)) {
                        vigiladas.add(key);
                        fitness += modoPonderado ? importancia[y][x] : 1;
                    }
                }
            }
        }

        return fitness;
    }

    // ===============================
    // RAYCAST SIMPLE (Bresenham)
    // ===============================
    private boolean lineaVision(double x0, double y0, double x1, double y1) {
        int ix0 = (int)Math.floor(x0);
        int iy0 = (int)Math.floor(y0);
        int ix1 = (int)Math.floor(x1);
        int iy1 = (int)Math.floor(y1);

        int dx = Math.abs(ix1 - ix0);
        int dy = Math.abs(iy1 - iy0);

        int sx = ix0 < ix1 ? 1 : -1;
        int sy = iy0 < iy1 ? 1 : -1;

        int err = dx - dy;

        int x = ix0;
        int y = iy0;

        while (true) {
            if (mapa.esObstaculo(x, y)) return false;
            if (x == ix1 && y == iy1) break;

            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x += sx; }
            if (e2 < dx)  { err += dx; y += sy; }
        }

        return true;
    }
}