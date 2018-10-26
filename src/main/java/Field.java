import java.util.ArrayList;
import java.util.Map;

public class Field {

    private String name;
    private String matchingPred;
    private ArrayList<Integer> feats;
    private Map<String,String> attrs;


    public Field(String key, Map<String, String> attrs) {
        name = key.toUpperCase();
        this.attrs = attrs;
        matchingPred = ((String)attrs.get("predicate").toUpperCase());
        feats = new ArrayList<Integer>();
    }


    public String getName() {
        return name;
    }

    public String getMatchingPred() {
        return matchingPred;
    }

    public void addFeat(int feat){
        feats.add(feat);
    }
}
