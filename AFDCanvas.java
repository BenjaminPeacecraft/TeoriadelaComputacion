package TeriaComputacion.interfaz;

import TeriaComputacion.modelo.AFD;
import TeriaComputacion.modelo.State;
import TeriaComputacion.modelo.Transition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class AFDCanvas extends JPanel {
    private static final int STATE_RADIUS   = 28;
    private static final Color COLOR_BG     = new Color(18, 20, 30);
    private static final Color COLOR_STATE  = new Color(45, 55, 90);
    private static final Color COLOR_INIT   = new Color(30, 140, 100);
    private static final Color COLOR_FINAL  = new Color(180, 80, 50);
    private static final Color COLOR_BOTH   = new Color(140, 80, 160);
    private static final Color COLOR_TRANS  = new Color(180, 190, 220);
    private static final Color COLOR_TEXT   = Color.WHITE;
    private static final Color COLOR_HIGHLIGHT = new Color(255, 220, 60);
    private static final Color COLOR_GRID   = new Color(30, 34, 50);

    private AFD afd;
    private State draggingState    = null;
    private State selectedState    = null;
    private State highlightedState = null;  
    private Point dragOffset = new Point();
    private boolean readOnly = false;
    private Consumer<String> statusCallback;

    private int stateCounter = 0;

    public AFDCanvas() {
        setBackground(COLOR_BG);
        setPreferredSize(new Dimension(700, 500));
        afd = new AFD("Nuevo AFD");
        setupListeners();
    }

    public void setAFD(AFD afd) {
        this.afd = afd;
        stateCounter = afd.getStates().size();
        repaint();
    }

    public AFD getAFD() { return afd; }

    public void setReadOnly(boolean readOnly) { this.readOnly = readOnly; }

    public void setStatusCallback(Consumer<String> cb) { this.statusCallback = cb; }

    public void setHighlightedState(String stateName) {
        if (stateName == null) { highlightedState = null; repaint(); return; }
        for (State s : afd.getStates()) {
            if (s.getName().equals(stateName)) { highlightedState = s; break; }
        }
        repaint();
    }

    public void clearHighlight() { highlightedState = null; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawGrid(g2);

        if (afd == null) return;
        Map<String, List<Transition>> transMap = new LinkedHashMap<>();
        for (Transition t : afd.getTransitions()) {
            String key = t.getFrom().getId() + "_" + t.getTo().getId();
            transMap.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }
        for (Map.Entry<String, List<Transition>> e : transMap.entrySet()) {
            drawTransitions(g2, e.getValue(), transMap);
        }
        for (State s : afd.getStates()) {
            drawState(g2, s);
        }
        if (selectedState != null) {
            Point p = selectedState.getPosition();
            g2.setColor(COLOR_HIGHLIGHT);
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1f, new float[]{6, 3}, 0));
            g2.drawOval(p.x - STATE_RADIUS - 4, p.y - STATE_RADIUS - 4,
                    2 * (STATE_RADIUS + 4), 2 * (STATE_RADIUS + 4));
            g2.setStroke(new BasicStroke(1f));
        }
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(COLOR_GRID);
        g2.setStroke(new BasicStroke(0.5f));
        for (int x = 0; x < getWidth(); x += 40) g2.drawLine(x, 0, x, getHeight());
        for (int y = 0; y < getHeight(); y += 40) g2.drawLine(0, y, getWidth(), y);
    }

    private void drawState(Graphics2D g2, State s) {
        Point p = s.getPosition();
        int r = STATE_RADIUS;
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillOval(p.x - r + 3, p.y - r + 3, 2 * r, 2 * r);
        Color fill;
        if (s.equals(highlightedState))    fill = new Color(220, 180, 40);
        else if (s.isInitial() && s.isFinal()) fill = COLOR_BOTH;
        else if (s.isInitial())             fill = COLOR_INIT;
        else if (s.isFinal())               fill = COLOR_FINAL;
        else                                fill = COLOR_STATE;
        GradientPaint gp = new GradientPaint(
                p.x - r, p.y - r, fill.brighter(),
                p.x + r, p.y + r, fill.darker());
        g2.setPaint(gp);
        g2.fillOval(p.x - r, p.y - r, 2 * r, 2 * r);
        g2.setColor(fill.brighter().brighter());
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(p.x - r, p.y - r, 2 * r, 2 * r);
        if (s.isFinal()) {
            g2.setColor(new Color(255, 255, 255, 120));
            g2.setStroke(new BasicStroke(1.5f));
            int ri = r - 5;
            g2.drawOval(p.x - ri, p.y - ri, 2 * ri, 2 * ri);
        }
        if (s.isInitial()) {
            g2.setColor(COLOR_INIT.brighter());
            g2.setStroke(new BasicStroke(2f));
            int ax = p.x - r - 25;
            g2.drawLine(ax, p.y, p.x - r, p.y);
            drawArrowHead(g2, ax + 10, p.y, p.x - r, p.y, COLOR_INIT.brighter());
        }
        g2.setColor(COLOR_TEXT);
        g2.setFont(new Font("Consolas", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        String label = s.getName();
        g2.drawString(label, p.x - fm.stringWidth(label) / 2, p.y + fm.getAscent() / 2 - 2);

        g2.setStroke(new BasicStroke(1f));
    }

    private void drawTransitions(Graphics2D g2, List<Transition> group,
                                  Map<String, List<Transition>> allTrans) {
        Transition t = group.get(0);
        State from = t.getFrom();
        State to   = t.getTo();
        StringBuilder labelBuilder = new StringBuilder();
        for (int i = 0; i < group.size(); i++) {
            if (i > 0) labelBuilder.append(", ");
            labelBuilder.append(group.get(i).getSymbol());
        }
        String label = labelBuilder.toString();

        Point pf = from.getPosition();
        Point pt  = to.getPosition();

        g2.setColor(COLOR_TRANS);
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        if (from.equals(to)) {
            int loopR = 18;
            int lcx = pf.x;
            int lcy = pf.y - STATE_RADIUS - loopR;
            g2.drawOval(lcx - loopR, lcy - loopR, loopR * 2, loopR * 2);
            drawArrow(g2, new Point(lcx + loopR - 3, lcy + 5), Math.PI / 2 + 0.5);
            drawLabel(g2, label, lcx, lcy - loopR - 6);
        } else {
             String reverseKey = to.getId() + "_" + from.getId();
            boolean hasReverse = allTrans.containsKey(reverseKey);

            if (hasReverse) {
                double dx = pt.x - pf.x, dy = pt.y - pf.y;
                double len = Math.sqrt(dx * dx + dy * dy);
                if (len == 0) return;
                double ux = dx / len, uy = dy / len;
                double nx = -uy,      ny = ux;
                double offset = 40;
                double cx = (pf.x + pt.x) / 2.0 + nx * offset;
                double cy = (pf.y + pt.y) / 2.0 + ny * offset;
                double sfx = cx - pf.x, sfy = cy - pf.y;
                double sfLen = Math.sqrt(sfx * sfx + sfy * sfy);
                int startX = (int) (pf.x + sfx / sfLen * STATE_RADIUS);
                int startY = (int) (pf.y + sfy / sfLen * STATE_RADIUS);
                double etx = cx - pt.x, ety = cy - pt.y;
                double etLen = Math.sqrt(etx * etx + ety * ety);
                int endX = (int) (pt.x + etx / etLen * STATE_RADIUS);
                int endY = (int) (pt.y + ety / etLen * STATE_RADIUS);

                QuadCurve2D curve = new QuadCurve2D.Float(startX, startY, (float)cx, (float)cy, endX, endY);
                g2.draw(curve);
                double angle = Math.atan2(endY - cy, endX - cx);
                drawArrow(g2, new Point(endX, endY), angle);
                int midX = (int) (0.25 * startX + 0.5 * cx + 0.25 * endX);
                int midY = (int) (0.25 * startY + 0.5 * cy + 0.25 * endY);
                drawLabel(g2, label, midX, midY - 6);
            } else {
                double dx = pt.x - pf.x, dy = pt.y - pf.y;
                double len = Math.sqrt(dx * dx + dy * dy);
                if (len == 0) return;
                double ux = dx / len, uy = dy / len;

                int sx = (int) (pf.x + ux * STATE_RADIUS);
                int sy = (int) (pf.y + uy * STATE_RADIUS);
                int ex = (int) (pt.x - ux * STATE_RADIUS);
                int ey = (int) (pt.y - uy * STATE_RADIUS);

                g2.drawLine(sx, sy, ex, ey);
                drawArrow(g2, new Point(ex, ey), Math.atan2(ey - sy, ex - sx));
                double labelAngle = Math.atan2(ey - sy, ex - sx);
                int mlx = (sx + ex) / 2 + (int)(-Math.sin(labelAngle) * 14);
                int mly = (sy + ey) / 2 + (int)( Math.cos(labelAngle) * 14);
                drawLabel(g2, label, mlx, mly);
            }
        }

        g2.setStroke(new BasicStroke(1f));
    }

    private void drawArrow(Graphics2D g2, Point tip, double angle) {
        int len = 10;
        double spread = Math.PI / 7;
        int x1 = (int) (tip.x - len * Math.cos(angle - spread));
        int y1 = (int) (tip.y - len * Math.sin(angle - spread));
        int x2 = (int) (tip.x - len * Math.cos(angle + spread));
        int y2 = (int) (tip.y - len * Math.sin(angle + spread));
        g2.fillPolygon(new int[]{tip.x, x1, x2}, new int[]{tip.y, y1, y2}, 3);
    }

    private void drawArrowHead(Graphics2D g2, int x1, int y1, int x2, int y2, Color color) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        g2.setColor(color);
        drawArrow(g2, new Point(x2, y2), angle);
    }

    private void drawLabel(Graphics2D g2, String label, int x, int y) {
        g2.setFont(new Font("Consolas", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();
        int w = fm.stringWidth(label) + 6, h = fm.getHeight() + 2;
        g2.setColor(new Color(15, 18, 30, 190));
        g2.fillRoundRect(x - w / 2, y - h + 3, w, h, 4, 4);
        g2.setColor(new Color(255, 220, 120));
        g2.drawString(label, x - fm.stringWidth(label) / 2, y);
    }

      private void setupListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (readOnly) return;
                State clicked = stateAt(e.getPoint());

                if (SwingUtilities.isRightMouseButton(e)) {
                    if (clicked != null) showStateMenu(clicked, e.getPoint());
                    return;
                }

                if (e.isControlDown()) {
                      if (clicked != null) {
                        if (selectedState == null) {
                            selectedState = clicked;
                            status("Estado '" + clicked.getName() + "' seleccionado. Ctrl+click en otro estado para crear transición.");
                        } else if (!selectedState.equals(clicked)) {
                            promptAddTransition(selectedState, clicked);
                            selectedState = null;
                        } else {
                            promptAddTransition(clicked, clicked);
                            selectedState = null;
                        }
                        repaint();
                    } else {
                        selectedState = null;
                        repaint();
                    }
                    return;
                }

                if (clicked != null) {
                    draggingState = clicked;
                    dragOffset.x = e.getX() - clicked.getPosition().x;
                    dragOffset.y = e.getY() - clicked.getPosition().y;
                } else {
                    selectedState = null;
                    addStateAt(e.getPoint());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggingState = null;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (readOnly || draggingState == null) return;
                draggingState.setPosition(new Point(
                        Math.max(STATE_RADIUS, Math.min(getWidth() - STATE_RADIUS, e.getX() - dragOffset.x)),
                        Math.max(STATE_RADIUS, Math.min(getHeight() - STATE_RADIUS, e.getY() - dragOffset.y))
                ));
                repaint();
            }
        });
    }

    private State stateAt(Point p) {
        for (State s : afd.getStates()) {
            Point sp = s.getPosition();
            if (p.distance(sp) <= STATE_RADIUS) return s;
        }
        return null;
    }

    private void addStateAt(Point p) {
        String id   = "s" + stateCounter;
        String name = "q" + stateCounter;
        stateCounter++;
        boolean isInit = afd.getInitialState() == null;
        State s = new State(id, name, isInit, false, new Point(p));
        afd.addState(s);
        status("Estado '" + name + "' agregado" + (isInit ? " (estado inicial)" : "") + ". Doble-click derecho para editar.");
        repaint();
    }

    private void promptAddTransition(State from, State to) {
        String sym = JOptionPane.showInputDialog(this,
                "Símbolo de transición de '" + from.getName() + "' a '" + to.getName() + "':",
                "Nueva Transición", JOptionPane.PLAIN_MESSAGE);
        if (sym != null && !sym.trim().isEmpty()) {
            afd.addTransition(new Transition(from, to, sym.trim()));
            status("Transición '" + from.getName() + "' -[" + sym.trim() + "]-> '" + to.getName() + "' agregada.");
            repaint();
        }
    }

    private void showStateMenu(State s, Point p) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem rename = new JMenuItem("✏ Renombrar");
        rename.addActionListener(e -> {
            String newName = JOptionPane.showInputDialog(this, "Nuevo nombre:", s.getName());
            if (newName != null && !newName.trim().isEmpty()) {
                s.setName(newName.trim());
                repaint();
            }
        });

        JCheckBoxMenuItem setInit = new JCheckBoxMenuItem("▶ Estado inicial", s.isInitial());
        setInit.addActionListener(e -> {
            if (!s.isInitial()) {
                for (State st : afd.getStates()) st.setInitial(false);
                s.setInitial(true);
            } else {
                s.setInitial(false);
            }
            repaint();
        });

        JCheckBoxMenuItem setFinal = new JCheckBoxMenuItem("✓ Estado de aceptación", s.isFinal());
        setFinal.addActionListener(e -> {
            s.setFinal(!s.isFinal());
            repaint();
        });

        JMenuItem addSelfLoop = new JMenuItem("↺ Agregar auto-bucle");
        addSelfLoop.addActionListener(e -> promptAddTransition(s, s));

        JMenuItem delete = new JMenuItem("🗑 Eliminar estado");
        delete.setForeground(new Color(220, 80, 80));
        delete.addActionListener(e -> {
            int res = JOptionPane.showConfirmDialog(this,
                    "¿Eliminar el estado '" + s.getName() + "' y todas sus transiciones?",
                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                afd.removeState(s);
                repaint();
            }
        });

        menu.add(rename);
        menu.addSeparator();
        menu.add(setInit);
        menu.add(setFinal);
        menu.addSeparator();
        menu.add(addSelfLoop);
        menu.addSeparator();
        menu.add(delete);
        menu.show(this, p.x, p.y);
    }

    private void status(String msg) {
        if (statusCallback != null) statusCallback.accept(msg);
    }
}
