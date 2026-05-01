package TeriaComputacion.algoritmo;

import TeriaComputacion.modelo.AFD;
import TeriaComputacion.modelo.State;
import TeriaComputacion.modelo.Transition;

import java.awt.Point;
import java.util.*;

public class StateEliminationAlgorithm {

    private static final String EPSILON = "ε";
    private static final String EMPTY   = "∅";  

    private final AFD originalAFD;
    private List<EliminationStep> steps;
    private String resultRegex;

    public StateEliminationAlgorithm(AFD afd) {
        this.originalAFD = afd;
        this.steps = new ArrayList<>();
        this.resultRegex = null;
    }

    public List<EliminationStep> getSteps() { return Collections.unmodifiableList(steps); }
    public String getResultRegex() { return resultRegex; }

    public void run() {
        steps.clear();
        resultRegex = null;

        List<State> origStates = new ArrayList<>(originalAFD.getStates());

        String START = "__qS__";
        String ACCEPT = "__qA__";

        Set<String> gnfaStates = new LinkedHashSet<>();
        gnfaStates.add(START);
        for (State s : origStates) gnfaStates.add(s.getName());
        gnfaStates.add(ACCEPT);

        Map<String, Map<String, String>> gnfa = new LinkedHashMap<>();
        for (String q : gnfaStates) {
            gnfa.put(q, new LinkedHashMap<>());
            for (String r : gnfaStates) gnfa.get(q).put(r, EMPTY);
        }

        State init = originalAFD.getInitialState();
        if (init == null) return;
        gnfa.get(START).put(init.getName(), EPSILON);

        for (State f : originalAFD.getFinalStates()) {
            gnfa.get(f.getName()).put(ACCEPT, EPSILON);
        }

        for (Transition t : originalAFD.getTransitions()) {
            String from = t.getFrom().getName();
            String to   = t.getTo().getName();
            String sym  = t.getSymbol();
            String cur  = gnfa.get(from).get(to);
            gnfa.get(from).put(to, union(cur, sym));
        }

        steps.add(new EliminationStep(0,
                "GNFA inicial construido. Se agregaron estado de inicio '" + START +
                "' y estado de aceptación '" + ACCEPT + "' con transiciones ε.",
                null,
                gnfaToAFD(gnfa, gnfaStates, origStates),
                "—"));

        List<String> toEliminate = new ArrayList<>();
        for (State s : origStates) toEliminate.add(s.getName());

        int stepNum = 1;
        for (String qRip : toEliminate) {
            String loop = gnfa.get(qRip).get(qRip); 

            StringBuilder desc = new StringBuilder();
            desc.append("Eliminando estado '").append(qRip).append("'.");
            if (!loop.equals(EMPTY)) {
                desc.append(" Bucle en '").append(qRip).append("': ").append(loop).append("*.");
            }

            List<String> sources = new ArrayList<>(gnfaStates);
            List<String> targets = new ArrayList<>(gnfaStates);
            sources.remove(qRip);
            targets.remove(qRip);

            List<String> updatedPairs = new ArrayList<>();

            for (String qi : sources) {
                for (String qj : targets) {
                    String r1 = gnfa.get(qi).get(qRip); 
                    String r2 = gnfa.get(qRip).get(qj);  
                    String r3 = gnfa.get(qi).get(qj);   

                    String newLabel = union(r3, concat(concat(r1, star(loop)), r2));
                    gnfa.get(qi).put(qj, newLabel);

                    if (!r1.equals(EMPTY) && !r2.equals(EMPTY)) {
                        updatedPairs.add("  δ(" + qi + "," + qj + ") = " + newLabel);
                    }
                }
            }

            gnfa.remove(qRip);
            for (Map<String, String> row : gnfa.values()) row.remove(qRip);
            gnfaStates.remove(qRip);

            String partial = gnfa.getOrDefault(START, Collections.emptyMap())
                               .getOrDefault(ACCEPT, EMPTY);

            if (!updatedPairs.isEmpty()) {
                desc.append("\nTransiciones actualizadas:\n").append(String.join("\n", updatedPairs));
            }

            steps.add(new EliminationStep(stepNum++,
                    desc.toString(),
                    qRip,
                    gnfaToAFD(gnfa, gnfaStates, origStates),
                    partial));
        }

        resultRegex = gnfa.getOrDefault(START, Collections.emptyMap())
                          .getOrDefault(ACCEPT, EMPTY);

        if (resultRegex.equals(EMPTY)) resultRegex = EMPTY;

        steps.add(new EliminationStep(stepNum,
                "Todos los estados eliminados. La expresión regular resultante es: " + resultRegex,
                null,
                null,
                resultRegex));
    }

    static String union(String a, String b) {
        if (a.equals(EMPTY)) return b;
        if (b.equals(EMPTY)) return a;
        if (a.equals(b))     return a;
        return "(" + a + "+" + b + ")";
    }

    static String concat(String a, String b) {
        if (a.equals(EMPTY) || b.equals(EMPTY)) return EMPTY;
        if (a.equals(EPSILON)) return b;
        if (b.equals(EPSILON)) return a;
        String la = needsParens(a) ? "(" + a + ")" : a;
        String lb = needsParens(b) ? "(" + b + ")" : b;
        return la + lb;
    }

    static String star(String a) {
        if (a.equals(EMPTY))   return EPSILON;  
        if (a.equals(EPSILON)) return EPSILON;  
        if (a.length() == 1)   return a + "*";
        return "(" + a + ")*";
    }

    private static boolean needsParens(String r) {
        if (r.length() <= 1) return false;
        if (r.startsWith("(") && r.endsWith(")") && matchingClose(r, 0) == r.length() - 1) return false;
        return r.contains("+");
    }

    private static int matchingClose(String s, int open) {
        int depth = 0;
        for (int i = open; i < s.length(); i++) {
            if (s.charAt(i) == '(') depth++;
            else if (s.charAt(i) == ')') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private AFD gnfaToAFD(Map<String, Map<String, String>> gnfa,
                           Set<String> gnfaStates,
                           List<State> origStates) {
        AFD snap = new AFD("snapshot");
        Map<String, State> stateMap = new LinkedHashMap<>();
        String[] names = gnfaStates.toArray(new String[0]);
        int n = names.length;
        int cx = 350, cy = 250, r = 200;

        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n - Math.PI / 2;
            int x = cx + (int) (r * Math.cos(angle));
            int y = cy + (int) (r * Math.sin(angle));

            boolean isInit  = names[i].equals("__qS__");
            boolean isFinal = names[i].equals("__qA__");

            State orig = getOrigByName(origStates, names[i]);
            Point pos = (orig != null) ? orig.getPosition() : new Point(x, y);

            State s = new State(names[i], names[i], isInit, isFinal, pos);
            stateMap.put(names[i], s);
            snap.addState(s);
        }

        for (String from : gnfa.keySet()) {
            for (Map.Entry<String, String> entry : gnfa.get(from).entrySet()) {
                String to    = entry.getKey();
                String label = entry.getValue();
                if (!label.equals(EMPTY)) {
                    snap.addTransition(new Transition(stateMap.get(from), stateMap.get(to), label));
                }
            }
        }
        return snap;
    }

    private State getOrigByName(List<State> states, String name) {
        return states.stream().filter(s -> s.getName().equals(name)).findFirst().orElse(null);
    }
}
