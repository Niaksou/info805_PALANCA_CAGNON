package fr.usmb.m1isc.compilation.tp.ast;

/**
 * Représente une boucle "while condition do corps".
 */
public class While extends Node {

    /** Condition de la boucle. */
    private final Node condition;
    /** Corps de la boucle. */
    private final Node body;

    /**
     * Construit une boucle while.
     *
     * @param condition condition d'entrée dans la boucle
     * @param body      corps exécuté tant que la condition est vraie
     */
    public While(Node condition, Node body) {
        this.condition = condition;
        this.body = body;
    }

    /**
     * Retourne la condition de la boucle.
     *
     * @return nœud condition
     */
    public Node getCondition() {
        return condition;
    }

    /**
     * Retourne le corps de la boucle.
     *
     * @return nœud corps
     */
    public Node getBody() {
        return body;
    }

    @Override
    public String toPrefix() {
        return "(WHILE " + condition.toPrefix() + " " + body.toPrefix() + ")";
    }
}
