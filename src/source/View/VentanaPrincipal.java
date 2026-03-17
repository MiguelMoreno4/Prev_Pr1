package source.View;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import source.AlgoritmosGeneticos.AlgoritmoGeneticoMTSP;
import source.Camaras.Camara;
import source.Camaras.CamaraReal;
import source.Camaras.Dron;
import source.Escenarios.EscenarioDatos;
import source.Escenarios.EscenariosFactory;
import source.Individuos.Individuo;
import source.Individuos.IndividuoMTSP;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    private JPanel contentPane;
    private PanelMapaReal panelMapa;
    private JButton btnEjecutar;
  
    private JComboBox<String> comboEscenario;

    private JRadioButton rdbtnMTSP;

    private JTextArea textAreaResultados;
    private JTextArea textAreaRutas;
    private PanelGrafica panelGrafica;

    private JSpinner spinPob, spinGens, spinCruce, spinMut, spinElite;

    private JComboBox<String> comboSeleccion;
    private JComboBox<String> comboCruceOp;
    private JComboBox<String> comboMutacionOp;
    
    private JTextPane textPaneCromosoma;
    
    // NUEVOS CONTROLES
    private JSpinner spinSeed;
    private JSpinner spinDrones;
    
    // PANEL DE RESULTADO FINAL MTSP
    private JPanel panelResultadoFinal;
    private JLabel lblMakespan;
    private JLabel lblSemilla;
    private JLabel lblTiemposDrones;
    private JTextPane textPaneCromosomaFinal;
    private JTextPane textPaneReporteFinal;

    // ==========================================
    // NUEVAS VARIABLES PARA TIEMPO REAL
    // ==========================================
    private JLabel lblTiempoCronometro;
    private JLabel[] lblTiemposDronesRealTime; // Array para soportar hasta 5 drones

    public VentanaPrincipal() {

        setTitle("Optimización de Cámaras - Panel de Control AG");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(50, 50, 1150, 950);

        contentPane = new JPanel();
        contentPane.setLayout(null);
        setContentPane(contentPane);

        // ===============================
        // PANEL CONFIGURACION SUPERIOR
        // ===============================

        JPanel pnlConfig = new JPanel();
        pnlConfig.setBounds(20, 10, 1090, 50);
        pnlConfig.setBorder(BorderFactory.createEtchedBorder());
        pnlConfig.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        contentPane.add(pnlConfig);

        comboEscenario = new JComboBox<>(new String[]{
                "Escenario 1 - Museo",
                "Escenario 2 - Pasillos",
                "Escenario 3 - Supermercado"
        });

        spinSeed = new JSpinner(new SpinnerNumberModel(1, 1, 9999999, 1));
        spinSeed.setPreferredSize(new Dimension(80, 22));

        spinDrones = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        spinDrones.setPreferredSize(new Dimension(50, 22));

        rdbtnMTSP = new JRadioButton("MTSP");
        rdbtnMTSP.setSelected(true); // Seleccionado por defecto para facilitar pruebas

        ButtonGroup grupoModo = new ButtonGroup();
        grupoModo.add(rdbtnMTSP);


        pnlConfig.add(new JLabel("Escenario:"));
        pnlConfig.add(comboEscenario);

        pnlConfig.add(new JLabel("Seed:"));
        pnlConfig.add(spinSeed);

        pnlConfig.add(new JLabel("Drones:"));
        pnlConfig.add(spinDrones);

        pnlConfig.add(new JLabel("Tipo:"));
        pnlConfig.add(rdbtnMTSP);

        // ===============================
        // PARAMETROS AG
        // ===============================

        JPanel pnlParams = new JPanel();
        pnlParams.setBounds(20, 65, 1090, 65);

        pnlParams.setBorder(
                BorderFactory.createTitledBorder(
                        null,
                        "Configuración del Algoritmo",
                        TitledBorder.LEADING,
                        TitledBorder.TOP,
                        new Font("Tahoma", Font.BOLD, 11)
                )
        );

        pnlParams.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 5));
        contentPane.add(pnlParams);

        Dimension dimSpin = new Dimension(50, 22);

        spinPob = new JSpinner(new SpinnerNumberModel(100, 10, 1000, 10));
        spinPob.setPreferredSize(dimSpin);

        spinGens = new JSpinner(new SpinnerNumberModel(200, 1, 5000, 50));
        spinGens.setPreferredSize(dimSpin);

        spinCruce = new JSpinner(new SpinnerNumberModel(60, 0, 100, 5));
        spinCruce.setPreferredSize(dimSpin);

        spinMut = new JSpinner(new SpinnerNumberModel(5, 0, 100, 1));
        spinMut.setPreferredSize(dimSpin);

        spinElite = new JSpinner(new SpinnerNumberModel(5, 0, 50, 1));
        spinElite.setPreferredSize(dimSpin);

        comboSeleccion = new JComboBox<>(new String[]{
                "Torneo", "Ruleta", "Estocástico", "Restos", "Ranking", "Truncamiento"
        });

        comboCruceOp = new JComboBox<>(new String[]{
                "PMX", "OX", "OXPP", "CX", "ERX", "Ordinal", "Inventado"
        });

        comboMutacionOp = new JComboBox<>(new String[]{
                "Inserción", "Intercambio", "Inversión", "Heurística", "Propia"
        });

        pnlParams.add(new JLabel("Pob:"));
        pnlParams.add(spinPob);

        pnlParams.add(new JLabel("Gen:"));
        pnlParams.add(spinGens);

        pnlParams.add(new JLabel("Cr%:"));
        pnlParams.add(spinCruce);

        pnlParams.add(new JLabel("Mu%:"));
        pnlParams.add(spinMut);

        pnlParams.add(new JLabel("El%:"));
        pnlParams.add(spinElite);

        pnlParams.add(new JLabel("Sel:"));
        pnlParams.add(comboSeleccion);

        pnlParams.add(new JLabel("Cruce:"));
        pnlParams.add(comboCruceOp);

        pnlParams.add(new JLabel("Mutación:"));
        pnlParams.add(comboMutacionOp);

        btnEjecutar = new JButton("EJECUTAR AG");

        btnEjecutar.setPreferredSize(new Dimension(130, 30));
        btnEjecutar.setFont(new Font("Tahoma", Font.BOLD, 11));
        btnEjecutar.setBackground(new Color(39, 174, 96));
        btnEjecutar.setForeground(Color.WHITE);

        pnlParams.add(btnEjecutar);

        // ===============================
        // MAPA
        // ===============================

        panelMapa = new PanelMapaReal();
        panelMapa.setBounds(20, 140, 520, 390);
        panelMapa.setBorder(BorderFactory.createTitledBorder("Visualización"));
        contentPane.add(panelMapa);

        // ===============================
        // CONSOLA
        // ===============================

        textAreaResultados = new JTextArea();
        textAreaResultados.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textAreaResultados.setEditable(false);
                
        // ===
        // Recorrido Drones
        //======
        textAreaRutas = new JTextArea();
        textAreaRutas.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textAreaRutas.setEditable(false);

        JScrollPane scrollRutas = new JScrollPane(textAreaRutas);
        scrollRutas.setBounds(555, 430, 555, 120);
        scrollRutas.setBorder(BorderFactory.createTitledBorder("Rutas de Drones"));

        contentPane.add(scrollRutas);

        // ===============================
        // CROMOSOMA SOLUCION
        // ===============================
        textPaneCromosoma = new JTextPane();
        textPaneCromosoma.setEditable(false);

        JScrollPane scrollCrom = new JScrollPane(textPaneCromosoma);
        scrollCrom.setBounds(20, 530, 520, 40);
        scrollCrom.setBorder(BorderFactory.createTitledBorder("Cromosoma Solución"));

        contentPane.add(scrollCrom);

        // ===============================
        // GRAFICA
        // ===============================

        panelGrafica = new PanelGrafica();
        panelGrafica.setBounds(20, 580, 1090, 320);
        panelGrafica.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        contentPane.add(panelGrafica);
        
     // ===============================
        // PANEL RESULTADO FINAL MTSP
        // ===============================
        panelResultadoFinal = new JPanel();
        panelResultadoFinal.setLayout(new GridLayout(1, 1)); // Fuerza al texto a ocupar todo el panel
        panelResultadoFinal.setBounds(555, 130, 555, 120);
        panelResultadoFinal.setBorder(BorderFactory.createTitledBorder("Resultado Final MTSP"));
        
        textPaneReporteFinal = new JTextPane();
        textPaneReporteFinal.setEditable(false);
        textPaneReporteFinal.setBackground(Color.WHITE); // Fondo blanco para verlo claro

        JScrollPane scrollReporte = new JScrollPane(textPaneReporteFinal);
        scrollReporte.setBorder(null);
        
        panelResultadoFinal.add(scrollReporte); // Añadimos el scroll al panel
        contentPane.add(panelResultadoFinal);   // Añadimos el panel a la ventana

        // ===============================
        // LISTENERS
        // =============================== // LISTENERS
        // ===============================
     // ===============================
     // ZONA DERECHA: REPORTE FINAL EXACTO (COMO EN LA FOTO)
     // ===============================
     panelResultadoFinal = new JPanel(new BorderLayout());
     panelResultadoFinal.setBounds(555, 130, 555, 120);
     // Importante: Sin bordes para que quede como texto limpio
     contentPane.add(panelResultadoFinal);

     // Inicializamos la variable global (asegúrate de tener 'private JTextPane textPaneReporteFinal;' arriba en tu clase)
     textPaneReporteFinal = new JTextPane();
     textPaneReporteFinal.setEditable(false);
     textPaneReporteFinal.setOpaque(false); // Fondo transparente
     textPaneReporteFinal.setBorder(null);
     panelResultadoFinal.add(textPaneReporteFinal, BorderLayout.CENTER);
     contentPane.add(textPaneReporteFinal); 
        // ===============================
        // LISTENERS
        // ===============================
        btnEjecutar.addActionListener(this::ejecutarAG);
        comboEscenario.addActionListener(e -> cargarEscenario(comboEscenario.getSelectedIndex()));

        cargarEscenario(0);
    }
    // =====================================
    // EJECUTAR ALGORITMO
    // =====================================

    private void ejecutarAG(ActionEvent e) {

        new Thread(() -> {

            SwingUtilities.invokeLater(() -> {
                panelGrafica.limpiar();
                btnEjecutar.setEnabled(false);
                textAreaResultados.setText("Ejecutando algoritmo...\n");
            });

            int escIdx = comboEscenario.getSelectedIndex();

            EscenarioDatos datos = EscenariosFactory.cargar(escIdx);

            Mapa mapaActual = (Mapa) datos.mapaObj;

            mapaActual.setMatrizImportancia(datos.importancia);

            int numDrones = (int) spinDrones.getValue();
            long seed = ((Number) spinSeed.getValue()).longValue();

            int tPob = (int) spinPob.getValue();
            int tGen = (int) spinGens.getValue();

            double pCruce = (int) spinCruce.getValue() / 100.0;
            double pMut = (int) spinMut.getValue() / 100.0;
            double pElite = (int) spinElite.getValue() / 100.0;

            String seleccion = (String) comboSeleccion.getSelectedItem();
            String cruce = (String) comboCruceOp.getSelectedItem();
            String mutacion = (String) comboMutacionOp.getSelectedItem();

            // -------------------------
            // AG MTSP DIRECTO
            // -------------------------
            if (rdbtnMTSP.isSelected()) {
                SwingUtilities.invokeLater(() -> panelMapa.limpiar());

                // Crear AG con lista vacía para usar su rnd con la semilla
                AlgoritmoGeneticoMTSP agMTSP = new AlgoritmoGeneticoMTSP(
                    mapaActual, new ArrayList<>(), numDrones, seed, this
                );

                // Generacion de camaras aleatorias con la semilla fijada
                List<Camara> camarasGeneradas = agMTSP.generarCamarasAleatorias(datos.numCamaras);
                System.out.println("Cámaras generadas: " + camarasGeneradas.size() + " de " + datos.numCamaras);
                agMTSP.setPuntosControl(camarasGeneradas);

                IndividuoMTSP mtsp = agMTSP.ejecutar(
                    tPob, tGen, pCruce, pMut, pElite,
                    seleccion, cruce, mutacion
                );

                if (mtsp == null) {
                    SwingUtilities.invokeLater(() ->
                        textAreaResultados.append("ERROR: A* no encontró rutas válidas.\n")
                    );
                    SwingUtilities.invokeLater(() -> btnEjecutar.setEnabled(true));
                    return;
                }

                SwingUtilities.invokeLater(() ->
                    mostrarCromosomaColoreado(mtsp, numDrones, camarasGeneradas.size())
                );
                mostrarRutasRealesMTSP(agMTSP, mtsp, camarasGeneradas, mapaActual);
            

             // ==========================================
                // --- NUEVO: LLAMADA AL PANEL RESULTADO ---
                // ==========================================
                
                // 1. Decodificamos las rutas del mejor individuo
                List<List<Integer>> rutasFinales = agMTSP.decodificar(mtsp);
                
                // 2. Calculamos los tiempos EXACTAMENTE igual que en tu método ejecutar()
                double[] arrayTiempos = agMTSP.calcularTiemposDrones(mtsp);
                List<Double> tiemposFinales = new ArrayList<>();
                if (arrayTiempos != null) { 
                    for (double t : arrayTiempos) {
                        tiemposFinales.add(t);
                    }
                }

                // 3. Enviamos los datos al panel visual (usamos invokeLater porque tocamos la interfaz)
                SwingUtilities.invokeLater(() -> {
                    // Usamos mtsp.fitness como makespan y pasamos la lista de tiempos calculada
                    mostrarResultadoFinalMTSP(mtsp.fitness, seed, new ArrayList<>(), tiemposFinales, rutasFinales); 
                });
                
                // ==========================================
            SwingUtilities.invokeLater(() -> btnEjecutar.setEnabled(true));
            }
        }).start();
    }

    public void actualizarMapaRealEnTiempoReal(List<CamaraReal> cams, int g, double f, int r, double a) {
        SwingUtilities.invokeLater(() -> {
            panelMapa.setCamaras(cams, r, a);
            panelMapa.repaint();
        });
    }

    public void actualizarMapaEnTiempoReal(Individuo ind, int gen) {
        SwingUtilities.invokeLater(() -> {
            List<CamaraReal> visual = new ArrayList<>();
            for (Camara c : ind.camaras) visual.add(new CamaraReal(c.x, c.y, 0));
            panelMapa.setCamaras(visual, 1, 0); 
            panelMapa.repaint();
        });
    }

   

    public void actualizarGrafica(double mGen, double mAbs, double med) {
        SwingUtilities.invokeLater(() -> panelGrafica.agregarDatos(mGen, mAbs, med));
    }

    private void cargarEscenario(int idx) {
        EscenarioDatos d = EscenariosFactory.cargar(idx);
        panelMapa.setMapa(d.mapa, d.importancia);
        panelMapa.repaint();
        String nombreEsc = comboEscenario.getSelectedItem().toString();
        textAreaResultados.setText("Escenario: " + nombreEsc + "\n");
    }

    private void mostrarRutasRealesMTSP(
            AlgoritmoGeneticoMTSP agMTSP,
            IndividuoMTSP mejor,
            List<Camara> puntosControl,
            Mapa mapaActual) {

        if (mejor == null) return;

        AStar aStar = new AStar(mapaActual);

        List<List<Point>> rutasVisual = new ArrayList<>();

        List<List<Integer>> rutas = agMTSP.decodificar(mejor);
        SwingUtilities.invokeLater(() ->
            mostrarRutasTexto(rutas, puntosControl)
        );

        Set<String> posicionesCamaras = new HashSet<>();
        for (Camara c : puntosControl)
            posicionesCamaras.add(c.x + "," + c.y);

        for (int d = 0; d < rutas.size(); d++) {

            Dron dron = agMTSP.getFlota().get(d);
            List<Integer> ruta = rutas.get(d);

            List<Point> caminoCompleto = new ArrayList<>();

            int xActual = dron.getBaseX();
            int yActual = dron.getBaseY();

            for (int idCam : ruta) {

                Camara destino = puntosControl.get(idCam);

                List<Point> camino = aStar.calcularRuta(
                        xActual, yActual,
                        destino.x, destino.y,
                        posicionesCamaras
                );

                if (camino != null)
                    caminoCompleto.addAll(camino);

                xActual = destino.x;
                yActual = destino.y;
            }

            // vuelta a base
            List<Point> vuelta = aStar.calcularRuta(
                    xActual, yActual,
                    dron.getBaseX(), dron.getBaseY(),
                    posicionesCamaras
            );

            if (vuelta != null)
                caminoCompleto.addAll(vuelta);

            rutasVisual.add(caminoCompleto);
        }

        // Mostrar cámaras como puntos en el mapa
        List<CamaraReal> camarasVisual = new ArrayList<>();
        for (Camara c : puntosControl)
            camarasVisual.add(new CamaraReal(c.x, c.y, 0));

        SwingUtilities.invokeLater(() -> {
            panelMapa.setCamaras(camarasVisual, 0, 0); // rango=0, apertura=0 → solo puntos
        });

        panelMapa.setRutasDrones(rutasVisual);
        panelMapa.repaint();
    }

    private void mostrarRutasTexto(List<List<Integer>> rutas, List<Camara> puntosControl) {

        StringBuilder sb = new StringBuilder();

        sb.append("===== RUTAS DRONES =====\n");

        for (int i = 0; i < rutas.size(); i++) {

            sb.append("Dron ").append(i + 1).append(": Base");

            for (Integer idCam : rutas.get(i)) {

                Camara c = puntosControl.get(idCam);

                sb.append(" -> (")
                  .append(c.x)
                  .append(",")
                  .append(c.y)
                  .append(")");
            }

            sb.append(" -> Base\n");
        }

        sb.append("========================\n");

        textAreaRutas.setText(sb.toString());
    }

    private void mostrarCromosomaColoreado(IndividuoMTSP ind, int numDrones, int numCamaras) {

        if (ind == null) return;

        StyledDocument doc = textPaneCromosoma.getStyledDocument();

        try {
            doc.remove(0, doc.getLength());
        } catch (Exception ignored) {}

        Color[] colores = {
                Color.BLUE,
                Color.MAGENTA,
                Color.GREEN,
                Color.ORANGE,
                Color.CYAN
        };

        int dronActual = 0;

        for (int g : ind.cromosoma) {

            if (g > numCamaras) {

                dronActual++;
                continue;
            }

            Style style = textPaneCromosoma.addStyle("color", null);
            StyleConstants.setForeground(style, colores[dronActual]);

            try {
                doc.insertString(doc.getLength(), g + " ", style);
            } catch (Exception ignored) {}
        }
    }
    
public void mostrarResultadoFinalMTSP(double makespan, long semilla, List<Double> velocidades, List<Double> tiemposPorDron, List<List<Integer>> rutas) {
	  System.out.println("Llamando a pintar el panel... Makespan: " + makespan + ", Drones: " + tiemposPorDron.size());

      long semillaReal = ((Number) spinSeed.getValue()).longValue();

      StyledDocument doc = textPaneReporteFinal.getStyledDocument();
      try { doc.remove(0, doc.getLength()); } catch (Exception ignored) {}

      Style styleBase = textPaneReporteFinal.addStyle("Base", null);
      StyleConstants.setFontFamily(styleBase, "SansSerif");
      StyleConstants.setFontSize(styleBase, 14);
      StyleConstants.setBold(styleBase, true);
      StyleConstants.setForeground(styleBase, Color.BLACK);

      Color[] textColors = {Color.BLUE, Color.MAGENTA, new Color(0, 153, 0), Color.ORANGE, Color.CYAN};

      try {
          doc.insertString(doc.getLength(), String.format("MAKESPAN: %.2f s\n", makespan).replace(".", ","), styleBase);
          doc.insertString(doc.getLength(), "SEMILLA: " + semillaReal + "\n", styleBase);

          doc.insertString(doc.getLength(), "TIEMPOS POR DRON: ", styleBase);
          for (int i = 0; i < tiemposPorDron.size(); i++) {
              Style styleColor = textPaneReporteFinal.addStyle("cT" + i, styleBase);
              StyleConstants.setForeground(styleColor, textColors[i % textColors.length]);
              doc.insertString(doc.getLength(), String.format("T%d=%.2f  ", (i + 1), tiemposPorDron.get(i)).replace(".", ","), styleColor);
          }
          doc.insertString(doc.getLength(), "\n", styleBase);

          doc.insertString(doc.getLength(), "CROMOSOMA: [ ", styleBase);
          for (int d = 0; d < rutas.size(); d++) {
              List<Integer> ruta = rutas.get(d);
              Style styleColor = textPaneReporteFinal.getStyle("cT" + d);

              for (Integer gen : ruta) {
                  doc.insertString(doc.getLength(), gen + " ", styleColor);
              }
              if (d < rutas.size() - 1) {
                  doc.insertString(doc.getLength(), "|| ", styleBase);
              }
          }
          doc.insertString(doc.getLength(), "]", styleBase);

          // --- FORZAR A SWING A ACTUALIZAR LA PANTALLA ---
          textPaneReporteFinal.revalidate();
          //textPaneReporteFinal.repaint();

      } catch (Exception e) {
          e.printStackTrace(); // Si hay algún error oculto, lo veremos aquí
      }
  }
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        EventQueue.invokeLater(() -> {
            new VentanaPrincipal().setVisible(true);
        });
    }    
}