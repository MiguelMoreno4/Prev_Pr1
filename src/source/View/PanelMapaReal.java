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
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // ===== 1. DIBUJAR MAPA (ORIGINAL DE TUS ARCHIVOS) =====
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

        // ===== 2. DIBUJAR CÁMARAS SEGÚN EL ALGORITMO =====
        if (camaras != null) {
            // Determinamos el algoritmo por la apertura (Modo Real vs Normal)
            if (apertura > 0.1 && apertura < 359) {
                // EJECUTA TU LÓGICA ORIGINAL DEL .ZIP
                dibujarCamarasReal(g2, cellSize);
            } else {
                // EJECUTA LA NUEVA LÓGICA DE ILUMINACIÓN PARA EL NORMAL
                dibujarCamarasNormal(g2, cellSize, filas, columnas);
            }
        }
    }

    /**
     * TU LÓGICA ORIGINAL DE LOS ARCHIVOS .ZIP
     */
    /**
     * LÓGICA PARA EL GENÉTICO REAL (AHORA CON DETECCIÓN DE MUROS)
     */
    private void dibujarCamarasReal(Graphics2D g2, int cellSize) {
        for (CamaraReal c : camaras) {
            // Posición exacta en píxeles (centro de la celda de la cámara)
            int cx = (int) (c.x * cellSize + (cellSize / 2.0));
            int cy = (int) (c.y * cellSize + (cellSize / 2.0));
            int radioVisionPixeles = rango * cellSize;

            // --- CREACIÓN DE LA MÁSCARA DE VISIÓN PRECISA ---
            Polygon mascara = new Polygon();
            mascara.addPoint(cx, cy);
            
            double angInicio = c.theta - (apertura / 2.0);
            
            // Afinamos a 0.5 grados para evitar huecos en la distancia
            for (double a = 0; a <= apertura; a += 0.5) {
                double rad = Math.toRadians(angInicio + a);
                double dFinal = rango;
                
                // Afinamos el paso de distancia a 0.1 para no "saltar" muros estrechos
                for (double d = 0; d <= rango; d += 0.1) {
                    // Calculamos la posición en coordenadas de matriz (celdas)
                    int nx = (int) (c.x + Math.cos(rad) * d + 0.5); // +0.5 para redondeo correcto
                    int ny = (int) (c.y + Math.sin(rad) * d + 0.5);

                    // Comprobación de límites y muros
                    if (nx < 0 || nx >= mapa[0].length || ny < 0 || ny >= mapa.length || mapa[ny][nx] == 1) {
                        dFinal = d;
                        break;
                    }
                }
                
                // Añadimos el punto al polígono de recorte
                int px = (int) (cx + Math.cos(rad) * dFinal * cellSize);
                int py = (int) (cy + Math.sin(rad) * dFinal * cellSize);
                mascara.addPoint(px, py);
            }

            // --- DIBUJO CON CLIP (TU VISTA ORIGINAL) ---
            Shape clipOriginal = g2.getClip();
            g2.setClip(mascara);

            g2.setColor(new Color(0, 120, 255, 60)); // Tu azul original
            // Importante: Usamos los mismos ángulos que en tu fillArc original
            g2.fillArc(cx - radioVisionPixeles, cy - radioVisionPixeles,
                       radioVisionPixeles * 2, radioVisionPixeles * 2,
                       (int)-(c.theta - apertura / 2), (int)-apertura);

            g2.setClip(clipOriginal);

            // --- CUERPO DE LA CÁMARA (TU CÓDIGO ORIGINAL) ---
            int r = cellSize / 3;
            g2.setColor(Color.BLUE);
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);
            g2.setColor(Color.WHITE);
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);
        }
    }
    /**
     * NUEVA FUNCIÓN PARA EL GENÉTICO NORMAL (ILUMINACIÓN CON MUROS)
     */
    private void dibujarCamarasNormal(Graphics2D g2, int cellSize, int filas, int columnas) {
        for (CamaraReal c : camaras) {
            int cx = (int) (c.x * cellSize + (cellSize / 2.0));
            int cy = (int) (c.y * cellSize + (cellSize / 2.0));
            int r = cellSize / 3;

            // 1. ILUMINACIÓN: Lanzamos rayos para marcar celdas (No atraviesa muros)
            g2.setColor(new Color(144, 238, 144, 100)); // verde claro
            for (int a = 0; a < 360; a += 5) {
                double rad = Math.toRadians(a);
                for (int d = 1; d <= rango; d++) {
                    int nx = (int) (c.x + Math.cos(rad) * d);
                    int ny = (int) (c.y + Math.sin(rad) * d);

                    if (nx < 0 || nx >= columnas || ny < 0 || ny >= filas) break;
                    if (mapa[ny][nx] == 1) break; // MURO: El rayo no pasa

                    g2.fillRect(nx * cellSize, ny * cellSize, cellSize, cellSize);
                }
            }

            // 2. CUERPO DE LA CÁMARA (Igual que en el Real para mantener estética)
            g2.setColor(Color.BLUE);
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);
            g2.setColor(Color.WHITE);
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);
        }
    }
}