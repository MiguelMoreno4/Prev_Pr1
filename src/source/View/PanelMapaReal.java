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

        Graphics2D g2 = (Graphics2D) g;
        // Suavizado de bordes para que los conos se vean mejor
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // ===== 1. DIBUJAR MAPA =====
        for (int y = 0; y < filas; y++) {
            for (int x = 0; x < columnas; x++) {
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
                g2.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                g2.setColor(new Color(200, 200, 200, 100)); // Rejilla suave
                g2.drawRect(x * cellSize, y * cellSize, cellSize, cellSize);
            }
        }

        // ===== 2. DIBUJAR CÁMARAS =====
        if (camaras != null) {
            for (CamaraReal c : camaras) {
                // CENTRADO: Sumamos cellSize/2 para que (x,y) sea el centro real de la cámara
                int cx = (int) (c.x * cellSize + (cellSize / 2.0));
                int cy = (int) (c.y * cellSize + (cellSize / 2.0));
                int r = cellSize / 3;

                // --- A. CONO DE VISIÓN (Solo si apertura > 0, para modo Real) ---
                if (apertura > 0.1 && apertura < 359) {
                    g2.setColor(new Color(0, 120, 255, 60)); // Azul translúcido
                    int radioVisionPixeles = rango * cellSize;
                    
                    // Nota: En Java Swing los ángulos van en sentido antihorario
                    // y el eje Y está invertido. Usamos -c.theta y -apertura para corregirlo.
                    int angInicio = (int) (c.theta - apertura / 2);
                    
                    g2.fillArc(cx - radioVisionPixeles, cy - radioVisionPixeles,
                               radioVisionPixeles * 2, radioVisionPixeles * 2,
                               -angInicio, (int)-apertura);
                }

                // --- B. CUERPO DE LA CÁMARA (Punto azul) ---
                g2.setColor(Color.BLUE);
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                
                // Borde blanco para resaltar
                g2.setColor(Color.WHITE);
                g2.drawOval(cx - r, cy - r, r * 2, r * 2);
            }
        }
    }
}