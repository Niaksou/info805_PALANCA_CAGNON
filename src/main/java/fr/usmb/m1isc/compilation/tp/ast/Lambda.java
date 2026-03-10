package fr.usmb.m1isc.compilation.tp.ast;

import java.util.List;

public class Lambda extends Node {

    private final List<String> params;
    private final Node body;

    public Lambda(List<String> params, Node body) {
        this.params = params;
        this.body   = body;
    }

    public List<String> getParams() { return params; }
    public Node getBody()           { return body; }

    @Override
    public String toPrefix() {
        return "LAMBDA(" + String.join(", ", params) + ") " + body.toPrefix();
    }
}
