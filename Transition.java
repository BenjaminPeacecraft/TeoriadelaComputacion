package TeriaComputacion.modelo;

import java.io.Serializable;

public class Transition implements Serializable {
    private static final long serialVersionUID = 1L;

    private State from;
    private State to;
    private String symbol;

    public Transition(State from, State to, String symbol) {
        this.from = from;
        this.to = to;
        this.symbol = symbol;
    }

    public State getFrom() { return from; }
    public State getTo() { return to; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    @Override
    public String toString() {
        return from.getName() + " --[" + symbol + "]--> " + to.getName();
    }
}
