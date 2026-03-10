package fr.usmb.m1isc.compilation.tp.codegen;

import fr.usmb.m1isc.compilation.tp.ast.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Générateur de code assembleur pour la machine à registres.
 * Supporte l'ensemble du sous-langage λ-ada (exercices 1, 2 et 3)
 */
public class CodeGenerator {

    private final StringBuilder data = new StringBuilder();
    private final StringBuilder code = new StringBuilder();
    private final StringBuilder lambdas = new StringBuilder(); 
    private final Set<String> vars = new LinkedHashSet<>();
    private int labelCounter = 0;

    // ── État pour génération dans un corps de lambda ──────────────────────
    private boolean      inLambda     = false;   
    private List<String> lambdaParams = null;    

    /* écrit dans le bon buffer selon le contexte */
    private StringBuilder buf() { return inLambda ? lambdas : code; } 

    // =========================================================================
    // POINT D'ENTRÉE
    // =========================================================================

    /**
     * Point d'entrée du générateur.
     * 1) Parcourt l'AST pour collecter les variables globales (Let).
     * 2) Génère le segment DATA (déclarations DD).
     * 3) Génère le segment CODE (programme principal + corps de lambdas).
     * Retourne le programme assembleur complet sous forme de chaîne.
     */
    public String generate(Node ast) {
        collectVars(ast);
        generateDataSegment();
        generateCodeSegment(ast);
        return data.toString() + code.toString();
    }

    private int newLabel() {
        return labelCounter++;
    }

    // =========================================================================
    // COLLECTE DES VARIABLES
    // =========================================================================

    /**
     * Parcourt récursivement l'AST pour remplir l'ensemble {@code vars}
     * avec tous les noms de variables introduites par des nœuds Let.
     * Ne collecte pas les paramètres de lambdas (qui sont sur la pile).
     */
    private void collectVars(Node n) {
        if (n == null) return;

        if (n instanceof Let let) {
            vars.add(let.getVarName());
            collectVars(let.getExpression());
        } else if (n instanceof Sequence seq) {
            collectVars(seq.getLeft());
            collectVars(seq.getRight());
        } else if (n instanceof BinaryOp bin) {
            collectVars(bin.getLeft());
            collectVars(bin.getRight());
        } else if (n instanceof UnaryOp un) {
            collectVars(un.getOperand());
        } else if (n instanceof While w) {
            collectVars(w.getCondition());
            collectVars(w.getBody());
        } else if (n instanceof IfThenElse ite) {
            collectVars(ite.getCondition());
            collectVars(ite.getThenBranch());
            collectVars(ite.getElseBranch());
        } else if (n instanceof Output out) {
            collectVars(out.getExpression());
        } else if (n instanceof FCall fc) {          
            for (Node arg : fc.getArgs()) collectVars(arg);
        } else if (n instanceof Lambda lm) {         
            collectVars(lm.getBody());
        }
        // Constant, Variable, Input, Nil : pas de déclaration à collecter
    }

    // =========================================================================
    // SEGMENTS
    // =========================================================================

    /**
     * Génère le segment DATA en déclarant une case mémoire (DD) pour
     * chaque variable globale trouvée par {@link #collectVars(Node)}.
     * Exemple :
     *   DATA SEGMENT
     *       x DD
     *       y DD
     *   DATA ENDS
     */
    private void generateDataSegment() {
        data.append("DATA SEGMENT\n");
        for (String v : vars) {
            data.append("\t").append(v).append(" DD\n");
        }
        data.append("DATA ENDS\n");
    }

    /**
     * Génère le segment CODE :
     *   - visite l'AST avec {@link #gen(Node)} pour produire le code principal ;
     *   - insère un saut "jmp end_pg_X" pour contourner les lambdas ;
     *   - colle ensuite tous les corps de lambdas stockés dans {@code lambdas} ;
     *   - termine par l'étiquette "end_pg_X:" et "CODE ENDS".
     */
    private void generateCodeSegment(Node ast) {
        int endLabel = newLabel();                                        
        code.append("CODE SEGMENT\n");
        gen(ast);
        code.append("\tjmp end_pg_").append(endLabel).append("\n");       
        code.append(lambdas);                                             
        code.append("end_pg_").append(endLabel).append(":\n");            
        code.append("CODE ENDS\n");
    }

    // =========================================================================
    // DISPATCH
    // =========================================================================

    /**
     * Dispatch principal du générateur.
     * Selon le type concret du nœud (Constant, Variable, Let, Lambda, FCall, etc.),
     * délègue vers la méthode {@code genXXX} correspondante.
     * C'est le "visiteur" de l'AST côté code assembleur.
     */
    private void gen(Node n) {
        if      (n instanceof Constant   c) genConstant(c);
        else if (n instanceof Variable   v) genVariable(v);
        else if (n instanceof BinaryOp   b) genBinaryOp(b);
        else if (n instanceof UnaryOp    u) genUnaryOp(u);
        else if (n instanceof Let        l) genLet(l);
        else if (n instanceof Sequence   s) genSequence(s);
        else if (n instanceof IfThenElse i) genIfThenElse(i);
        else if (n instanceof While      w) genWhile(w);
        else if (n instanceof Input)        genInput();
        else if (n instanceof Output     o) genOutput(o);
        else if (n instanceof Nil)          genNil();
        else if (n instanceof Lambda    lm) genLambda(lm);  
        else if (n instanceof FCall     fc) genFCall(fc);    
        else {
            buf().append("\t; nœud non supporté : ")
                 .append(n.getClass().getSimpleName()).append("\n");
        }
    }

    // =========================================================================
    // EXPRESSIONS DE BASE
    // =========================================================================

    private void genConstant(Constant c) {
        buf().append("\tmov eax, ").append(c.getValue()).append("\n");
    }

    /**
     * Génère le chargement d'une variable dans eax.
     * - Dans le code principal : "mov eax, <nom>" (variable globale en DATA).
     * - Dans une lambda : si le nom correspond à un paramètre, génère un
     *   accès pile via un offset relatif à ebp (par ex. 8[ebp], 12[ebp], ...),
     *   calculé à partir de {@code lambdaParams}.
     */
    private void genVariable(Variable v) {
        // Si on est dans une lambda et que c'est un paramètre → accès [ebp]
        if (inLambda && lambdaParams != null) {                           
            int idx = lambdaParams.indexOf(v.getName());
            if (idx >= 0) {
                int n = lambdaParams.size();
                int offset = (n - idx + 1) * 4; // param 0 → (n+1)*4, param n-1 → 8
                buf().append("\tmov eax, ").append(offset).append("[ebp]\n");
                return;
            }
        }
        buf().append("\tmov eax, ").append(v.getName()).append("\n");
    }

    private void genNil() {
        buf().append("\tmov eax, 0\n");
    }

    private void genInput() {
        buf().append("\tin eax\n");
    }

    private void genOutput(Output o) {
        gen(o.getExpression());
        buf().append("\tout eax\n");
    }

    // =========================================================================
    // AFFECTATION ET SÉQUENCE
    // =========================================================================

    private void genLet(Let l) {
        gen(l.getExpression());
        buf().append("\tmov ").append(l.getVarName()).append(", eax\n");
    }

    private void genSequence(Sequence s) {
        gen(s.getLeft());
        gen(s.getRight());
    }

    // =========================================================================
    // OPÉRATIONS ARITHMÉTIQUES ET BOOLÉENNES
    // =========================================================================

    private void genBinaryOp(BinaryOp b) {
        String op = b.getOperator();

        switch (op) {
            case "AND" -> { genAnd(b); return; }
            case "OR"  -> { genOr(b);  return; }
        }

        // Calcul de gauche → eax, sauvegarde sur la pile
        gen(b.getLeft());
        buf().append("\tpush eax\n");

        // Calcul de droite → eax
        gen(b.getRight());
        buf().append("\tpop ebx\n"); // ebx = gauche, eax = droite

        switch (op) {
            case "+"   -> { buf().append("\tadd ebx, eax\n");
                            buf().append("\tmov eax, ebx\n"); }
            case "-"   -> { buf().append("\tsub ebx, eax\n"); // gauche - droite = ebx - eax
                            buf().append("\tmov eax, ebx\n"); }
            case "*"   -> buf().append("\tmul eax, ebx\n");
            case "/"   -> { buf().append("\tdiv ebx, eax\n"); // ebx = ebx / eax
                            buf().append("\tmov eax, ebx\n"); }
            case "mod" -> { // a mod b : on calcule a - (a/b)*b, ebx=a (gauche), eax=b (droite)
                buf().append("\tmov ecx, eax\n");  // ecx = b
                buf().append("\tmov eax, ebx\n");  // eax = a
                buf().append("\tdiv ebx, ecx\n");  // ebx = a/b
                buf().append("\tmul ebx, ecx\n");  // ebx = (a/b)*b
                buf().append("\tsub eax, ebx\n");  // eax = a - (a/b)*b
            }
            case "<"   -> genComparison("jl", b);
            case "<="  -> genComparisonLeq(b);
            case ">"   -> genComparison("jg", b);   
            case ">="  -> genComparisonGeq(b);      
            case "="   -> genComparison("jz", b);
            default    -> buf().append("\t; opérateur inconnu: ").append(op).append("\n");
        }
    }

    private void genComparison(String jumpInstr, BinaryOp b) {
        // ebx = gauche, eax = droite (déjà calculés par genBinaryOp)
        int label = newLabel();
        buf().append("\tsub ebx, eax\n");
        buf().append("\t").append(jumpInstr)
             .append(" vrai_").append(jumpInstr).append("_").append(label).append("\n");
        buf().append("\tmov eax, 0\n");
        buf().append("\tjmp fin_").append(jumpInstr).append("_").append(label).append("\n");
        buf().append("vrai_").append(jumpInstr).append("_").append(label).append(":\n");
        buf().append("\tmov eax, 1\n");
        buf().append("fin_").append(jumpInstr).append("_").append(label).append(":\n");
    }

    private void genComparisonLeq(BinaryOp b) {
        // ebx=a (gauche), eax=b (droite) : a <= b  ⟺  b - a >= 0
        int label = newLabel();
        buf().append("\tsub eax, ebx\n"); // eax = droite - gauche
        buf().append("\tjns fauxleq_").append(label).append("\n");
        buf().append("\tmov eax, 1\n");
        buf().append("\tjmp finleq_").append(label).append("\n");
        buf().append("fauxleq_").append(label).append(":\n");
        buf().append("\tmov eax, 0\n");
        buf().append("finleq_").append(label).append(":\n");
    }


    private void genComparisonGeq(BinaryOp b) {
        // ebx=a (gauche), eax=b (droite) : a >= b  ⟺  a - b >= 0
        int label = newLabel();
        buf().append("\tsub ebx, eax\n"); // ebx = gauche - droite
        buf().append("\tjns vraigeq_").append(label).append("\n");
        buf().append("\tmov eax, 0\n");
        buf().append("\tjmp fingeq_").append(label).append("\n");
        buf().append("vraigeq_").append(label).append(":\n");
        buf().append("\tmov eax, 1\n");
        buf().append("fingeq_").append(label).append(":\n");
    }

    private void genUnaryOp(UnaryOp u) {
        gen(u.getOperand());
        switch (u.getOperator()) {
            case "-" -> { // eax = 0 - eax
                buf().append("\tmov ebx, 0\n");
                buf().append("\tsub ebx, eax\n");
                buf().append("\tmov eax, ebx\n");
            }
            case "NOT" -> { // NOT booléen : 1 - eax
                int label = newLabel();
                buf().append("\tjz vrainot_").append(label).append("\n");
                buf().append("\tmov eax, 0\n");
                buf().append("\tjmp finnot_").append(label).append("\n");
                buf().append("vrainot_").append(label).append(":\n");
                buf().append("\tmov eax, 1\n");
                buf().append("finnot_").append(label).append(":\n");
            }
            default -> buf().append("\t; opérateur unaire inconnu : ")
                            .append(u.getOperator()).append("\n");
        }
    }

    private void genAnd(BinaryOp b) {
        int label = newLabel();
        gen(b.getLeft());
        buf().append("\tjz fauxand_").append(label).append("\n");
        gen(b.getRight());
        buf().append("\tjz fauxand_").append(label).append("\n");
        buf().append("\tmov eax, 1\n");
        buf().append("\tjmp finand_").append(label).append("\n");
        buf().append("fauxand_").append(label).append(":\n");
        buf().append("\tmov eax, 0\n");
        buf().append("finand_").append(label).append(":\n");
    }

    private void genOr(BinaryOp b) {
        int label = newLabel();
        gen(b.getLeft());
        buf().append("\tjnz suiteor_").append(label).append("\n");
        buf().append("\tmov eax, 1\n");
        buf().append("\tjmp finor_").append(label).append("\n");
        buf().append("suiteor_").append(label).append(":\n");
        gen(b.getRight());
        buf().append("\tjz fauxor_").append(label).append("\n");
        buf().append("\tmov eax, 1\n");
        buf().append("\tjmp finor_").append(label).append("\n");
        buf().append("fauxor_").append(label).append(":\n");
        buf().append("\tmov eax, 0\n");
        buf().append("finor_").append(label).append(":\n");
    }

    // =========================================================================
    // STRUCTURES DE CONTRÔLE
    // =========================================================================

    private void genIfThenElse(IfThenElse i) {
        int label = newLabel();
        gen(i.getCondition());
        buf().append("\tjz else_").append(label).append("\n");
        gen(i.getThenBranch());
        buf().append("\tjmp finif_").append(label).append("\n");
        buf().append("else_").append(label).append(":\n");
        gen(i.getElseBranch());
        buf().append("finif_").append(label).append(":\n");
    }

    private void genWhile(While w) {
        int label = newLabel();
        buf().append("debutwhile_").append(label).append(":\n");
        gen(w.getCondition());
        buf().append("\tjz finwhile_").append(label).append("\n");
        gen(w.getBody());
        buf().append("\tjmp debutwhile_").append(label).append("\n");
        buf().append("finwhile_").append(label).append(":\n");
    }

    // =========================================================================
    // LAMBDA ET APPEL DE FONCTION  
    // =========================================================================

    private void genLambda(Lambda lm) {
        int label = newLabel();
        List<String> params = lm.getParams();
        int n = params.size();

        // Dans le code principal : charger l'adresse de la lambda dans eax
        code.append("\tlea eax, lambda_").append(label).append("\n");

        // Émettre le corps dans le buffer lambdas
        lambdas.append("lambda_").append(label).append(":\n");
        lambdas.append("\tenter 0\n");

        // Activer le mode lambda
        boolean savedInLambda       = inLambda;
        List<String> savedParams    = lambdaParams;
        inLambda     = true;
        lambdaParams = params;

        gen(lm.getBody()); // génère dans lambdas via buf()


        inLambda     = savedInLambda;
        lambdaParams = savedParams;


        lambdas.append("\tleave\n");
        lambdas.append("\tret\n");
    }

private void genFCall(FCall fc) {
    List<Node> args = fc.getArgs();
    int n = args.size();


    buf().append("\tmov eax, ").append(fc.getName()).append("\n");
    buf().append("\tpush eax\n");


    for (Node arg : args) {
        gen(arg);
        buf().append("\tpush eax\n");
    }


    buf().append("\tmov eax, ").append(n * 4).append("[esp]\n");
    buf().append("\tcall eax\n");
    buf().append("\tadd esp, ").append((n + 1) * 4).append("\n");

}

}
