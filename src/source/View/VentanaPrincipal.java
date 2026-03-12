package source.View;


import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import source.AlgoritmosGeneticos.AlgoritmoGenetico;
import source.AlgoritmosGeneticos.AlgoritmoGeneticoMTSP;
import source.AlgoritmosGeneticos.AlgoritmoGeneticoReal;
import source.Camaras.Camara;
import source.Camaras.CamaraReal;
import source.Camaras.Dron;
import source.Escenarios.EscenarioDatos;
import source.Escenarios.EscenariosFactory;
import source.Individuos.Individuo;
import source.Individuos.IndividuoMTSP;
import source.Individuos.IndividuoReal;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    private JPanel contentPane;
    private PanelMapaReal panelMapa;
    private JButton btnEjecutar;
    private JCheckBox chckbxPonderado;
    private JComboBox<String> comboEscenario;

    private JRadioButton rdbtnNormal, rdbtnReal;

    private JTextArea textAreaResultados;
    private JTextArea textAreaRutas;
    private PanelGrafica panelGrafica;

    private JLabel lblGenActual, lblMejorFitness;

    private JSpinner spinPob, spinGens, spinCruce, spinMut, spinElite;

    private JComboBox<String> comboSeleccion;
    private JComboBox<String> comboCruceOp;
    private JComboBox<String> comboMutacionOp;
    
    private JTextPane textPaneCromosoma;
    
    // NUEVOS CONTROLES
    private JSpinner spinSeed;
    private JSpinner spinDrones;

    public VentanaPrincipal() {

        setTitle("Optimización de Cámaras - Panel de Control AG");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(50, 50, 1150, 800);

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

        rdbtnNormal = new JRadioButton("Binario", true);
        rdbtnReal = new JRadioButton("Real");

        ButtonGroup grupoModo = new ButtonGroup();
        grupoModo.add(rdbtnNormal);
        grupoModo.add(rdbtnReal);

        chckbxPonderado = new JCheckBox("Ponderado");

        pnlConfig.add(new JLabel("Escenario:"));
        pnlConfig.add(comboEscenario);

        pnlConfig.add(new JLabel("Seed:"));
        pnlConfig.add(spinSeed);

        pnlConfig.add(new JLabel("Drones:"));
        pnlConfig.add(spinDrones);

        pnlConfig.add(new JLabel("Tipo:"));
        pnlConfig.add(rdbtnNormal);
        pnlConfig.add(rdbtnReal);

        pnlConfig.add(chckbxPonderado);

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
                "Torneo",
                "Ruleta",
                "Estocástico",
                "Restos",
                "Ranking",
                "Truncamiento"
        });

        comboCruceOp = new JComboBox<>(new String[]{
                "PMX",
                "OX",
                "OXPP",
                "CX",
                "ERX",
                "Ordinal",
                "Inventado"
        });

        comboMutacionOp = new JComboBox<>(new String[]{
                "Inserción",
                "Intercambio",
                "Inversión",
                "Heurística",
                "Propia"
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

        JScrollPane scroll = new JScrollPane(textAreaResultados);
        scroll.setBounds(555, 140, 555, 260);
        scroll.setBorder(BorderFactory.createTitledBorder("Consola de Resultados"));

        contentPane.add(scroll);
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
        // ESTADO
        // ===============================

        lblGenActual = new JLabel("Generación: 0");
        lblGenActual.setFont(new Font("Tahoma", Font.BOLD, 14));
        lblGenActual.setBounds(30, 540, 200, 25);
        contentPane.add(lblGenActual);

        lblMejorFitness = new JLabel("Mejor Fitness: 0.00");
        lblMejorFitness.setFont(new Font("Tahoma", Font.BOLD, 14));
        lblMejorFitness.setForeground(new Color(41, 128, 185));
        lblMejorFitness.setBounds(250, 540, 400, 25);
        contentPane.add(lblMejorFitness);

        // ===============================
        // GRAFICA
        // ===============================

        panelGrafica = new PanelGrafica();
        panelGrafica.setBounds(20, 575, 1090, 170);
        panelGrafica.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        contentPane.add(panelGrafica);
        textPaneCromosoma = new JTextPane();
        textPaneCromosoma.setEditable(false);

        JScrollPane scrollCrom = new JScrollPane(textPaneCromosoma);
        scrollCrom.setBounds(20, 530, 520, 60);
        scrollCrom.setBorder(BorderFactory.createTitledBorder("Cromosoma Solución"));

        contentPane.add(scrollCrom);
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

            boolean ponderado = chckbxPonderado.isSelected();

            // -------------------------
            // AG BINARIO
            // -------------------------

            if (!rdbtnReal.isSelected()) {

                AlgoritmoGenetico ag =
                        new AlgoritmoGenetico(mapaActual, datos.rango, datos.numCamaras, this);

                ag.setModoPonderado(ponderado);

                Individuo mejor = ag.ejecutar(tGen, pMut, pCruce);

                List<Camara> camarasFinales = new ArrayList<>();

                for (Camara c : mejor.camaras)
                    camarasFinales.add(new Camara(c.x, c.y));

                AlgoritmoGeneticoMTSP agMTSP =
                        new AlgoritmoGeneticoMTSP(
                                mapaActual,
                                camarasFinales,
                                numDrones,
                                seed,
                                this
                        );

                IndividuoMTSP mtsp = agMTSP.ejecutarSimulacion();
                SwingUtilities.invokeLater(() ->
                mostrarCromosomaColoreado(mtsp, numDrones, camarasFinales.size())
        );
                mostrarRutasRealesMTSP(
                        agMTSP,
                        mtsp,
                        camarasFinales,
                        mapaActual
                );

                SwingUtilities.invokeLater(() -> mostrarResultados(mejor));
            }

            // -------------------------
            // AG REAL
            // -------------------------

            else {

                AlgoritmoGeneticoReal agReal =
                        new AlgoritmoGeneticoReal(
                                mapaActual,
                                datos.rango,
                                datos.numCamaras,
                                datos.apertura,
                                this
                        );

                agReal.setModoPonderado(ponderado);

                agReal.setConfig(tPob, pCruce, pMut, pElite);

                IndividuoReal mejor = agReal.ejecutar(tGen);

                List<Camara> camarasFinales = new ArrayList<>();

                for (CamaraReal c : mejor.camaras)
                    camarasFinales.add(new Camara(c.getX(), c.getY()));

                AlgoritmoGeneticoMTSP agMTSP =
                        new AlgoritmoGeneticoMTSP(
                                mapaActual,
                                camarasFinales,
                                numDrones,
                                seed,
                                this
                        );

                IndividuoMTSP mtsp = agMTSP.ejecutarSimulacion();
                
                SwingUtilities.invokeLater(() ->
                mostrarCromosomaColoreado(mtsp, numDrones, camarasFinales.size())
        );
                mostrarRutasRealesMTSP(
                        agMTSP,
                        mtsp,
                        camarasFinales,
                        mapaActual
                );

                SwingUtilities.invokeLater(() ->
                        mostrarResultadosReal(mejor, datos.rango, datos.apertura));
                
            }

            SwingUtilities.invokeLater(() -> btnEjecutar.setEnabled(true));

        }).start();
    }

    public void actualizarMapaRealEnTiempoReal(List<CamaraReal> cams, int g, double f, int r, double a) {
        SwingUtilities.invokeLater(() -> {
            panelMapa.setCamaras(cams, r, a);
            panelMapa.repaint();
            lblGenActual.setText("Generación: " + g);
            lblMejorFitness.setText("Mejor Fitness: " + String.format("%.2f", f));
        });
    }

    public void actualizarMapaEnTiempoReal(Individuo ind, int gen) {
        SwingUtilities.invokeLater(() -> {
            List<CamaraReal> visual = new ArrayList<>();
            for (Camara c : ind.camaras) visual.add(new CamaraReal(c.x, c.y, 0));
            panelMapa.setCamaras(visual, 1, 0); 
            panelMapa.repaint();
            lblGenActual.setText("Generación: " + gen);
            lblMejorFitness.setText("Mejor Fitness: " + String.format("%.2f", ind.fitness));
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

    private void mostrarResultadosReal(IndividuoReal ind, int r, double a) {
        textAreaResultados.setText("=== RESULTADOS FINALES (REAL) ===\n");
        textAreaResultados.append("Fitness Máximo: " + String.format("%.2f", ind.fitness) + "\n\n");
        for(int i=0; i<ind.camaras.size(); i++) {
            CamaraReal c = ind.camaras.get(i);
            textAreaResultados.append(String.format("Cámara %d: X:%.1f, Y:%.1f, Áng:%.1f°\n", i+1, c.x, c.y, c.theta));
        }
        panelMapa.setCamaras(ind.camaras, r, a);
        panelMapa.repaint();
    }

    private void mostrarResultados(Individuo ind) {
        textAreaResultados.setText("=== RESULTADOS FINALES (BINARIO) ===\n");
        textAreaResultados.append("Fitness Máximo: " + String.format("%.2f", ind.fitness) + "\n\n");
        List<CamaraReal> lista = new ArrayList<>();
        for(Camara c : ind.camaras) {
            lista.add(new CamaraReal(c.x, c.y, 0));
            textAreaResultados.append(String.format("Cámara en: (%d, %d)\n", (int)c.x, (int)c.y));
        }
        panelMapa.setCamaras(lista, 1, 0);
        panelMapa.repaint();
    }
    private void mostrarRutasRealesMTSP(
            AlgoritmoGeneticoMTSP agMTSP,
            IndividuoMTSP mejor,
            List<Camara> puntosControl,
            Mapa mapaActual) {

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

        panelMapa.setRutasDrones(rutasVisual);
        panelMapa.repaint();
    }
   // private IndividuoMTSP convertirAMTSP(List<Camara> camaras, int numDrones) {
    //    AlgoritmoGeneticoMTSP tmpMTSP = new AlgoritmoGeneticoMTSP(null, camaras, numDrones, 0, this);
    //    return tmpMTSP.decodificar(camaras); // Devuelve IndividuoMTSP válido
   // }
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

            if (g >= numCamaras) {

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
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        EventQueue.invokeLater(() -> {
            new VentanaPrincipal().setVisible(true);
        });
    }    
}