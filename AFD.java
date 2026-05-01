package TeriaComputacion.modelo;

import java.io.Serializable;
import java.util.*;

public class AFD implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<State> states;
    private List<Transition> transitions;
    private Set<String> alphabet;
    private String name;

    public AFD() {
        states = new ArrayList<>();
        transitions = new ArrayList<>();
        alphabet = new LinkedHashSet<>();
        name = "AFD sin nombre";
    }

    public AFD(String name) {
        this();
        this.name = name;
    }

    // -------- States --------
    public void addState(State s) { states.add(s); }

    public boolean removeState(State s) {
        transitions.removeIf(t -> t.getFrom().equals(s) || t.getTo().equals(s));
        return states.remove(s);
    }

    public State getStateById(String id) {
        return states.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }

    public State getStateByName(String name) {
        return states.stream().filter(s -> s.getName().equals(name)).findFirst().orElse(null);
    }

    public State getInitialState() {
        return states.stream().filter(State::isInitial).findFirst().orElse(null);
    }

    public List<State> getFinalStates() {
        List<State> finals = new ArrayList<>();
        for (State s : states) if (s.isFinal()) finals.add(s);
        return finals;
    }

    public List<State> getStates() { return Collections.unmodifiableList(states); }

    public void addTransition(Transition t) {
        transitions.add(t);
        alphabet.add(t.getSymbol());
    }

    public boolean removeTransition(Transition t) { return transitions.remove(t); }

    public List<Transition> getTransitions() { return Collections.unmodifiableList(transitions); }

    public List<Transition> getTransitionsFrom(State s) {
        List<Transition> res = new ArrayList<>();
        for (Transition t : transitions) if (t.getFrom().equals(s)) res.add(t);
        return res;
    }

    public List<Transition> getTransitionsTo(State s) {
        List<Transition> res = new ArrayList<>();
        for (Transition t : transitions) if (t.getTo().equals(s)) res.add(t);
        return res;
    }

    public Transition getTransition(State from, String symbol) {
        for (Transition t : transitions) {
            if (t.getFrom().equals(from) && t.getSymbol().equals(symbol)) return t;
        }
        return null;
    }

    public Set<String> getAlphabet() { return Collections.unmodifiableSet(alphabet); }

    public void rebuildAlphabet() {
        alphabet.clear();
        for (Transition t : transitions) alphabet.add(t.getSymbol());
    }

    public ValidationResult validate() {
        List<String> errors = new ArrayList<>();
        long initCount = states.stream().filter(State::isInitial).count();
        if (initCount == 0) errors.add("No hay estado inicial definido.");
        if (initCount > 1) errors.add("Hay más de un estado inicial.");
        if (getFinalStates().isEmpty()) errors.add("No hay estados de aceptación definidos.");
        for (State s : states) {
            Map<String, Integer> symbolCount = new HashMap<>();
            for (Transition t : getTransitionsFrom(s)) {
                if (t.getSymbol().isEmpty() || t.getSymbol().equals("ε") || t.getSymbol().equals("eps")) {
                    errors.add("Transición épsilon detectada desde el estado '" + s.getName() + "'. Los AFD no permiten transiciones épsilon.");
                }
                symbolCount.merge(t.getSymbol(), 1, Integer::sum);
            }
            for (Map.Entry<String, Integer> e : symbolCount.entrySet()) {
                if (e.getValue() > 1) {
                    errors.add("El estado '" + s.getName() + "' tiene " + e.getValue() +
                            " transiciones con el símbolo '" + e.getKey() + "'. Un AFD permite solo una.");
                }
            }
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public AFD deepCopy() {
        Map<String, State> stateMap = new HashMap<>();
        AFD copy = new AFD(this.name);
        for (State s : states) {
            State ns = new State(s.getId(), s.getName(), s.isInitial(), s.isFinal(),
                    new java.awt.Point(s.getPosition()));
            copy.addState(ns);
            stateMap.put(s.getId(), ns);
        }
        for (Transition t : transitions) {
            copy.transitions.add(new Transition(
                    stateMap.get(t.getFrom().getId()),
                    stateMap.get(t.getTo().getId()),
                    t.getSymbol()));
        }
        copy.alphabet.addAll(this.alphabet);
        return copy;
    }

    @Override
    public String toString() {
        return "AFD{states=" + states.size() + ", transitions=" + transitions.size() + "}";
    }
    public static class ValidationResult {
        public final boolean valid;
        public final List<String> errors;

        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }
    }
}
