package TeriaComputacion.interfaz;

import TeriaComputacion.algoritmo.EliminationStep;
import TeriaComputacion.algoritmo.StateEliminationAlgorithm;
import TeriaComputacion.modelo.AFD;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class EliminationPanel extends JPanel {

    private static final Color BG      = new Color(14, 16, 26);
    private static final Color PANEL_BG= new Color(22, 26, 42);
    private static final Color ACCENT  = new Color(100, 200, 160);
    private static final Color TEXT    = new Color(200, 210, 230);
    private static final Color REGEX   = new Color(255, 200, 80);

    private StateEliminationAlgorithm algorithm;
    private List<EliminationStep> steps;
    private int currentStep = 0;

    private JLabel stepLabel;
    private JTextArea descArea;
    private AFDCanvas snapshotCanvas;
    private JLabel regexLabel;
    private JButton prevBtn, nextBtn;
    private JLabel resultLabel;

    public EliminationPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);

        stepLabel = new JLabel("Paso 0 / 0", SwingConstants.CENTER);
        stepLabel.setFont(new Font("Consolas", Font.BOLD, 15));
        stepLabel.setForeground(ACCENT);
        header.add(stepLabel, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        JSplitPane center = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        center.setDividerLocation(480);
        center.setResizeWeight(0.6);
        center.setBackground(BG);
        snapshotCanvas = new AFDCanvas();
        snapshotCanvas.setReadOnly(true);
        snapshotCanvas.setPreferredSize(new Dimension(480, 380));
        JPanel canvasWrap = new JPanel(new BorderLayout());
        canvasWrap.setBackground(PANEL_BG);
        canvasWrap.setBorder(titledBorder("GNFA - Estado actual"));
        canvasWrap.add(snapshotCanvas, BorderLayout.CENTER);
        center.setLeftComponent(canvasWrap);
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(PANEL_BG);
        infoPanel.setBorder(titledBorder("Descripción del paso"));

        descArea = new JTextArea(10, 20);
        descArea.setEditable(false);
        descArea.setBackground(new Color(18, 22, 35));
        descArea.setForeground(TEXT);
        descArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(BorderFactory.createEmptyBorder());

        JLabel regexTitle = new JLabel("Expresión Regular parcial:");
        regexTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        regexTitle.setForeground(TEXT);
        regexTitle.setBorder(BorderFactory.createEmptyBorder(10, 8, 4, 8));
        regexTitle.setAlignmentX(LEFT_ALIGNMENT);

        regexLabel = new JLabel("—");
        regexLabel.setFont(new Font("Consolas", Font.BOLD, 14));
        regexLabel.setForeground(REGEX);
        regexLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 70, 30), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        regexLabel.setBackground(new Color(40, 35, 15));
        regexLabel.setOpaque(true);
        regexLabel.setAlignmentX(LEFT_ALIGNMENT);

        infoPanel.add(descScroll);
        infoPanel.add(regexTitle);
        infoPanel.add(regexLabel);
        center.setRightComponent(infoPanel);

        add(center, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(10, 10));
        footer.setBackground(BG);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        navPanel.setBackground(BG);

        prevBtn = createNavBtn("◀ Anterior");
        prevBtn.addActionListener(e -> navigate(-1));

        nextBtn = createNavBtn("Siguiente ▶");
        nextBtn.addActionListener(e -> navigate(1));

        navPanel.add(prevBtn);
        navPanel.add(nextBtn);

        resultLabel = new JLabel("", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Consolas", Font.BOLD, 16));
        resultLabel.setForeground(REGEX);
        resultLabel.setBackground(new Color(40, 35, 15));
        resultLabel.setOpaque(true);
        resultLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(REGEX.darker(), 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));

        footer.add(navPanel, BorderLayout.NORTH);
        footer.add(resultLabel, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    public void runAlgorithm(AFD afd) {
        algorithm = new StateEliminationAlgorithm(afd);
        algorithm.run();
        steps = algorithm.getSteps();
        currentStep = 0;
        resultLabel.setText("");
        updateDisplay();
    }

    public String getResultRegex() {
        return algorithm != null ? algorithm.getResultRegex() : null;
    }

    private void navigate(int delta) {
        if (steps == null) return;
        currentStep = Math.max(0, Math.min(steps.size() - 1, currentStep + delta));
        updateDisplay();
    }

    private void updateDisplay() {
        if (steps == null || steps.isEmpty()) return;

        EliminationStep step = steps.get(currentStep);
        stepLabel.setText("Paso " + step.getStepNumber() + " / " + (steps.size() - 1));
        descArea.setText(step.getDescription());
        descArea.setCaretPosition(0);

        String partial = step.getCurrentExpression();
        regexLabel.setText(partial == null || partial.equals("—") ? "—" : partial);

        if (step.getSnapshotAFD() != null) {
            snapshotCanvas.setAFD(step.getSnapshotAFD());
            if (step.getEliminatedState() != null) {
                snapshotCanvas.setHighlightedState(step.getEliminatedState());
            } else {
                snapshotCanvas.clearHighlight();
            }
        }

        prevBtn.setEnabled(currentStep > 0);
        nextBtn.setEnabled(currentStep < steps.size() - 1);
        if (currentStep == steps.size() - 1 && algorithm != null) {
            String re = algorithm.getResultRegex();
            if (re != null) {
                resultLabel.setText("✓ Expresión Regular: " + re);
            }
        } else {
            resultLabel.setText("");
        }
    }

    private JButton createNavBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBackground(new Color(40, 50, 80));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT.darker(), 1),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(60, 80, 120));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(40, 50, 80));
            }
        });
        return btn;
    }

    private TitledBorder titledBorder(String title) {
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(50, 60, 90), 1), title);
        tb.setTitleColor(ACCENT);
        tb.setTitleFont(new Font("SansSerif", Font.BOLD, 11));
        return tb;
    }
}
