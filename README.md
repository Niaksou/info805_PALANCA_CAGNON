```
# TP Compilation - Génération de code λ-ada

**Auteur :** Clément Palanca et Leny Cagnon

## Description

Compilateur pour un sous-ensemble du langage λ-ada (exercices 1, 2 et 3).
Génère du code assembleur pour la machine à registres (`vm-0.9.jar`).

Supporte : expressions arithmétiques, booléennes, structures de contrôle
(`if/then/else`, `while`), entrées/sorties, et **fonctions lambda avec appel**.

## Compilation

    ./gradlew clean build

## Utilisation

    java -jar build/libs/I805_TP2_Lambada.jar source.txt > output.asm
    java -jar vm-0.9.jar output.asm

## Exemples

### Exercice 1 – Expressions arithmétiques

    # source : test_ex1.txt
    # let prixHt = 200;
    # let prixTtc = prixHt * 119 / 100.

    java -jar build/libs/I805_TP2_Lambada.jar test_ex1.txt > ex1.asm
    java -jar vm-0.9.jar ex1.asm

### Exercice 2 – PGCD

    java -jar build/libs/I805_TP2_Lambada.jar test_ex2.txt > pgcd.asm
    printf "48\n18\n" | java -jar vm-0.9.jar pgcd.asm
    # → 6

### Exercice 3 – Lambdas

    # source : test_ex3.txt
    # let double = lambda(x) x * 2;
    # let r = double(21).

    java -jar build/libs/I805_TP2_Lambada.jar test_ex3.txt > ex3.asm
    java -jar vm-0.9.jar ex3.asm
    # → 42

## Architecture du générateur de code

Le générateur (`CodeGenerator.java`) parcourt l'AST en trois passes :

1. **Collecte des variables** (`collectVars`) – identifie toutes les variables
   globales introduites par des noeuds `Let` pour les déclarer dans le segment
   `DATA`. Les paramètres de lambdas sont exclus (ils vivent sur la pile).
2. **Segment DATA** – émet une déclaration `DD` par variable globale.
3. **Segment CODE** – génère le code principal, puis insère un `jmp endpgX`
   pour contourner les corps de lambdas, colle ces corps à la suite, et
   termine par l'étiquette `endpgX`.

## Gestion des lambdas

### Le problème

Un corps de lambda ne doit **pas** être exécuté en ligne lors de la
définition. Sans précaution, le CPU tomberait dedans immédiatement en
traversant le segment CODE de haut en bas.

### La solution : double buffer + jmp

Le générateur maintient deux `StringBuilder` séparés :

- `code` -> instructions du programme principal
- `lambdas` -> corps des lambdas, accumulés à part

La méthode `buf()` redirige automatiquement les émissions vers le bon
buffer selon le flag `inLambda`. Au moment de finaliser, le segment CODE
est structuré ainsi :

    CODE SEGMENT
        ... code principal ...
        jmp endpg0          <- saute par-dessus les lambdas

        lambda1:            <- corps de la lambda
            enter 0
            ...
            leave
            ret

    endpg0:                 <- reprise du programme principal
    CODE ENDS

### Variables locales vs globales dans une lambda

| Contexte             | Type de variable  | Accès généré                          |
|----------------------|-------------------|---------------------------------------|
| Programme principal  | Globale (`Let`)   | `mov eax, x` (segment DATA)           |
| Corps de lambda      | Paramètre         | `mov eax, 8[ebp]` (pile, offset ebp)  |
| Corps de lambda      | Globale (`Let`)   | `mov eax, x` (segment DATA)           |

Les paramètres sont passés sur la **pile** via la convention `call`/`ret`.
L'offset de chaque paramètre est calculé comme :

    offset = (n - idx - 1) * 4 + 8

où `n` est le nombre de paramètres et `idx` l'index du paramètre (0-based).

### Appel d'une lambda (FCall)

    lea eax, lambda1     ; adresse du corps
    push arg_n           ; push des arguments
    ...
    push arg_0
    call eax             ; appel -> résultat dans eax
    add esp, (n+1)*4     ; nettoyage de la pile

### Lambdas imbriquées

L'état `inLambda` et `lambdaParams` sont **sauvegardés et restaurés** à
chaque entrée/sortie de lambda, ce qui permet la génération correcte de
lambdas imbriquées.
```
