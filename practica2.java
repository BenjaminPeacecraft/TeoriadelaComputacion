import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class SimuladorAFD extends JFrame {

    // --- FASE 2: BACKEND (Lógica del Autómata) ---
    private Set<String> alfabeto = new HashSet<>();
    private Set<String> estados = new HashSet<>();
    private String estadoInicial = null;
    private Set<String> estadosAceptacion = new HashSet<>();
    // Mapa de transiciones: Estado -> (Símbolo -> EstadoDestino)
    private Map<String, Map<String, String>> transiciones = new HashMap<>();

    // Componentes de la Fase 3: Interfaz Gráfica
    private JTextArea txtConsola;
    private JTextField txtCadena;
    private JTable tablaTransiciones;
    private DefaultTableModel modeloTabla;
    private JLabel lblEstadoAutomata;

    public SimuladorAFD() {
        configurarVentana();
        inicializarComponentes();
    }

    private void configurarVentana() {
        setTitle("Simulador AFD - Práctica 2 (Teoría de la Computación)");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
    }

    private void inicializarComponentes() {
        // Panel Superior: Carga de Archivo
        JPanel panelNorte = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnCargar = new JButton("Cargar Autómata (.jff / .xml)");
        lblEstadoAutomata = new JLabel("Ningún autómata cargado.");
        
        btnCargar.addActionListener(e -> cargarAutomataXML());
        panelNorte.add(btnCargar);
        panelNorte.add(lblEstadoAutomata);
        add(panelNorte, BorderLayout.NORTH);

        // Panel Central: Tabla y Consola
        JPanel panelCentro = new JPanel(new GridLayout(2, 1, 5, 5));
        
        modeloTabla = new DefaultTableModel(new Object[]{"Estado Actual", "Símbolo", "Estado Siguiente"}, 0);
        tablaTransiciones = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaTransiciones);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Función de Transición"));
        
        txtConsola = new JTextArea();
        txtConsola.setEditable(false);
        JScrollPane scrollConsola = new JScrollPane(txtConsola);
        scrollConsola.setBorder(BorderFactory.createTitledBorder("Consola de Resultados"));
        
        panelCentro.add(scrollTabla);
        panelCentro.add(scrollConsola);
        add(panelCentro, BorderLayout.CENTER);

        // Panel Inferior: Operaciones
        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSur.add(new JLabel("Cadena:"));
        txtCadena = new JTextField(15);
        panelSur.add(txtCadena);
        
        JButton btnValidar = new JButton("Validar Cadena");
        JButton btnCadenas = new JButton("Prefijos/Sufijos");
        JButton btnCerraduras = new JButton("Cerraduras (n=3)");

        btnValidar.addActionListener(e -> validarCadena());
        btnCadenas.addActionListener(e -> calcularSubcadenas());
        btnCerraduras.addActionListener(e -> calcularCerraduras());

        panelSur.add(btnValidar);
        panelSur.add(btnCadenas);
        panelSur.add(btnCerraduras);
        add(panelSur, BorderLayout.SOUTH);
    }

    // --- LÓGICA DE PARSEO DE ARCHIVOS JFLAP (.jff / .xml) ---
    private void cargarAutomataXML() {
        JFileChooser fileChooser = new JFileChooser();
        int seleccion = fileChooser.showOpenDialog(this);
        
        if (seleccion == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(archivo);
                doc.getDocumentElement().normalize();

                limpiarAutomata();

                // 1. Leer Estados
                NodeList nListEstados = doc.getElementsByTagName("state");
                for (int i = 0; i < nListEstados.getLength(); i++) {
                    Node nNode = nListEstados.item(i);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        String idEstado = eElement.getAttribute("id");
                        estados.add(idEstado);
                        
                        if (eElement.getElementsByTagName("initial").getLength() > 0) {
                            estadoInicial = idEstado;
                        }
                        if (eElement.getElementsByTagName("final").getLength() > 0) {
                            estadosAceptacion.add(idEstado);
                        }
                    }
                }

                // 2. Leer Transiciones
                NodeList nListTrans = doc.getElementsByTagName("transition");
                for (int i = 0; i < nListTrans.getLength(); i++) {
                    Node nNode = nListTrans.item(i);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        String from = eElement.getElementsByTagName("from").item(0).getTextContent();
                        String to = eElement.getElementsByTagName("to").item(0).getTextContent();
                        String read = eElement.getElementsByTagName("read").item(0).getTextContent();
                        
                        alfabeto.add(read);
                        transiciones.putIfAbsent(from, new HashMap<>());
                        transiciones.get(from).put(read, to);
                        
                        modeloTabla.addRow(new Object[]{"q" + from, read, "q" + to});
                    }
                }

                lblEstadoAutomata.setText("Autómata cargado: " + estados.size() + " estados, Alfabeto: " + alfabeto);
                imprimirConsola("Autómata cargado exitosamente desde: " + archivo.getName());

            } catch (Exception e) {
                imprimirConsola("Error al cargar el archivo: " + e.getMessage());
            }
        }
    }

    private void limpiarAutomata() {
        alfabeto.clear();
        estados.clear();
        estadosAceptacion.clear();
        transiciones.clear();
        estadoInicial = null;
        modeloTabla.setRowCount(0);
        txtConsola.setText("");
    }

    // --- LÓGICA DE VALIDACIÓN ---
    private void validarCadena() {
        if (estadoInicial == null) {
            imprimirConsola("Error: No hay autómata cargado.");
            return;
        }

        String cadena = txtCadena.getText();
        String estadoActual = estadoInicial;
        StringBuilder traza = new StringBuilder("Traza: q" + estadoActual);

        imprimirConsola("--- Iniciando validación para la cadena: '" + cadena + "' ---");

        for (char c : cadena.toCharArray()) {
            String simbolo = String.valueOf(c);
            
            if (!alfabeto.contains(simbolo)) {
                imprimirConsola(traza.toString() + " -> [RECHAZADA: Símbolo '" + simbolo + "' no está en el alfabeto]");
                return;
            }

            Map<String, String> transEstado = transiciones.get(estadoActual);
            if (transEstado == null || !transEstado.containsKey(simbolo)) {
                imprimirConsola(traza.toString() + " -> [RECHAZADA: No hay transición para '" + simbolo + "' desde q" + estadoActual + "]");
                return;
            }

            estadoActual = transEstado.get(simbolo);
            traza.append(" --(").append(simbolo).append(")--> q").append(estadoActual);
        }

        boolean aceptada = estadosAceptacion.contains(estadoActual);
        traza.append(aceptada ? " [ACEPTADA]" : " [RECHAZADA: Estado q" + estadoActual + " no es de aceptación]");
        imprimirConsola(traza.toString());
    }

    // --- FUNCIONALIDADES ADICIONALES ---
    private void calcularSubcadenas() {
        String cadena = txtCadena.getText();
        if (cadena.isEmpty()) {
            imprimirConsola("Por favor ingrese una cadena.");
            return;
        }

        Set<String> prefijos = new HashSet<>();
        Set<String> sufijos = new HashSet<>();
        Set<String> subcadenas = new HashSet<>();

        for (int i = 0; i <= cadena.length(); i++) {
            prefijos.add(cadena.substring(0, i));
            sufijos.add(cadena.substring(i));
            for (int j = i + 1; j <= cadena.length(); j++) {
                subcadenas.add(cadena.substring(i, j));
            }
        }

        imprimirConsola("--- Análisis de Cadena: " + cadena + " ---");
        imprimirConsola("Prefijos: " + prefijos);
        imprimirConsola("Sufijos: " + sufijos);
        imprimirConsola("Subcadenas: " + subcadenas);
    }

    private void calcularCerraduras() {
        if (alfabeto.isEmpty()) {
            imprimirConsola("Error: Cargue un autómata para obtener el alfabeto primero.");
            return;
        }

        int n = 3; // Longitud máxima para no saturar la memoria
        List<String> kleene = generarCombinaciones(new ArrayList<>(alfabeto), n, true);
        List<String> positiva = generarCombinaciones(new ArrayList<>(alfabeto), n, false);

        imprimirConsola("--- Cerraduras (Longitud máx " + n + ") ---");
        imprimirConsola("Alfabeto: " + alfabeto);
        imprimirConsola("Cerradura de Kleene (Sigma*): " + kleene);
        imprimirConsola("Cerradura Positiva (Sigma+): " + positiva);
    }

    private List<String> generarCombinaciones(List<String> alf, int maxLength, boolean includeEpsilon) {
        List<String> resultado = new ArrayList<>();
        if (includeEpsilon) resultado.add("λ (Épsilon)");
        
        Queue<String> cola = new LinkedList<>(alf);
        resultado.addAll(alf);

        while (!cola.isEmpty()) {
            String actual = cola.poll();
            if (actual.length() < maxLength) {
                for (String simbolo : alf) {
                    String nuevo = actual + simbolo;
                    resultado.add(nuevo);
                    cola.add(nuevo);
                }
            }
        }
        return resultado;
    }

    private void imprimirConsola(String mensaje) {
        txtConsola.append(mensaje + "\n");
        txtConsola.setCaretPosition(txtConsola.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SimuladorAFD().setVisible(true);
        });
    }
}
