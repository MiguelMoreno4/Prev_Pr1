package source.View;

import java.util.*;
import java.awt.Point;
import source.Camaras.Camara;

public class AStar {

    private Mapa mapa;

    public AStar(Mapa mapa) {
        this.mapa = mapa;
    }

    private static class Nodo implements Comparable<Nodo> {
        int x, y;
        double g, f;
        Nodo padre;

        Nodo(int x, int y, double g, double f, Nodo padre) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.f = f;
            this.padre = padre;
        }

        @Override
        public int compareTo(Nodo o) {
            return Double.compare(this.f, o.f);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Nodo)) return false;
            Nodo n = (Nodo) obj;
            return this.x == n.x && this.y == n.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    /**
     * Calcula el coste de ir de (x0,y0) a (x1,y1) evitando obstáculos y penalizando cámaras intermedias.
     * @param x0, y0 origen
     * @param x1, y1 destino
     * @param posicionesCamaras conjunto de coordenadas "x,y" que representan cámaras
     * @return coste total mínimo (positivo) o Double.POSITIVE_INFINITY si no hay ruta
     */
    public double calcularCoste(int x0, int y0, int x1, int y1, Set<String> posicionesCamaras) {

        if (mapa.esObstaculo(x0, y0) || mapa.esObstaculo(x1, y1))
            return Double.POSITIVE_INFINITY;

        PriorityQueue<Nodo> open = new PriorityQueue<>();
        Set<Nodo> closed = new HashSet<>();

        Nodo start = new Nodo(x0, y0, 0, heuristica(x0, y0, x1, y1), null);
        open.add(start);

        int[][] dirs = { {0,1}, {0,-1}, {1,0}, {-1,0} }; // ortogonales

        while (!open.isEmpty()) {
            Nodo actual = open.poll();

            if (actual.x == x1 && actual.y == y1)
                return actual.g; // coste acumulado

            closed.add(actual);

            for (int[] d : dirs) {
                int nx = actual.x + d[0];
                int ny = actual.y + d[1];

                if (!mapa.enRango(nx, ny) || mapa.esObstaculo(nx, ny))
                    continue;

                double costeCelda = mapa.getCoste(nx, ny);
                if (costeCelda < 1) costeCelda = 1; // asegurar mínimo 1

                // Penalización si atraviesa otra cámara que no sea destino
                String key = nx + "," + ny;
                if (posicionesCamaras.contains(key) && !(nx == x1 && ny == y1)) {
                    costeCelda += 500; // penalización fuerte
                }

                Nodo vecino = new Nodo(nx, ny, actual.g + costeCelda, 0, actual);
                vecino.f = vecino.g + heuristica(nx, ny, x1, y1);

                if (closed.contains(vecino))
                    continue;

                // Si ya está en open con menor g, no agregar
                boolean skip = false;
                for (Nodo n : open) {
                    if (n.equals(vecino) && n.g <= vecino.g) {
                        skip = true;
                        break;
                    }
                }
                if (!skip)
                    open.add(vecino);
            }
        }

        return Double.POSITIVE_INFINITY; // no hay ruta
    }
    public List<Point> calcularRuta(int x0, int y0, int x1, int y1, Set<String> posicionesCamaras) {

        if (mapa.esObstaculo(x0, y0) || mapa.esObstaculo(x1, y1))
            return null;

        PriorityQueue<Nodo> open = new PriorityQueue<>();
        Set<Nodo> closed = new HashSet<>();

        Nodo start = new Nodo(x0, y0, 0, heuristica(x0, y0, x1, y1), null);
        open.add(start);

        int[][] dirs = { {0,1}, {0,-1}, {1,0}, {-1,0} };

        while (!open.isEmpty()) {

            Nodo actual = open.poll();

            if (actual.x == x1 && actual.y == y1)
                return reconstruirCamino(actual);

            closed.add(actual);

            for (int[] d : dirs) {

                int nx = actual.x + d[0];
                int ny = actual.y + d[1];

                if (!mapa.enRango(nx, ny) || mapa.esObstaculo(nx, ny))
                    continue;

                double costeCelda = mapa.getCoste(nx, ny);
                if (costeCelda < 1) costeCelda = 1;

                String key = nx + "," + ny;
                if (posicionesCamaras.contains(key) && !(nx == x1 && ny == y1)) {
                    costeCelda += 500;
                }

                Nodo vecino = new Nodo(nx, ny, actual.g + costeCelda, 0, actual);
                vecino.f = vecino.g + heuristica(nx, ny, x1, y1);

                if (closed.contains(vecino))
                    continue;

                boolean skip = false;
                for (Nodo n : open) {
                    if (n.equals(vecino) && n.g <= vecino.g) {
                        skip = true;
                        break;
                    }
                }

                if (!skip)
                    open.add(vecino);
            }
        }

        return null;
    }
    private List<Point> reconstruirCamino(Nodo nodoFinal) {

        List<Point> camino = new ArrayList<>();

        Nodo actual = nodoFinal;

        while (actual != null) {
            camino.add(0, new Point(actual.x, actual.y));
            actual = actual.padre;
        }

        return camino;
    }
    private double heuristica(int x0, int y0, int x1, int y1) {
        // Manhattan (positivo)
        return Math.abs(x0 - x1) + Math.abs(y0 - y1);
    }
}