package source.Main;

import javax.swing.SwingUtilities;
import source.View.VentanaPrincipal;

public class Main {
    public static void main(String[] args) {
        // La forma correcta de lanzar aplicaciones Swing en Java
        SwingUtilities.invokeLater(() -> {
            try {
                VentanaPrincipal ventana = new VentanaPrincipal();
                ventana.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}