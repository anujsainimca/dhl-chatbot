package com.nlp.stringsimilarity;
import java.util.*;

class SortStringArray
{
    public static void main (String[] args) throws java.lang.Exception
    {
        String S = "No one could disentangle correctly";
        String W[] = S.split(" ");
        Arrays.sort(W, new StringLengthComparator());
        for(String str: W)
        System.out.println(str); //print Your Expected Result.
    }
}
 class StringLengthComparator implements Comparator<String>{ //Custom Comparator class according to your need

    public int compare(String str1, String str2) {
            return str1.length() - str2.length();// compare length of Strings
        }
 }