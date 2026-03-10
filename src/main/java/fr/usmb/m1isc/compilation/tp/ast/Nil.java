package fr.usmb.m1isc.compilation.tp.ast;

public class Nil extends Node {
    @Override
    public String toPrefix() {
        return "NIL";
    }
}
