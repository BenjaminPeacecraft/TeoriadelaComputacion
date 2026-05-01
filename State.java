package TeriaComputacion.modelo;

import java.awt.Point;
import java.io.Serializable;

public class State implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private boolean isInitial;
    private boolean isFinal;
    private Point position;

    public State(String id, String name, boolean isInitial, boolean isFinal) {
        this.id = id;
        this.name = name;
        this.isInitial = isInitial;
        this.isFinal = isFinal;
        this.position = new Point(100, 100);
    }

    public State(String id, String name, boolean isInitial, boolean isFinal, Point position) {
        this(id, name, isInitial, isFinal);
        this.position = position;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isInitial() { return isInitial; }
    public void setInitial(boolean initial) { this.isInitial = initial; }
    public boolean isFinal() { return isFinal; }
    public void setFinal(boolean aFinal) { this.isFinal = aFinal; }
    public Point getPosition() { return position; }
    public void setPosition(Point position) { this.position = position; }

    @Override
    public String toString() { return name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;
        State s = (State) o;
        return id.equals(s.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
