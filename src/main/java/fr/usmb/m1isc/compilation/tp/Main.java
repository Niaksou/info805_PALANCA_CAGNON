package fr.usmb.m1isc.compilation.tp;

import java.io.FileReader;
import java.io.InputStreamReader;

import fr.usmb.m1isc.compilation.tp.ast.Node;
import fr.usmb.m1isc.compilation.tp.codegen.CodeGenerator;
import java_cup.runtime.Symbol;

/**
 * Point d'entrée du compilateur.
 * Cette version lit un programme λ-ada, construit l'AST
 * (via JFlex + CUP), puis génère du code assembleur
 * pour la machine à registres (exercice 1).
 */
public class Main {

    /**
     * Méthode principale :
     *  - lit le fichier source passé en argument (ou stdin),
     *  - lance l'analyse lexicale et syntaxique,
     *  - récupère l'AST,
     *  - génère le code assembleur et l'affiche sur la sortie standard.
     *
     * @param args tableau d'arguments, éventuellement args[0] = chemin du fichier source
     * @throws Exception en cas d'erreur d'entrée/sortie ou d'analyse
     */
    public static void main(String[] args) throws Exception {
        LexicalAnalyzer yy;
        if (args.length > 0) {
            yy = new LexicalAnalyzer(new FileReader(args[0]));
        } else {
            yy = new LexicalAnalyzer(new InputStreamReader(System.in));
        }

        @SuppressWarnings("deprecation")
        parser p = new parser(yy);

        // Lancement du parsing pour obtenir l'AST
        Symbol result = p.parse();

        if (result != null && result.value != null) {
            Node ast = (Node) result.value;

            // Génération de code
            CodeGenerator gen = new CodeGenerator();
            String asm = gen.generate(ast);

            // Affichage du code assembleur sur la sortie standard
            System.out.println(asm);
        } else {
            System.err.println("Erreur : aucun arbre abstrait généré.");
        }
    }
}
