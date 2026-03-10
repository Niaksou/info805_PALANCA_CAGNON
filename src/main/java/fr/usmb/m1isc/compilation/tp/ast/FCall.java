package fr.usmb.m1isc.compilation.tp.ast;

import java.util.List;
import java.util.stream.Collectors;

public class FCall extends Node {

    private final String     name;
    private final List<Node> args;

    public FCall(String name, List<Node> args) {
        this.name = name;
        this.args = args;
    }

    public String     getName() { return name; }
    public List<Node> getArgs() { return args; }

    @Override
    public String toPrefix() {
        String argsStr = args.stream()
                             .map(Node::toPrefix)
                             .collect(Collectors.joining(", "));
        return "FCALL(" + name + ", " + argsStr + ")";
    }
}
