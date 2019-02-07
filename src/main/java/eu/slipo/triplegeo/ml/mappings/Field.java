package eu.slipo.triplegeo.ml.mappings;
import java.util.ArrayList;
import java.util.Map;

/*
Contains the data for each field (csv column)
 */

public class Field {

    //field name (keyword)
    private String name;
    //predicate that the field matches to
    private String matchingPred;
    //content-dependent features (e.g., is numeric, contains '@' etc), populated at read, csv content is not kept
    private boolean[] feats;
    private Map<String,String> attrs;


    public Field(String key, Map<String, String> attrs, boolean[] feats) {
        name = key.toUpperCase();
        this.attrs = attrs;
        matchingPred = ((String)attrs.get("predicate").toUpperCase());
        this.feats = feats;
    }

    public Field(String key, boolean[] feats) {
        name = key.toUpperCase();
        this.feats = feats;
    }

    public String getName() {
        return name;
    }

    public String getMatchingPred() {
        return matchingPred;
    }

    public boolean[] getFeats() {
        return feats;
    }

    public int getFeat(int i) {
        if(feats[i])
            return 1;
        else
            return 0;
    }
}
