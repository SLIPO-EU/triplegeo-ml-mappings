package eu.slipo.triplegeo.ml.mappings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains mappings for each column (field) of a CSV file. Member {@link Mappings#fields}
 * contains a {@link LinkedHashMap} for each column. The column name is the key.
 * LinkedHashMap contains the probability that the column matches to each predicate.
 * Predicates are in decreasing probability order.
 *
 * Probability may be low for all. We are interested in its relative value for different
 * predicates.
 */
public class Mappings {

    public static class Field {

        private String name;
        private Map<String, Double> predicates;

        public Field(String name, Map<String, Double> predicates) {
            this.name = name;
            this.predicates = Collections.unmodifiableMap(predicates);
        }

        public String getName() {
            return name;
        }

        public Map<String, Double> getPredicates() {
            return predicates;
        }

    }

    private List<Field> fields = new ArrayList<Field>();

    public void addField(String name, LinkedHashMap<String, Double> predicates) {
        fields.add(new Field(name, predicates));
    }

    public Map<String, Double> getFieldPredicates(String name) {
        Field field = fields.stream().filter(f -> f.name.equals(name)).findFirst().orElse(null);

        return field == null ? null : field.predicates;
    }

    public List<Field> getFields() {
        return this.fields;
    }

}
