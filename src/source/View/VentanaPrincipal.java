package source.View;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import source.AlgoritmosGeneticos.AlgoritmoGenetico;
import source.AlgoritmosGeneticos.AlgoritmoGeneticoMTSP;
import source.AlgoritmosGeneticos.AlgoritmoGeneticoReal;
import source.Camaras.Camara;
import source.Camaras.CamaraReal;
import source.Escenarios.EscenarioDatos;
import source.Escenarios.EscenariosFactory;
import source.Individuos.Individuo;
import source.Individuos.IndividuoMTSP;
import source.Individuos.IndividuoReal;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VentanaPrincipal extends JFrame {

    private JPanel contentPane;
    private PanelMapaReal panelMapa; 
    private JButton btnEjecutar;
    private JCheckBox chckbxPonderado;
    private JComboBox<String> comboEscenario;
    private JRadioButton rdbtnNormal, rdbtnReal;
    private JTextArea textAreaResultados;
    private PanelGrafica panelGrafica;

    private JLabel lblGenActual, lblMejorFitness;
    private JSpinner spinPob, spinGens, spinCruce, spinMut, spinElite;
    private JComboBox<String> comboSeleccion, comboCruce;
   
    
 // --- Atributos de clase ---
   // private JComboBox<String> comboSeleccion;
    private JComboBox<String> comboCruceOp;
    private JComboBox<String> comboMutacionOp;

    public VentanaPrincipal() {
        setTitle("Optimización de Cámaras - Panel de Control AG");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(50, 50, 1150, 800); 

        contentPane = new JPanel();
        contentPane.setLayout(null);
        setContentPane(contentPane);

        // ===== BLOQUE CONFIGURACIÓN SUPERIOR =====
        JPanel pnlConfig = new JPanel();
        pnlConfig.setBounds(20, 10, 1090, 50);
        pnlConfig.setBorder(BorderFactory.createEtchedBorder());
        pnlConfig.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        contentPane.add(pnlConfig);

        comboEscenario = new JComboBox<>(new String[]{"Escenario 1 - Museo", "Escenario 2 - Pasillos", "Escenario 3 - Supermercado"});
        rdbtnNormal = new JRadioButton("Binario", true);
        rdbtnReal = new JRadioButton("Real");
        ButtonGroup grupoModo = new ButtonGroup();
        grupoModo.add(rdbtnNormal); grupoModo.add(rdbtnReal);

        pnlConfig.add(new JLabel("Escenario:")); pnlConfig.add(comboEscenario);
        pnlConfig.add(Box.createHorizontalStrut(20));
        pnlConfig.add(new JLabel("Tipo:")); pnlConfig.add(rdbtnNormal); pnlConfig.add(rdbtnReal);
        pnlConfig.add(Box.createHorizontalStrut(20));
        chckbxPonderado = new JCheckBox("Ponderado");
        pnlConfig.add(chckbxPonderado);

        // ===== BLOQUE PARÁMETROS AG =====
        JPanel pnlParams = new JPanel();
        pnlParams.setBounds(20, 65, 1090, 65);
        pnlParams.setBorder(BorderFactory.createTitledBorder(null, "Configuración del Algoritmo", TitledBorder.LEADING, TitledBorder.TOP, new Font("Tahoma", Font.BOLD, 11)));
        pnlParams.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 5));
        contentPane.add(pnlParams);

        Dimension dimSpin = new Dimension(50, 22);
        spinPob = new JSpinner(new SpinnerNumberModel(100, 10, 1000, 10)); spinPob.setPreferredSize(dimSpin);
        spinGens = new JSpinner(new SpinnerNumberModel(200, 1, 5000, 50)); spinGens.setPreferredSize(dimSpin);
        spinCruce = new JSpinner(new SpinnerNumberModel(60, 0, 100, 5)); spinCruce.setPreferredSize(dimSpin);
        spinMut = new JSpinner(new SpinnerNumberModel(5, 0, 100, 1)); spinMut.setPreferredSize(dimSpin);
        spinElite = new JSpinner(new SpinnerNumberModel(5, 0, 50, 1)); spinElite.setPreferredSize(dimSpin);

        comboSeleccion = new JComboBox<>(new String[]{"Torneo", "Ruleta", "Estocástico", "Truncamiento", "Restos"});
        comboCruceOp = new JComboBox<>(new String[]{"Monopunto", "Uniforme", "Aritmético", "BLX-α"});
        comboMutacionOp = new JComboBox<>(new String[]{"Gen", "Gaussiana"});

        pnlParams.add(new JLabel("Pob:")); pnlParams.add(spinPob);
        pnlParams.add(new JLabel("Gen:")); pnlParams.add(spinGens);
        pnlParams.add(new JLabel("Cr%:")); pnlParams.add(spinCruce);
        pnlParams.add(new JLabel("Mu%:")); pnlParams.add(spinMut);
        pnlParams.add(new JLabel("El%:")); pnlParams.add(spinElite);
        pnlParams.add(new JLabel("Sel:")); pnlParams.add(comboSeleccion);
        pnlParams.add(new JLabel("Cruce Op:")); pnlParams.add(comboCruceOp);
        pnlParams.add(new JLabel("Mutación:")); pnlParams.add(comboMutacionOp);

        btnEjecutar = new JButton("EJECUTAR AG");
        btnEjecutar.setPreferredSize(new Dimension(130, 30));
        btnEjecutar.setFont(new Font("Tahoma", Font.BOLD, 11));
        btnEjecutar.setBackground(new Color(39, 174, 96));
        btnEjecutar.setForeground(Color.WHITE);
        btnEjecutar.setOpaque(true);
        btnEjecutar.setBorderPainted(false);
        btnEjecutar.setFocusPainted(false);
        pnlParams.add(Box.createHorizontalStrut(10));
        pnlParams.add(btnEjecutar);

        // ===== MAPA Y RESULTADOS =====
        panelMapa = new PanelMapaReal();
        panelMapa.setBounds(20, 140, 520, 390); 
        panelMapa.setBorder(BorderFactory.createTitledBorder("Visualización"));
        contentPane.add(panelMapa);

        textAreaResultados = new JTextArea();
        textAreaResultados.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textAreaResultados.setEditable(false);
        JScrollPane scroll = new JScrollPane(textAreaResultados);
        scroll.setBounds(555, 140, 555, 390); 
        scroll.setBorder(BorderFactory.createTitledBorder("Consola de Resultados"));
        contentPane.add(scroll);

        lblGenActual = new JLabel("Generación: 0");
        lblGenActual.setFont(new Font("Tahoma", Font.BOLD, 14));
        lblGenActual.setBounds(30, 540, 200, 25);
        contentPane.add(lblGenActual);

        lblMejorFitness = new JLabel("Mejor Fitness: 0.00");
        lblMejorFitness.setFont(new Font("Tahoma", Font.BOLD, 14));
        lblMejorFitness.setForeground(new Color(41, 128, 185));
        lblMejorFitness.setBounds(250, 540, 400, 25);
        contentPane.add(lblMejorFitness);

        panelGrafica = new PanelGrafica();
        panelGrafica.setBounds(20, 575, 1090, 170);
        panelGrafica.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        contentPane.add(panelGrafica);

        btnEjecutar.addActionListener(this::ejecutarAG);
        comboEscenario.addActionListener(e -> cargarEscenario(comboEscenario.getSelectedIndex()));
        cargarEscenario(0);
        
        //probarAStarMapa10x10();
        probarAGMTSPMapa10x10();
    }

    // --- EJECUTAR AG CORREGIDO ---
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
       
            
            int tPob = (int) spinPob.getValue();
            int tGen = (int) spinGens.getValue();
            double pCruce = (int) spinCruce.getValue() / 100.0;
            double pMut = (int) spinMut.getValue() / 100.0;
            double pElite = (int) spinElite.getValue() / 100.0;
            boolean ponderado = chckbxPonderado.isSelected();

            if (!rdbtnReal.isSelected()) {
                AlgoritmoGenetico ag = new AlgoritmoGenetico(mapaActual, datos.rango, datos.numCamaras, this);
                ag.setModoPonderado(ponderado);

                switch (comboSeleccion.getSelectedIndex()) {
                    case 0: ag.setMetodoSeleccion(AlgoritmoGenetico.MetodoSeleccion.TORNEO); break;
                    case 1: ag.setMetodoSeleccion(AlgoritmoGenetico.MetodoSeleccion.RULETA); break;
                    case 2: ag.setMetodoSeleccion(AlgoritmoGenetico.MetodoSeleccion.ESTOCASTICO); break;
                    case 3: ag.setMetodoSeleccion(AlgoritmoGenetico.MetodoSeleccion.TRUNCAMIENTO); break;
                    case 4: ag.setMetodoSeleccion(AlgoritmoGenetico.MetodoSeleccion.RESTOS); break;
                }

                ag.setMetodoCruce(AlgoritmoGenetico.MetodoCruce.MONOPUNTO);
                Individuo mejor = ag.ejecutar(tGen, pMut, pCruce);
                SwingUtilities.invokeLater(() -> mostrarResultados(mejor));

            } else {
                AlgoritmoGeneticoReal agReal = new AlgoritmoGeneticoReal(mapaActual, datos.rango, datos.numCamaras, datos.apertura, this);
                agReal.setModoPonderado(ponderado);

                switch (comboSeleccion.getSelectedIndex()) {
                    case 0: agReal.setMetodoSeleccion(AlgoritmoGeneticoReal.MetodoSeleccion.TORNEO); break;
                    case 1: agReal.setMetodoSeleccion(AlgoritmoGeneticoReal.MetodoSeleccion.RULETA); break;
                    case 2: agReal.setMetodoSeleccion(AlgoritmoGeneticoReal.MetodoSeleccion.ESTOCASTICO); break;
                    case 3: agReal.setMetodoSeleccion(AlgoritmoGeneticoReal.MetodoSeleccion.TRUNCAMIENTO); break;
                    case 4: agReal.setMetodoSeleccion(AlgoritmoGeneticoReal.MetodoSeleccion.RESTOS); break;
                }

                switch (comboCruceOp.getSelectedIndex()) {
                    case 0: agReal.setMetodoCruce(AlgoritmoGeneticoReal.MetodoCruce.MONOPUNTO); break;
                    case 1: agReal.setMetodoCruce(AlgoritmoGeneticoReal.MetodoCruce.UNIFORME); break;
                    case 2: agReal.setMetodoCruce(AlgoritmoGeneticoReal.MetodoCruce.ARITMETICO); break;
                    case 3: agReal.setMetodoCruce(AlgoritmoGeneticoReal.MetodoCruce.BLX_ALPHA); break;
                }

                switch (comboMutacionOp.getSelectedIndex()) {
                    case 0: agReal.setMetodoMutacion(AlgoritmoGeneticoReal.MetodoMutacion.GEN); break;
                    case 1: agReal.setMetodoMutacion(AlgoritmoGeneticoReal.MetodoMutacion.GAUSSIANA); break;
                }

                agReal.setConfig(tPob, pCruce, pMut, pElite);
                IndividuoReal mejor = agReal.ejecutar(tGen);
                SwingUtilities.invokeLater(() -> mostrarResultadosReal(mejor, datos.rango, datos.apertura));
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
 // --- PRUEBA A* MAPA 10x10 ---
    private void probarAStarMapa10x10() {
        EscenarioDatos datos = EscenariosFactory.cargar(0); // mapa1
        Mapa mapaActual = (Mapa) datos.mapaObj;
        mapaActual.setMatrizImportancia(datos.importancia);
        AStar aStar = new AStar(mapaActual);

        // Crear conjunto vacío de cámaras para penalización
        Set<String> posicionesCamaras = new HashSet<>();

        // Coordenadas seguras dentro del mapa 10x10
        int origenX = 0, origenY = 0;
        int destinoX = 9, destinoY = 9;

        double coste = aStar.calcularCoste(origenX, origenY, destinoX, destinoY, posicionesCamaras);
        System.out.println("Coste A* mapa1 (0,0 → 9,9) = " + coste);
    }
    private void probarAGMTSPMapa10x10() {
        EscenarioDatos datos = EscenariosFactory.cargar(0); // mapa1
        Mapa mapaActual = (Mapa) datos.mapaObj;
        mapaActual.setMatrizImportancia(datos.importancia);

        // Crear puntos de control del mapa
        List<Camara> puntosControl = new ArrayList<>();
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                if (!mapaActual.esObstaculo(x, y)) {
                    puntosControl.add(new Camara(x, y));
                }
            }
        }

        int numDrones = 2;
        long semilla = 12345L;

        AlgoritmoGeneticoMTSP agMTSP = new AlgoritmoGeneticoMTSP(
            mapaActual,
            puntosControl,
            numDrones,
            semilla,
            this
        );

        // Ejecutar pocas generaciones solo para prueba
        IndividuoMTSP mejor = agMTSP.ejecutar(30, 0.8, 0.2);

        textAreaResultados.append("\n=== PRUEBA AG MTSP ===\n");
        textAreaResultados.append("Mejor Fitness: " + 
                String.format("%.2f", mejor.fitness) + "\n");

        List<List<Integer>> rutas = agMTSP.decodificar(mejor);

        // Convertir a puntos visuales
        List<List<Point>> rutasVisual = new ArrayList<>();

        for (int d = 0; d < rutas.size(); d++) {

            List<Point> rutaDrone = new ArrayList<>();

            for (int id : rutas.get(d)) {
                Camara c = puntosControl.get(id);
                rutaDrone.add(new Point(c.x, c.y));
                textAreaResultados.append(
                    String.format("Drone %d -> (%d,%d)\n", 
                    d+1, c.x, c.y));
            }

            rutasVisual.add(rutaDrone);
        }

        panelMapa.setRutasDrones(rutasVisual);
        panelMapa.repaint();
    }
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        EventQueue.invokeLater(() -> {
            new VentanaPrincipal().setVisible(true);
        });
    }    
}