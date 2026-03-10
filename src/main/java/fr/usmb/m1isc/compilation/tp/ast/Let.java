package fr.usmb.m1isc.compilation.tp.ast;

/**
 * Représente une affectation / déclaration de variable : let x = expr.
 */
public class Let extends Node {

    /** Nom de la variable déclarée. */
    private final String varName;
    /** Expression dont la valeur est affectée à la variable. */
    private final Node expression;

    /**
     * Construit un nœud d'affectation "let varName = expression".
     *
     * @param varName    nom de la variable
     * @param expression expression assignée à la variable
     */
    public Let(String varName, Node expression) {
        this.varName = varName;
        this.expression = expression;
    }

    /**
     * Retourne le nom de la variable déclarée.
     *
     * @return nom de variable
     */
    public String getVarName() {
        return varName;
    }

    /**
     * Retourne l'expression associée à la variable.
     *
     * @return nœud expression
     */
    public Node getExpression() {
        return expression;
    }

    @Override
    public String toPrefix() {
        return "(LET " + varName + " " + expression.toPrefix() + ")";
    }
}
