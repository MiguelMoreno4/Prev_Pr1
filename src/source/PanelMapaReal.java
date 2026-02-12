package source;

import javax.swing.*;
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
        int cellSize = Math.min(getWidth() / columnas, getHeight() / filas);

        // ===== MAPA =====
        for (int y = 0; y < filas; y++) {
            for (int x = 0; x < columnas; x++) {
                if (mapa[y][x] == 1) {
                    g.setColor(Color.BLACK);
                } else {
                    int imp = importancia[y][x];
                    switch (imp) {
                        case 20: g.setColor(new Color(150,0,0)); break;
                        case 10: g.setColor(Color.RED); break;
                        case 5:  g.setColor(Color.YELLOW); break;
                        default: g.setColor(Color.WHITE);
                    }
                }
                g.fillRect(x*cellSize, y*cellSize, cellSize, cellSize);
                g.setColor(Color.GRAY);
                g.drawRect(x*cellSize, y*cellSize, cellSize, cellSize);
            }
        }

        // ===== CÁMARAS REAL =====
        if (camaras != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.BLUE);
            for (CamaraReal c : camaras) {
                int cx = (int)(c.x * cellSize);
                int cy = (int)(c.y * cellSize);
                int r = cellSize / 3;
                g2.fillOval(cx - r, cy - r, r*2, r*2);

                // Dibuja cono de visión
                g2.setColor(new Color(0,0,255,50));
                int angInicio = (int) (c.theta - apertura/2);
                g2.fillArc(cx - rango*cellSize, cy - rango*cellSize,
                           rango*2*cellSize, rango*2*cellSize,
                           angInicio, (int)apertura);
                g2.setColor(Color.BLUE);
            }
        }
    }
}