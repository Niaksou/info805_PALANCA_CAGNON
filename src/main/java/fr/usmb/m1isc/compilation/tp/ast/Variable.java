package fr.usmb.m1isc.compilation.tp.ast;

/**
 * Représente une variable (identificateur) dans l'arbre abstrait.
 */
public class Variable extends Node {

    /** Nom de la variable. */
    private final String name;

    /**
     * Construit une variable avec son nom.
     *
     * @param name nom de la variable
     */
    public Variable(String name) {
        this.name = name;
    }

    /**
     * Retourne le nom de cette variable.
     *
     * @return nom de la variable
     */
    public String getName() {
        return name;
    }

    @Override
    public String toPrefix() {
        return name;
    }
}
