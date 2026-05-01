package TeriaComputacion.algoritmo;

import TeriaComputacion.modelo.AFD;

public class EliminationStep {
    private final int stepNumber;
    private final String description;
    private final String eliminatedState;   
    private final AFD snapshotAFD;          
    private final String currentExpression; 

    public EliminationStep(int stepNumber, String description, String eliminatedState,
                           AFD snapshotAFD, String currentExpression) {
        this.stepNumber = stepNumber;
        this.description = description;
        this.eliminatedState = eliminatedState;
        this.snapshotAFD = snapshotAFD;
        this.currentExpression = currentExpression;
    }

    public int getStepNumber() { return stepNumber; }
    public String getDescription() { return description; }
    public String getEliminatedState() { return eliminatedState; }
    public AFD getSnapshotAFD() { return snapshotAFD; }
    public String getCurrentExpression() { return currentExpression; }

    @Override
    public String toString() {
        return "Paso " + stepNumber + ": " + description;
    }
}
