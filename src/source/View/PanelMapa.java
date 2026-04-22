package source.View;

import javax.swing.*;
import java.awt.*;

public class PanelMapa extends JPanel {

    private Mapa mapaActual;
    private Rover roverActual;

    public PanelMapa() {
        setBackground(Color.WHITE);
    }

    
    public void actualizar(Mapa mapa, Rover rover) {
        this.mapaActual = mapa;
        this.roverActual = rover;
        repaint(); // Forzamos a que se vuelva a pintar
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Si aún no hemos simulado nada, no pintamos nada
        if (mapaActual == null || roverActual == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int filas = Mapa.FILAS;
        int columnas = Mapa.COLUMNAS;

        // Ajuste de tamaño para que quede cuadrado y centrado
        int padding = 10;
        int availableWidth = getWidth() - (padding * 2);
        int availableHeight = getHeight() - (padding * 2);

        int cellSize = Math.min(availableWidth / columnas, availableHeight / filas);
        int xOffset = (getWidth() - (cellSize * columnas)) / 2;
        int yOffset = (getHeight() - (cellSize * filas)) / 2;

        // 1. DIBUJAR EL MAPA (Casillas)
        for (int y = 0; y < filas; y++) {
            for (int x = 0; x < columnas; x++) {
                int px = xOffset + (x * cellSize);
                int py = yOffset + (y * cellSize);

                switch (mapaActual.getCasilla(x, y)) {
                case MURO:
                    g2.setColor(new Color(200, 50, 50)); // Rojo: pared/muro
                    break;
                case MUESTRA:
                    g2.setColor(new Color(255, 255, 0)); // Verde: recompensa/muestra
                    break;
                case ARENA:
                    g2.setColor(new Color(255, 140, 0)); // Naranja: arena
                    break;
                case SUELO:
                default:
                	if (roverActual.haVisitado(x, y)) {
                        g2.setColor(new Color(150, 200, 255)); // Azul clarito: rastro (suelo pisado)
                    } else {
                        g2.setColor(new Color(100, 100, 100)); // Gris oscuro: libre/suelo virgen
                    }
                 
                    break;
            }

                g2.fillRect(px, py, cellSize, cellSize);
                
                // Borde de la casilla
                g2.setColor(new Color(200, 170, 120)); 
                g2.drawRect(px, py, cellSize, cellSize);
            }
        }

        // 2. DIBUJAR AL ROVER
        int rX = xOffset + (roverActual.getX() * cellSize);
        int rY = yOffset + (roverActual.getY() * cellSize);

        // Cuerpo del Rover (Círculo Azul)
        g2.setColor(Color.BLUE);
        g2.fillOval(rX + 2, rY + 2, cellSize - 4, cellSize - 4);
        
        // Borde del Rover
        g2.setColor(Color.BLACK);
        g2.drawOval(rX + 2, rY + 2, cellSize - 4, cellSize - 4);

        // 3. DIBUJAR LA DIRECCIÓN DEL ROVER (Morro amarillo)
        g2.setColor(Color.YELLOW);
        int midX = rX + (cellSize / 2);
        int midY = rY + (cellSize / 2);
        int tamanoMorro = cellSize / 4;

        switch (roverActual.getDireccion()) {
            case NORTE:
                g2.fillOval(midX - (tamanoMorro / 2), rY + 4, tamanoMorro, tamanoMorro);
                break;
            case SUR:
                g2.fillOval(midX - (tamanoMorro / 2), rY + cellSize - tamanoMorro - 4, tamanoMorro, tamanoMorro);
                break;
            case ESTE:
                g2.fillOval(rX + cellSize - tamanoMorro - 4, midY - (tamanoMorro / 2), tamanoMorro, tamanoMorro);
                break;
            case OESTE:
                g2.fillOval(rX + 4, midY - (tamanoMorro / 2), tamanoMorro, tamanoMorro);
                break;
        }
    }
}