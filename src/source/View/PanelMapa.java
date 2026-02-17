package source.View;

import javax.swing.*;

import source.Camaras.Camara;

import java.awt.*;
import java.util.List;

public class PanelMapa extends JPanel {

    private int[][] mapa;
    private int[][] importancia;
    private List<Camara> camaras;

    public PanelMapa() {
        setBackground(Color.WHITE);
    }

    public void setMapa(int[][] mapa, int[][] importancia) {
        this.mapa = mapa;
        this.importancia = importancia;
        repaint();
    }

    public void setCamaras(List<Camara> camaras) {
        this.camaras = camaras;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (mapa == null) return;

        int filas = mapa.length;
        int columnas = mapa[0].length;

        int cellSize = Math.min(
                getWidth() / columnas,
                getHeight() / filas
        );

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

        // ===== CÁMARAS =====
        if (camaras != null) {
            g.setColor(Color.BLUE);
            for (Camara c : camaras) {
                int cx = c.x * cellSize + cellSize/2;
                int cy = c.y * cellSize + cellSize/2;
                int r = cellSize / 3;
                g.fillOval(cx - r, cy - r, r*2, r*2);
            }
        }
    }
}