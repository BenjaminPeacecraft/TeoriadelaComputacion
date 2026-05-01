package TeriaComputacion.interfaz;

import TeriaComputacion.io.AFDFileManager;
import TeriaComputacion.io.JFLAPImporter;
import TeriaComputacion.modelo.AFD;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

public class MainWindow extends JFrame {
    private static final Color BG_DARK   = new Color(12, 14, 24);
    private static final Color BG_PANEL  = new Color(20, 24, 40);
    private static final Color ACCENT    = new Color(90, 180, 150);
    private static final Color ACCENT2   = new Color(200, 140, 60);
    private static final Color TEXT      = new Color(200, 210, 230);
    private static final Color STATUS_BG = new Color(18, 20, 34);

    private AFDCanvas afdCanvas;
    private EliminationPanel eliminationPanel;
    private JTabbedPane tabs;
    private JLabel statusBar;
    private AFD currentAFD;

    public MainWindow() {
        super("AFD → Expresión Regular  |  IPN-ESCOM Teoría de la Computación");
        currentAFD = new AFD("Nuevo AFD");
        initUI();
        applyLookAndFeel();
    }

    private void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    private void initUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildToolbar(), BorderLayout.WEST);
        tabs = new JTabbedPane();
        tabs.setBackground(BG_PANEL);
        tabs.setForeground(TEXT);
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        afdCanvas = new AFDCanvas();
        afdCanvas.setStatusCallback(this::setStatus);
        afdCanvas.setAFD(currentAFD);

        JPanel editorTab = buildEditorTab();
        tabs.addTab("📐 Editor de AFD", editorTab);

        eliminationPanel = new EliminationPanel();
        tabs.addTab("⚙ Conversión AFD→ER", eliminationPanel);

        tabs.addTab("ℹ Ayuda", buildHelpPanel());

        add(tabs, BorderLayout.CENTER);

        statusBar = new JLabel("  Listo. Haz click en el canvas para agregar estados.");
        statusBar.setFont(new Font("Consolas", Font.PLAIN, 12));
        statusBar.setForeground(new Color(140, 170, 160));
        statusBar.setBackground(STATUS_BG);
        statusBar.setOpaque(true);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(40, 50, 70)),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        add(statusBar, BorderLayout.SOUTH);

        setJMenuBar(buildMenuBar());
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(10, 12, 22));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT.darker()));

        JLabel title = new JLabel("  ⚡ AFD → Expresión Regular", SwingConstants.LEFT);
        title.setFont(new Font("Consolas", Font.BOLD, 20));
        title.setForeground(ACCENT);
        title.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel subtitle = new JLabel("Práctica 4 · Teoría de la Computación · IPN-ESCOM  ");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(120, 140, 160));
        subtitle.setHorizontalAlignment(SwingConstants.RIGHT);

        header.add(title, BorderLayout.WEST);
        header.add(subtitle, BorderLayout.EAST);
        return header;
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
        toolbar.setBackground(BG_PANEL);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(40, 50, 70)),
                BorderFactory.createEmptyBorder(10, 8, 10, 8)));

        toolbar.add(sectionLabel("ARCHIVO"));
        toolbar.add(toolBtn("📄 Nuevo AFD",     this::newAFD));
        toolbar.add(toolBtn("💾 Guardar AFD",   this::saveAFD));
        toolbar.add(toolBtn("📂 Abrir AFD",     this::openAFD));
        toolbar.add(toolBtn("📥 Importar JFLAP",this::importJFLAP));
        toolbar.add(Box.createVerticalStrut(16));

        toolbar.add(sectionLabel("AUTÓMATA"));
        toolbar.add(toolBtn("🔍 Validar AFD",    this::validateAFD));
        toolbar.add(toolBtn("🗑 Limpiar canvas", this::clearCanvas));
        toolbar.add(Box.createVerticalStrut(16));

        toolbar.add(sectionLabel("CONVERSIÓN"));
        toolbar.add(toolBtn("▶ Convertir AFD→ER", this::convertToER));

        toolbar.add(Box.createVerticalGlue());

        toolbar.add(buildLegend());

        return toolbar;
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(new Color(100, 110, 140));
        lbl.setBorder(BorderFactory.createEmptyBorder(4, 2, 2, 2));
        return lbl;
    }

    private JButton toolBtn(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setBackground(new Color(30, 36, 58));
        btn.setForeground(TEXT);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 55, 80), 1),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(180, 36));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.addActionListener(e -> action.run());
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(45, 58, 90)); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(new Color(30, 36, 58)); }
        });
        return btn;
    }

    private JPanel buildLegend() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(18, 22, 36));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(40, 50, 70), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        p.add(legendRow(new Color(30, 140, 100), "Estado inicial"));
        p.add(legendRow(new Color(180, 80, 50),  "Estado final"));
        p.add(legendRow(new Color(140, 80, 160), "Inicial y final"));
        p.add(legendRow(new Color(45, 55, 90),   "Estado normal"));

        JLabel hint = new JLabel("<html><small><font color='#6080a0'>" +
                "Click: nuevo estado<br>Ctrl+click: transición<br>Click derecho: opciones</font></small></html>");
        hint.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        p.add(hint);
        return p;
    }

    private JPanel legendRow(Color color, String label) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        row.setBackground(new Color(18, 22, 36));
        JLabel circle = new JLabel("●");
        circle.setForeground(color);
        circle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JLabel text = new JLabel(label);
        text.setFont(new Font("SansSerif", Font.PLAIN, 10));
        text.setForeground(new Color(150, 160, 180));
        row.add(circle);
        row.add(text);
        return row;
    }

    private JPanel buildEditorTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(BG_PANEL);
        rightPanel.setPreferredSize(new Dimension(220, 0));
        rightPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(40, 50, 70)));

        JLabel transTitle = new JLabel("  Transiciones", SwingConstants.LEFT);
        transTitle.setFont(new Font("Consolas", Font.BOLD, 13));
        transTitle.setForeground(ACCENT);
        transTitle.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));

        DefaultListModel<String> transModel = new DefaultListModel<>();
        JList<String> transList = new JList<>(transModel);
        transList.setBackground(new Color(18, 22, 36));
        transList.setForeground(TEXT);
        transList.setFont(new Font("Consolas", Font.PLAIN, 11));
        transList.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        JButton refreshBtn = toolBtn("↻ Actualizar lista", () -> refreshTransitionList(transModel));
        refreshBtn.setMaximumSize(new Dimension(220, 32));

        rightPanel.add(transTitle, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(transList), BorderLayout.CENTER);
        rightPanel.add(refreshBtn, BorderLayout.SOUTH);

        panel.add(afdCanvas, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private void refreshTransitionList(DefaultListModel<String> model) {
        model.clear();
        for (var t : currentAFD.getTransitions()) {
            model.addElement(t.getFrom().getName() + " →[" + t.getSymbol() + "]→ " + t.getTo().getName());
        }
    }

       private JPanel buildHelpPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_PANEL);

        JTextArea help = new JTextArea(HELP_TEXT);
        help.setEditable(false);
        help.setBackground(new Color(18, 22, 36));
        help.setForeground(TEXT);
        help.setFont(new Font("Consolas", Font.PLAIN, 13));
        help.setMargin(new Insets(16, 20, 16, 20));
        help.setLineWrap(true);
        help.setWrapStyleWord(true);

        p.add(new JScrollPane(help), BorderLayout.CENTER);
        return p;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.setBackground(BG_PANEL);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(40, 50, 70)));

        JMenu fileMenu = menu("Archivo");
        fileMenu.add(menuItem("Nuevo AFD",       KeyEvent.VK_N, this::newAFD));
        fileMenu.add(menuItem("Guardar AFD...",  KeyEvent.VK_S, this::saveAFD));
        fileMenu.add(menuItem("Abrir AFD...",    KeyEvent.VK_O, this::openAFD));
        fileMenu.addSeparator();
        fileMenu.add(menuItem("Importar JFLAP (.jff)...", -1, this::importJFLAP));
        fileMenu.addSeparator();
        fileMenu.add(menuItem("Salir", -1, () -> System.exit(0)));

        JMenu automataMenu = menu("Autómata");
        automataMenu.add(menuItem("Validar AFD",      -1, this::validateAFD));
        automataMenu.add(menuItem("Limpiar canvas",   -1, this::clearCanvas));

        JMenu convertMenu = menu("Conversión");
        convertMenu.add(menuItem("Convertir AFD → ER", KeyEvent.VK_R, this::convertToER));

        JMenu helpMenu = menu("Ayuda");
        helpMenu.add(menuItem("Acerca de...", -1, this::showAbout));

        bar.add(fileMenu);
        bar.add(automataMenu);
        bar.add(convertMenu);
        bar.add(helpMenu);
        return bar;
    }

    private JMenu menu(String text) {
        JMenu m = new JMenu(text);
        m.setForeground(TEXT);
        m.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return m;
    }

    private JMenuItem menuItem(String text, int acceleratorKey, Runnable action) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(new Font("SansSerif", Font.PLAIN, 13));
        if (acceleratorKey != -1) {
            item.setAccelerator(KeyStroke.getKeyStroke(acceleratorKey, InputEvent.CTRL_DOWN_MASK));
        }
        item.addActionListener(e -> action.run());
        return item;
    }

    private void newAFD() {
        int r = JOptionPane.showConfirmDialog(this,
                "¿Crear un nuevo AFD? Se perderán los cambios no guardados.",
                "Nuevo AFD", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            currentAFD = new AFD("Nuevo AFD");
            afdCanvas.setAFD(currentAFD);
            setStatus("Nuevo AFD creado.");
        }
    }

    private void saveAFD() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar AFD");
        fc.setFileFilter(new FileNameExtensionFilter("Archivo AFD (*.afd)", "afd"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (!f.getName().endsWith(".afd")) f = new File(f.getAbsolutePath() + ".afd");
            try {
                currentAFD = afdCanvas.getAFD();
                AFDFileManager.save(currentAFD, f);
                setStatus("AFD guardado en: " + f.getName());
            } catch (Exception ex) {
                error("Error al guardar: " + ex.getMessage());
            }
        }
    }

    private void openAFD() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Abrir AFD");
        fc.setFileFilter(new FileNameExtensionFilter("Archivo AFD (*.afd)", "afd"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                currentAFD = AFDFileManager.load(fc.getSelectedFile());
                afdCanvas.setAFD(currentAFD);
                setStatus("AFD cargado: " + currentAFD.getName());
                tabs.setSelectedIndex(0);
            } catch (Exception ex) {
                error("Error al cargar el archivo: " + ex.getMessage());
            }
        }
    }

    private void importJFLAP() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Importar archivo JFLAP");
        fc.setFileFilter(new FileNameExtensionFilter("Archivos JFLAP (*.jff)", "jff"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            JFLAPImporter importer = new JFLAPImporter();
            JFLAPImporter.ImportResult result = importer.importFile(fc.getSelectedFile());
            if (result.isSuccess()) {
                currentAFD = result.afd;
                afdCanvas.setAFD(currentAFD);
                tabs.setSelectedIndex(0);

                StringBuilder msg = new StringBuilder("AFD importado desde JFLAP: " + currentAFD.getName());
                if (!result.warnings.isEmpty()) {
                    msg.append("\n\nAdvertencias:\n");
                    for (String w : result.warnings) msg.append("• ").append(w).append("\n");
                    JOptionPane.showMessageDialog(this, msg.toString(), "Importación con advertencias",
                            JOptionPane.WARNING_MESSAGE);
                }
                setStatus("Importado desde JFLAP: " + currentAFD.getName() +
                        " (" + currentAFD.getStates().size() + " estados, " +
                        currentAFD.getTransitions().size() + " transiciones)");
            } else {
                error("Error al importar JFLAP:\n" + result.error);
            }
        }
    }

    private void validateAFD() {
        currentAFD = afdCanvas.getAFD();
        AFD.ValidationResult val = currentAFD.validate();
        if (val.valid) {
            JOptionPane.showMessageDialog(this,
                    "✅ El autómata es un AFD válido.\n\n" +
                    "Estados: " + currentAFD.getStates().size() + "\n" +
                    "Transiciones: " + currentAFD.getTransitions().size() + "\n" +
                    "Alfabeto: " + currentAFD.getAlphabet(),
                    "Validación exitosa", JOptionPane.INFORMATION_MESSAGE);
            setStatus("AFD válido ✓");
        } else {
            StringBuilder sb = new StringBuilder("❌ El autómata NO es un AFD válido:\n\n");
            for (String e : val.errors) sb.append("• ").append(e).append("\n");
            JOptionPane.showMessageDialog(this, sb.toString(), "Errores de validación",
                    JOptionPane.ERROR_MESSAGE);
            setStatus("AFD inválido — revisa los errores.");
        }
    }

    private void clearCanvas() {
        int r = JOptionPane.showConfirmDialog(this,
                "¿Limpiar el canvas? Se eliminarán todos los estados y transiciones.",
                "Limpiar canvas", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            currentAFD = new AFD("Nuevo AFD");
            afdCanvas.setAFD(currentAFD);
            setStatus("Canvas limpiado.");
        }
    }

    private void convertToER() {
        currentAFD = afdCanvas.getAFD();
        AFD.ValidationResult val = currentAFD.validate();
        if (!val.valid) {
            StringBuilder sb = new StringBuilder(
                    "El AFD tiene errores que deben corregirse antes de convertir:\n\n");
            for (String e : val.errors) sb.append("• ").append(e).append("\n");
            sb.append("\n¿Deseas continuar de todas formas? (puede producir resultados incorrectos)");
            int r = JOptionPane.showConfirmDialog(this, sb.toString(),
                    "AFD con errores", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (r != JOptionPane.YES_OPTION) return;
        }

        if (currentAFD.getStates().isEmpty()) {
            error("El AFD no tiene estados. Agrega estados y transiciones primero.");
            return;
        }

        setStatus("Ejecutando algoritmo de eliminación de estados...");
        eliminationPanel.runAlgorithm(currentAFD);
        tabs.setSelectedIndex(1);

        String re = eliminationPanel.getResultRegex();
        setStatus("Conversión completada. ER = " + re);
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
                "AFD → Expresión Regular\n" +
                "Práctica 4 - Teoría de la Computación\n" +
                "IPN - Escuela Superior de Cómputo\n\n" +
                "Implementa el algoritmo de eliminación de estados\n" +
                "para convertir AFD a Expresiones Regulares.\n\n" +
                "Características:\n" +
                "• Editor gráfico interactivo de AFD\n" +
                "• Importación de archivos JFLAP (.jff)\n" +
                "• Guardado/carga de AFD en formato .afd\n" +
                "• Conversión paso a paso AFD → ER\n" +
                "• Validación de AFD",
                "Acerca de", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setStatus(String msg) {
        statusBar.setText("  " + msg);
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static final String HELP_TEXT =
        "=== GUÍA DE USO - AFD → EXPRESIÓN REGULAR ===\n\n" +
        "CREAR UN AFD:\n" +
        "  • Haz click en el canvas vacío para agregar un estado\n" +
        "  • El primer estado agregado se marca automáticamente como inicial (verde)\n" +
        "  • Click derecho en un estado → menú de opciones:\n" +
        "      - Renombrar\n" +
        "      - Marcar como estado inicial\n" +
        "      - Marcar como estado de aceptación (doble círculo)\n" +
        "      - Agregar auto-bucle\n" +
        "      - Eliminar\n" +
        "  • Ctrl+click en un estado, luego Ctrl+click en otro → agrega transición\n" +
        "  • Arrastra estados para reorganizar el layout\n\n" +
        "IMPORTAR JFLAP:\n" +
        "  • Usa Archivo → Importar JFLAP (.jff) o el botón del panel izquierdo\n" +
        "  • El software verifica que sea un autómata finito (type=fa)\n" +
        "  • Muestra advertencias si detecta transiciones ε o no-determinismo\n\n" +
        "CONVERTIR AFD → ER:\n" +
        "  1. Crea o importa un AFD\n" +
        "  2. Opcionalmente valídalo con el botón 'Validar AFD'\n" +
        "  3. Haz click en '▶ Convertir AFD→ER'\n" +
        "  4. En la pestaña 'Conversión AFD→ER' usa los botones:\n" +
        "     ◀ Anterior / Siguiente ▶  para navegar paso a paso\n" +
        "  5. El último paso muestra la Expresión Regular resultante\n\n" +
        "ALGORITMO DE ELIMINACIÓN DE ESTADOS:\n" +
        "  1. Se crea un GNFA añadiendo estado inicial qS y estado final qA\n" +
        "  2. Se agregan transiciones ε de qS al estado inicial original\n" +
        "     y de cada estado de aceptación a qA\n" +
        "  3. Se elimina cada estado intermedio uno a uno:\n" +
        "     Para cada par (qi, qj):\n" +
        "       δ(qi,qj) = δ(qi,qj) ∪ δ(qi,qRip) · δ(qRip,qRip)* · δ(qRip,qj)\n" +
        "  4. La etiqueta final de qS→qA es la Expresión Regular\n\n" +
        "GUARDAR/CARGAR:\n" +
        "  • Guardar AFD: guarda en formato .afd (JSON)\n" +
        "  • Abrir AFD: carga un .afd previamente guardado\n\n" +
        "NOTACIÓN EN EXPRESIONES REGULARES:\n" +
        "  ε = cadena vacía\n" +
        "  ∅ = lenguaje vacío\n" +
        "  + = unión (alternativa)\n" +
        "  * = cerradura de Kleene\n" +
        "  Concatenación: implícita (AB = A seguido de B)\n";
}
