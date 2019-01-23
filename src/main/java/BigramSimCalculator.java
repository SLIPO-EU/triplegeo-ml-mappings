import java.util.ArrayList;
import java.util.List;

/*
Class that calculates the string similarity between keywords
Takes values in [0,1]
 */
public class BigramSimCalculator {

    static double calcBigramSim(String s1, String s2){

        if(s1.length()>1 && s2.length()>1) {
            List<char[]> b1 = bigram(s1);
            List<char[]> b2 = bigram(s2);
            return bigramSimScore(b1, b2);
        }
        else if(s1.length()==1 && s2.length()==1)
            if(s2.contains(s1))
                return(1);
            else
                return(0);
        else if(s1.length()==1 && s2.length()>1)
            if(s2.contains(s1))
                return(1/(s2.length()+1));
            else
                return(0);
        else
            if(s1.contains(s2))
                return(1/(s1.length()+1));
            else
                return(0);


    }

    public static List<char[]> bigram(String input) {
        ArrayList<char[]> bigram = new ArrayList<char[]>();
        for (int i = 0; i < input.length() - 1; i++) {
            char[] chars = new char[2];
            chars[0] = input.charAt(i);
            chars[1] = input.charAt(i+1);
            bigram.add(chars);
        }
        return bigram;
    }

    public static double bigramSimScore(List<char[]> bigram1, List<char[]> bigram2) {
        List<char[]> copy = new ArrayList<char[]>(bigram2);
        int matches = 0;
        for (int i = bigram1.size(); --i >= 0;) {
            char[] bigram = bigram1.get(i);
            for (int j = copy.size(); --j >= 0;) {
                char[] toMatch = copy.get(j);
                if (bigram[0] == toMatch[0] && bigram[1] == toMatch[1]) {
                    copy.remove(j);
                    matches += 2;
                    break;
                }
            }
        }
        return (double) matches / (bigram1.size() + bigram2.size());
    }

}
