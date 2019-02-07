package eu.slipo.triplegeo.ml.mappings;

import java.util.HashMap;
import java.util.LinkedHashMap;

/*
Contains mappings for each column (field) of a csv.
Object "maps" contains a LinkedHashMap for each column. Column name is the key.
LinkedHashMap contains the probability that the column  matches to each predicate.
Predicates are in decreasing probability order.
(Probability may be low for all. We are interested in its relative value for different predicates.)
*/
public class Mappings {

    HashMap<String,LinkedHashMap<String,Double>> maps;

    public Mappings(){
        maps = new HashMap<String,LinkedHashMap<String, Double>>();
    }

    //add mappings for column "name"
    public void addFieldMap(String name, LinkedHashMap<String,Double> preds) {
        maps.put(name,preds);
    }

    //get mappings for column "name"
    public LinkedHashMap<String,Double> getFieldMap(String name) {
        return maps.get(name);
    }

}
