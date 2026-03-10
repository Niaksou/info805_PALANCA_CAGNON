package fr.usmb.m1isc.compilation.tp.ast;

/**
 * Représente une séquence d'expressions séparées par des ';'.
 * Exemple : e1 ; e2.
 */
public class Sequence extends Node {

    /** Première expression de la séquence. */
    private final Node left;
    /** Seconde expression de la séquence. */
    private final Node right;

    /**
     * Construit une séquence "left ; right".
     *
     * @param left  première expression
     * @param right seconde expression
     */
    public Sequence(Node left, Node right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Retourne la première expression de la séquence.
     *
     * @return nœud gauche
     */
    public Node getLeft() {
        return left;
    }

    /**
     * Retourne la seconde expression de la séquence.
     *
     * @return nœud droit
     */
    public Node getRight() {
        return right;
    }

    @Override
    public String toPrefix() {
        return "(; " + left.toPrefix() + " " + right.toPrefix() + ")";
    }
}
