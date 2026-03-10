package fr.usmb.m1isc.compilation.tp.ast;

/**
 * Représente une opération binaire dans l'arbre abstrait.
 * Exemple : a + b, x * y, a / b, a mod b.
 */
public class BinaryOp extends Node {

    /** Symbole de l'opérateur : "+", "-", "*", "/", "mod", etc. */
    private final String operator;
    /** Sous-expression gauche. */
    private final Node left;
    /** Sous-expression droite. */
    private final Node right;

    /**
     * Construit une opération binaire.
     *
     * @param operator symbole de l'opérateur ("+", "-", "*", "/", "mod", ...)
     * @param left     sous-expression gauche
     * @param right    sous-expression droite
     */
    public BinaryOp(String operator, Node left, Node right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    /**
     * Retourne le symbole de l'opérateur.
     *
     * @return opérateur sous forme de chaîne
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Retourne la sous-expression gauche.
     *
     * @return nœud gauche
     */
    public Node getLeft() {
        return left;
    }

    /**
     * Retourne la sous-expression droite.
     *
     * @return nœud droit
     */
    public Node getRight() {
        return right;
    }

    @Override
    public String toPrefix() {
        return "(" + operator + " " + left.toPrefix() + " " + right.toPrefix() + ")";
    }
}
