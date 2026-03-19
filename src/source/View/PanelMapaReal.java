package source.View;

import javax.swing.*;
import source.Camaras.Camara;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PanelMapaReal extends JPanel {

    private int[][] mapa;
    private int[][] importancia;
    private List<Camara> camaras;
    private List<List<Integer>> rutas; // rutas decodificadas para numerar cámaras
    private List<List<Point>> rutasDrones = new ArrayList<>();

    public PanelMapaReal() {
        setBackground(Color.WHITE);
    }

    public void limpiar() {
        this.camaras = null;
        this.rutas = null;
        this.rutasDrones = new ArrayList<>();
        repaint();
    }

    public void setMapa(int[][] mapa, int[][] importancia) {
        this.mapa = mapa;
        this.importancia = importancia;
        repaint();
    }

    // setCamaras con rutas para numeración por orden de visita
    public void setCamaras(List<Camara> camaras, List<List<Integer>> rutas) {
        this.camaras = camaras;
        this.rutas = rutas;
        repaint();
    }

    // setCamaras sin rutas (compatibilidad)
    public void setCamaras(List<Camara> camaras) {
        this.camaras = camaras;
        this.rutas = null;
        repaint();
    }

    public void setRutasDrones(List<List<Point>> rutas) {
        this.rutasDrones = rutas;
    }

    private List<Color> coloresDrones = Arrays.asList(
    	    Color.BLUE, Color.MAGENTA, Color.GREEN, Color.ORANGE, Color.CYAN
    	);

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (mapa == null) return;

        int filas = mapa.length;
        int columnas = mapa[0].length;

        // --- AJUSTE DE TAMAÑO PARA REDUCIR HUECOS ---
        // Añadimos un pequeño margen (padding) de 10 píxeles para que no pegue a los bordes
        int padding = 10;
        int availableWidth = getWidth() - (padding * 2);
        int availableHeight = getHeight() - (padding * 2);

        int cellSize = Math.min(availableWidth / columnas, availableHeight / filas);

        // Calculamos el desplazamiento para centrar el mapa en el panel
        int xOffset = (getWidth() - (cellSize * columnas)) / 2;
        int yOffset = (getHeight() - (cellSize * filas)) / 2;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. DIBUJAR MAPA
        for (int y = 0; y < filas; y++) {
            for (int x = 0; x < columnas; x++) {
                int px = xOffset + (x * cellSize);
                int py = yOffset + (y * cellSize);

                if (mapa[y][x] == 1) {
                    g2.setColor(Color.BLACK);
                } else {
                    int imp = importancia[y][x];
                    switch (imp) {
                        case 20: g2.setColor(new Color(150, 0, 0)); break;
                        case 15: g2.setColor(Color.PINK); break;
                        case 10: g2.setColor(Color.RED); break;
                        case 5:  g2.setColor(Color.YELLOW); break;
                        default: g2.setColor(Color.WHITE);
                    }
                }
                g2.fillRect(px, py, cellSize, cellSize);
                g2.setColor(new Color(200, 200, 200, 100));
                g2.drawRect(px, py, cellSize, cellSize);
            }
        }

        // 2. DIBUJAR CÁMARAS numeradas por orden de visita
        if (camaras != null) {
            dibujarCamaras(g2, cellSize, xOffset, yOffset);
        }

        // ===== DIBUJAR DRONES Y SU RUTA =====
        if (rutasDrones != null) {
            g2.setStroke(new BasicStroke(2)); // ancho de línea para rutas
            for (int d = 0; d < rutasDrones.size(); d++) {
                List<Point> ruta = rutasDrones.get(d);
                if (ruta.isEmpty()) continue;

                Color colorDron = coloresDrones.get(d % coloresDrones.size());
                g2.setColor(colorDron);

                // Dibujar la ruta completa del dron
                for (int i = 0; i < ruta.size() - 1; i++) {
                    Point p1 = ruta.get(i);
                    Point p2 = ruta.get(i + 1);
                    int x1 = xOffset + p1.x * cellSize + cellSize / 2;
                    int y1 = yOffset + p1.y * cellSize + cellSize / 2;
                    int x2 = xOffset + p2.x * cellSize + cellSize / 2;
                    int y2 = yOffset + p2.y * cellSize + cellSize / 2;
                    g2.drawLine(x1, y1, x2, y2);
                }

                // Dibujar dron en la última posición de la ruta
                Point ultima = ruta.get(ruta.size() - 1);
                int x = xOffset + ultima.x * cellSize + cellSize / 2;
                int y = yOffset + ultima.y * cellSize + cellSize / 2;
                int r = cellSize / 2; // radio del dron

                g2.setColor(colorDron);
                g2.fillOval(x - r / 2, y - r / 2, r, r);
                g2.setColor(Color.WHITE);
                g2.drawOval(x - r / 2, y - r / 2, r, r);
            }
        }
    }

    private void dibujarCamaras(Graphics2D g2, int cellSize, int xOff, int yOff) {
    	Color[] coloresDronesArr = {Color.BLUE, Color.MAGENTA, Color.GREEN, Color.ORANGE, Color.CYAN};
    	
        // Calcular orden de visita y color por cámara según las rutas
        int[] ordenVisita = new int[camaras.size()];
        Color[] colorPorCamara = new Color[camaras.size()];

        // Por defecto todas azules
        for (int i = 0; i < camaras.size(); i++)
            colorPorCamara[i] = Color.BLUE;

        if (rutas != null) {
            for (int d = 0; d < rutas.size(); d++) {
                List<Integer> ruta = rutas.get(d);
                for (int v = 0; v < ruta.size(); v++) {
                    int idCam = ruta.get(v);
                    ordenVisita[idCam] = v + 1; // orden empieza en 1
                    colorPorCamara[idCam] = coloresDronesArr[d % coloresDronesArr.length];
                }
            }
        }

        // Configuramos la letra para que se escale según el tamaño de la celda
        g2.setFont(new Font("Arial", Font.BOLD, Math.max(8, cellSize / 2)));

        for (int i = 0; i < camaras.size(); i++) {
            Camara c = camaras.get(i);
            int cx = xOff + (c.x * cellSize) + (cellSize / 2);
            int cy = yOff + (c.y * cellSize) + (cellSize / 2);
            int r = cellSize / 2;

            // Dibujar bola con color del dron correspondiente
            g2.setColor(colorPorCamara[i]);
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);
            g2.setColor(Color.WHITE);
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);

            // Dibujar número de orden de visita
            g2.setColor(Color.WHITE);
            String num = String.valueOf(ordenVisita[i]);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(num, cx - fm.stringWidth(num) / 2, cy + fm.getAscent() / 2);
        }
    }
}