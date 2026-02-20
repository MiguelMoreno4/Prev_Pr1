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
                dibujarCamarasReal(g2, cellSize);
            } else {
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
        // 1. ILUMINACIÓN EN CRUZ
        g2.setColor(new Color(144, 238, 144, 150)); // Verde semitransparente

        for (CamaraReal c : camaras) {
            // Obtenemos la celda origen (donde está parada la cámara)
            int xCentro = (int) Math.floor(c.x);
            int yCentro = (int) Math.floor(c.y);

            // Direcciones: Derecha, Izquierda, Abajo, Arriba
            int[][] direcciones = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

            for (int[] dir : direcciones) {
                // Empezamos desde la celda de la cámara y nos movemos en la dirección
                for (int d = 1; d <= Math.max(filas, columnas); d++) {
                    int nx = xCentro + (dir[0] * d);
                    int ny = yCentro + (dir[1] * d);

                    // Comprobar límites del tablero
                    if (nx < 0 || nx >= columnas || ny < 0 || ny >= filas) break;
                    
                    // Comprobar si hay muro
                    if (mapa[ny][nx] == 1) break; 

                    // Pintar la celda visible
                    g2.fillRect(nx * cellSize, ny * cellSize, cellSize, cellSize);
                    
                    // Dibujar un borde sutil para que se sigan viendo las celdas
                    g2.setColor(new Color(0, 100, 0, 40));
                    g2.drawRect(nx * cellSize, ny * cellSize, cellSize, cellSize);
                    g2.setColor(new Color(144, 238, 144, 150)); // Restaurar color verde
                }
            }
        }

        // 2. CUERPO DE LAS CÁMARAS (Para que queden por encima de la luz)
        for (CamaraReal c : camaras) {
            int cx = (int) (c.x * cellSize + (cellSize / 2.0));
            int cy = (int) (c.y * cellSize + (cellSize / 2.0));
            int r = cellSize / 3;

            g2.setColor(Color.BLUE);
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);
        }
    }
}