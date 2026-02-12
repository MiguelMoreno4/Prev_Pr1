package source;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    private JPanel contentPane;
    private PanelMapa panelMapa;
    private JButton btnEjecutar;
    private JCheckBox chckbxPonderado;
    private JComboBox<String> comboEscenario;
    private JRadioButton rdbtnNormal, rdbtnReal;
    private JTextArea textAreaResultados;

    private Individuo mejorAbsoluto = null;
    private double mejorFitnessAbsoluto = -1;

    public VentanaPrincipal() {

        setTitle("Optimización de Cámaras - AG");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 820, 520);

        contentPane = new JPanel();
        contentPane.setLayout(null);
        setContentPane(contentPane);

        // ===== ESCENARIO =====
        comboEscenario = new JComboBox<>(new String[]{
                "Escenario 1 - Museo",
                "Escenario 2 - Pasillos",
                "Escenario 3 - Supermercado"
        });
        comboEscenario.setBounds(20, 15, 220, 25);
        contentPane.add(comboEscenario);

        // ===== PONDERADO =====
        chckbxPonderado = new JCheckBox("Modo ponderado");
        chckbxPonderado.setBounds(260, 15, 150, 25);
        contentPane.add(chckbxPonderado);

        // ===== MODO NORMAL/REAL =====
        rdbtnNormal = new JRadioButton("Normal");
        rdbtnNormal.setBounds(420, 15, 80, 25);
        rdbtnNormal.setSelected(true);

        rdbtnReal = new JRadioButton("Real");
        rdbtnReal.setBounds(510, 15, 80, 25);

        ButtonGroup grupoModo = new ButtonGroup();
        grupoModo.add(rdbtnNormal);
        grupoModo.add(rdbtnReal);

        contentPane.add(rdbtnNormal);
        contentPane.add(rdbtnReal);

        // ===== BOTÓN EJECUTAR =====
        btnEjecutar = new JButton("Ejecutar AG");
        btnEjecutar.setBounds(610, 15, 150, 25);
        contentPane.add(btnEjecutar);

        // ===== MAPA =====
        panelMapa = new PanelMapa();
        panelMapa.setBounds(20, 60, 350, 350);
        panelMapa.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        contentPane.add(panelMapa);

        // ===== RESULTADOS =====
        textAreaResultados = new JTextArea();
        textAreaResultados.setEditable(false);
        JScrollPane scroll = new JScrollPane(textAreaResultados);
        scroll.setBounds(400, 60, 380, 350);
        contentPane.add(scroll);

        btnEjecutar.addActionListener(this::ejecutarAG);

        cargarEscenario(0);
    }

    /**
     * Función común para ejecutar AG. 
     * Decide si usar modo normal o real según el radioButton.
     */
    private void ejecutarAG(ActionEvent e) {

        int escenario = comboEscenario.getSelectedIndex();
        EscenarioDatos datos = EscenariosFactory.cargar(escenario);

        panelMapa.setMapa(datos.mapa, datos.importancia);

        boolean ponderado = chckbxPonderado.isSelected();
        boolean modoReal = rdbtnReal.isSelected();

        if (modoReal) {
            ejecutarAGReal(datos, ponderado);
        } else {
            ejecutarAGNormal(datos, ponderado);
        }
    }

    /**
     * Ejecución del AG normal (V1)
     */
    private void ejecutarAGNormal(EscenarioDatos datos, boolean ponderado) {

        AlgoritmoGenetico ag = new AlgoritmoGenetico(
                datos.mapaObj,
                datos.rango,
                datos.numCamaras
        );
        ag.setModoPonderado(ponderado);
        if (ponderado) ag.setImportancia(datos.importancia);

        Individuo candidato = ag.ejecutar(200, 0.15);

        mostrarResultados(candidato);
    }

    /**
     * Ejecución del AG real (V2 con cámaras orientables)
     * Aquí se llama a tu AlgoritmoGeneticoReal
     */
    private void ejecutarAGReal(EscenarioDatos datos, boolean ponderado) {

        AlgoritmoGeneticoReal agReal = new AlgoritmoGeneticoReal(
                datos.mapaObj,
                datos.rango,
                datos.numCamaras,
                60.0 // ángulo de apertura por defecto
        );
        agReal.setModoPonderado(ponderado);
        if (ponderado) agReal.setImportancia(datos.importancia);

        IndividuoReal candidato = agReal.ejecutar(200, 0.15);

        mostrarResultadosReal(candidato);
    }

    /**
     * Mostrar resultados del AG normal
     */
    private void mostrarResultados(Individuo candidato) {
        if (mejorAbsoluto == null || candidato.fitness > mejorFitnessAbsoluto) {
            mejorFitnessAbsoluto = candidato.fitness;
            mejorAbsoluto = copiarIndividuo(candidato);

            textAreaResultados.setText("");
            textAreaResultados.append("MEJOR SOLUCIÓN\n");
            textAreaResultados.append("Fitness = " + mejorFitnessAbsoluto + "\n\n");

            for (Camara c : mejorAbsoluto.camaras) {
                textAreaResultados.append("Cámara en (" + c.x + "," + c.y + ")\n");
            }

            panelMapa.setCamaras(mejorAbsoluto.camaras);
        }
    }

    /**
     * Mostrar resultados del AG real
     */
    private void mostrarResultadosReal(IndividuoReal candidato) {
        if (mejorAbsoluto == null || candidato.fitness > mejorFitnessAbsoluto) {
            mejorFitnessAbsoluto = candidato.fitness;

            // Convertir IndividuoReal a camaras redondeadas para el panel
            mejorAbsoluto = new Individuo();
            for (CamaraReal c : candidato.camaras) {
                mejorAbsoluto.camaras.add(new Camara(
                        (int)Math.round(c.x),
                        (int)Math.round(c.y)
                ));
            }
            mejorAbsoluto.fitness = candidato.fitness;

            textAreaResultados.setText("");
            textAreaResultados.append("MEJOR SOLUCIÓN (Real)\n");
            textAreaResultados.append("Fitness = " + mejorFitnessAbsoluto + "\n\n");

            for (CamaraReal c : candidato.camaras) {
                textAreaResultados.append(String.format(
                        "Cámara en (%.2f, %.2f), θ=%.1f°\n",
                        c.x, c.y, c.theta
                ));
            }

            panelMapa.setCamaras(mejorAbsoluto.camaras);
        }
    }

    private void cargarEscenario(int idx) {
        EscenarioDatos d = EscenariosFactory.cargar(idx);
        panelMapa.setMapa(d.mapa, d.importancia);
    }

    private Individuo copiarIndividuo(Individuo ind) {
        Individuo copia = new Individuo();
        for (Camara c : ind.camaras) {
            copia.camaras.add(new Camara(c.x, c.y));
        }
        copia.fitness = ind.fitness;
        return copia;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}