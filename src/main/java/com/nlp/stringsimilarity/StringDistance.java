
package com.nlp.stringsimilarity;

import java.io.Serializable;

public interface StringDistance extends Serializable {

    public double distance(String s1, String s2);
}