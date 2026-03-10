# TP Compilation - Génération de code λ-ada

**Auteur :** Clément [Nom]

## Description

Compilateur pour un sous-ensemble du langage λ-ada.
Génère du code assembleur pour la machine à registres (vm-0.9.jar).

## Compilation

./gradlew clean build

## Utilisation

java -jar build/libs/I805_TP2_Lambada.jar source.txt > output.asm
java -jar vm-0.9.jar output.asm

## Exemples

### Exercice 1 - Expressions arithmétiques

# source : test_ex1.txt

# let prixHt = 200;

# let prixTtc = prixHt \* 119 / 100.

java -jar build/libs/I805_TP2_Lambada.jar test_ex1.txt > ex1.asm

### Exercice 2 - PGCD

# source : test_ex2.txt

java -jar build/libs/I805_TP2_Lambada.jar test_ex2.txt > pgcd.asm
printf "48\n18\n" | java -jar vm-0.9.jar pgcd.asm

# → 6
