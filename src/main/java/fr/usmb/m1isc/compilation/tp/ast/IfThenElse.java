package fr.usmb.m1isc.compilation.tp.ast;

/**
 * Représente une conditionnelle "if condition then e1 else e2".
 */
public class IfThenElse extends Node {

    /** Condition de la branche. */
    private final Node condition;
    /** Expression évaluée si la condition est vraie. */
    private final Node thenBranch;
    /** Expression évaluée si la condition est fausse. */
    private final Node elseBranch;

    /**
     * Construit une conditionnelle if/then/else.
     *
     * @param condition  condition
     * @param thenBranch branche then
     * @param elseBranch branche else
     */
    public IfThenElse(Node condition, Node thenBranch, Node elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    /**
     * Retourne la condition.
     *
     * @return nœud condition
     */
    public Node getCondition() {
        return condition;
    }

    /**
     * Retourne la branche then.
     *
     * @return nœud then
     */
    public Node getThenBranch() {
        return thenBranch;
    }

    /**
     * Retourne la branche else.
     *
     * @return nœud else
     */
    public Node getElseBranch() {
        return elseBranch;
    }

    @Override
    public String toPrefix() {
        return "(IF " + condition.toPrefix() + " " + thenBranch.toPrefix() + " " + elseBranch.toPrefix() + ")";
    }
}
