package com.nlp.stringsimilarity;

/**
 * Used to indicate the cost of character substitution.
 * 
 * Cost should always be in [0.0 .. 1.0]
 * For example, in an OCR application, cost('o', 'a') could be 0.4
 * In a checkspelling application, cost('u', 'i') could be 0.4 because these are
 * next to each other on the keyboard...
 * 
 */
public interface CharacterSubstitutionInterface {
    public double cost(char c1, char c2);
}