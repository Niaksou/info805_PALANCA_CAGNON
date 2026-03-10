package fr.usmb.m1isc.compilation.tp.ast;

/**
 * Représente une instruction de sortie "output expr".
 */
public class Output extends Node {

    /** Expression dont la valeur est affichée. */
    private final Node expression;

    /**
     * Construit une instruction output.
     *
     * @param expression expression à afficher
     */
    public Output(Node expression) {
        this.expression = expression;
    }

    /**
     * Retourne l'expression associée à output.
     *
     * @return nœud expression
     */
    public Node getExpression() {
        return expression;
    }

    @Override
    public String toPrefix() {
        return "(OUTPUT " + expression.toPrefix() + ")";
    }
}
