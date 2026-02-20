package source.View;

import javax.swing.*;
import source.Camaras.CamaraReal;
import java.awt.*;
import java.util.List;

public class PanelMapaReal extends JPanel {

    private int[][] mapa;
    private int[][] importancia;
    private List<CamaraReal> camaras;
    private int rango;
    private double apertura;

    public PanelMapaReal() {
        setBackground(Color.WHITE);
        // Sugerencia: añadir un borde para ver los límites del panel si es necesario
        // setBorder(BorderFactory.createLineBorder(Color.GRAY));
    }

    public void setMapa(int[][] mapa, int[][] importancia) {
        this.mapa = mapa;
        this.importancia = importancia;
        repaint();
    }

    public void setCamaras(List<CamaraReal> camaras, int rango, double apertura) {
        this.camaras = camaras;
        this.rango = rango;
        this.apertura = apertura;
        repaint();
    }

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

        // 2. DIBUJAR CÁMARAS Y PUNTOS CIAN
        if (camaras != null) {
            // Pasamos xOffset y yOffset a las funciones de dibujo
            if (apertura > 0.1 && apertura < 359) {
                marcarCeldasContabilizadas(g2, cellSize, xOffset, yOffset);
                dibujarCamarasReal(g2, cellSize, xOffset, yOffset);
            } else {
                dibujarCamarasNormal(g2, cellSize, filas, columnas, xOffset, yOffset);
            }
        }
    }

    private void marcarCeldasContabilizadas(Graphics2D g2, int cellSize, int xOff, int yOff) {
        if (mapa == null || camaras == null) return;
        boolean[][] visto = new boolean[mapa.length][mapa[0].length];
        
        for (CamaraReal c : camaras) {
            for (double a = c.theta - apertura / 2; a <= c.theta + apertura / 2; a += 1.0) {
                for (int d = 1; d <= rango; d++) {
                    int vx = (int) Math.floor(c.x + Math.cos(Math.toRadians(a)) * d);
                    int vy = (int) Math.floor(c.y + Math.sin(Math.toRadians(a)) * d);

                    if (vx < 0 || vx >= mapa[0].length || vy < 0 || vy >= mapa.length) break;
                    if (mapa[vy][vx] == 1) break;

                    if (!visto[vy][vx]) {
                        visto[vy][vx] = true;
                        g2.setColor(new Color(0, 255, 255, 180));
                        int pad = cellSize / 3;
                        g2.fillOval(xOff + (vx * cellSize) + pad, yOff + (vy * cellSize) + pad, cellSize / 3, cellSize / 3);
                    }
                }
            }
        }
    }

    private void dibujarCamarasReal(Graphics2D g2, int cellSize, int xOff, int yOff) {
        for (CamaraReal c : camaras) {
            int cx = (int) (xOff + (c.x * cellSize) + (cellSize / 2.0));
            int cy = (int) (yOff + (c.y * cellSize) + (cellSize / 2.0));
            int radioVisionPixeles = rango * cellSize;

            g2.setColor(new Color(0, 120, 255, 60)); 
            g2.fillArc(cx - radioVisionPixeles, cy - radioVisionPixeles,
                       radioVisionPixeles * 2, radioVisionPixeles * 2,
                       (int)-(c.theta + apertura / 2), (int)apertura);

            int r = cellSize / 3;
            g2.setColor(Color.BLUE);
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);
            g2.setColor(Color.WHITE);
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);
        }
    }

    private void dibujarCamarasNormal(Graphics2D g2, int cellSize, int filas, int columnas, int xOff, int yOff) {
        g2.setColor(new Color(144, 238, 144, 150));
        for (CamaraReal c : camaras) {
            int xCentro = (int) Math.floor(c.x);
            int yCentro = (int) Math.floor(c.y);
            int[][] direcciones = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] dir : direcciones) {
                for (int d = 1; d <= Math.max(filas, columnas); d++) {
                    int nx = xCentro + (dir[0] * d);
                    int ny = yCentro + (dir[1] * d);
                    if (nx < 0 || nx >= columnas || ny < 0 || ny >= filas || mapa[ny][nx] == 1) break; 
                    g2.fillRect(xOff + (nx * cellSize), yOff + (ny * cellSize), cellSize, cellSize);
                }
            }
        }
        for (CamaraReal c : camaras) {
            int cx = (int) (xOff + (c.x * cellSize) + (cellSize / 2.0));
            int cy = (int) (yOff + (c.y * cellSize) + (cellSize / 2.0));
            int r = cellSize / 3;
            g2.setColor(Color.BLUE);
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);
        }
    }
}