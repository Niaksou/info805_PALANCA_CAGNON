package fr.usmb.m1isc.compilation.tp.ast;

/**
 * Représente une constante entière dans l'arbre abstrait.
 * C'est une feuille de l'arbre : elle n'a pas de sous-nœuds.
 */
public class Constant extends Node {

    /** Valeur entière de la constante. */
    private final int value;

    /**
     * Construit une constante entière.
     *
     * @param value valeur de la constante
     */
    public Constant(int value) {
        this.value = value;
    }

    /**
     * Retourne la valeur entière de cette constante.
     *
     * @return valeur entière
     */
    public int getValue() {
        return value;
    }

    @Override
    public String toPrefix() {
        return String.valueOf(value);
    }
}
