package com.nlp.stringsimilarity;

/**
 * The similarity between the two strings is the cosine of the angle between
 * these two vectors representation. It is computed as V1 . V2 / (|V1| * |V2|)
 * The cosine distance is computed as 1 - cosine similarity.
 *
 */
public class Cosine extends ShingleBased implements
        NormalizedStringDistance, NormalizedStringSimilarity {

    /**
     * Implements Cosine Similarity between strings. The strings are first
     * transformed in vectors of occurrences of k-shingles (sequences of k
     * characters). In this n-dimensional space, the similarity between the two
     * strings is the cosine of their respective vectors.
     *
     * @param k
     */
    public Cosine(int k) {
        super(k);
    }

    public Cosine() {
        super();
    }

    public double similarity(String s1, String s2) {
        KShingling ks = new KShingling(k);
        int[] profile1 = ks.getArrayProfile(s1);
        int[] profile2 = ks.getArrayProfile(s2);

        return dotProduct(profile1, profile2) / (norm(profile1) * norm(profile2));
    }

    /**
     * Compute the norm L2 : sqrt(Sum_i( v_i²))
     *
     * @param profile
     * @return L2 norm
     */
    protected static double norm(int[] profile) {
        double agg = 0;

        for (int v : profile) {
            agg += v * v;
        }

        return Math.sqrt(agg);
    }

    protected static double dotProduct(int[] profile1, int[] profile2) {
        int length = Math.max(profile1.length, profile2.length);
        profile1 = java.util.Arrays.copyOf(profile1, length);
        profile2 = java.util.Arrays.copyOf(profile2, length);

        double agg = 0;
        for (int i = 0; i < length; i++) {
            agg += profile1[i] * profile2[i];
        }
        return agg;
    }

    public double distance(String s1, String s2) {
        return similarity(s1, s2);
    }

    public static void main(String args[])
    {
    	Cosine cosine=new Cosine();
    	String s1="sapient corporation";
    	String s2="sapient corportion";
    	Double matchingScore=cosine.distance(s1,s2);
    	System.out.println(matchingScore);
    }

}