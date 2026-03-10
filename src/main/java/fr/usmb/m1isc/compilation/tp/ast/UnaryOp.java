package fr.usmb.m1isc.compilation.tp.ast;

/**
 * Représente une opération unaire dans l'arbre abstrait.
 * Exemple : -x, not x.
 */
public class UnaryOp extends Node {

    /** Symbole de l'opérateur unaire : "-", "NOT", etc. */
    private final String operator;
    /** Sous-expression sur laquelle s'applique l'opérateur. */
    private final Node operand;

    /**
     * Construit une opération unaire.
     *
     * @param operator symbole de l'opérateur ("-", "NOT", ...)
     * @param operand  sous-expression opérande
     */
    public UnaryOp(String operator, Node operand) {
        this.operator = operator;
        this.operand = operand;
    }

    /**
     * Retourne le symbole de l'opérateur unaire.
     *
     * @return opérateur sous forme de chaîne
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Retourne l'opérande de cette opération unaire.
     *
     * @return nœud opérande
     */
    public Node getOperand() {
        return operand;
    }

    @Override
    public String toPrefix() {
        return "(" + operator + " " + operand.toPrefix() + ")";
    }
}
