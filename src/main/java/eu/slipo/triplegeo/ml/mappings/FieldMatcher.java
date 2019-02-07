package eu.slipo.triplegeo.ml.mappings;
import com.opencsv.CSVIterator;
import com.opencsv.CSVReader;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.util.*;

/* Class that produces predicate matchings for each column of a new csv file.
The class maintains a binary classifier for each predicate. Performs two
functions: 1) trains the models given a set of csvs and mappings and
2) predicts the mappings of a new csv based on the trained models */
public class FieldMatcher {

    ArrayList<Predicate> predicates;
    ArrayList<Field> fields;

    public FieldMatcher(){
        predicates = new ArrayList<Predicate>();
        fields = new ArrayList<Field>();
    }

   //reads dataset, adds observed predicates and fields
   public void readSingleDataset(String yamlPath, String csvPath) throws IOException {

        //reads everything
        Yaml yaml = new Yaml();
        InputStream inputStream = new FileInputStream(yamlPath);
        FileReader filereader = new FileReader(csvPath);
        Map<String, Object> data = yaml.load(inputStream);
        Set<String> keys = data.keySet();
        CSVReader csvReader = new CSVReader(filereader,'|');
        String[] fieldNames = csvReader.readNext();

        //produces features from field content
        boolean[][] feats = scanCSV(csvReader,fieldNames);

        //for each csv field add to fields and predicates
        for(String key:keys) {
            Map<String, String> attrs = (Map<String, String>) data.get(key);
            if (!predicateExists(attrs))
                addToPredicates(attrs);
            addToFields(key, attrs, feats[whichEquals(fieldNames,key)]);
        }

    }

    //creates features from the contents of each field
    private boolean[][] scanCSV(CSVReader csvReader, String[] fieldNames) throws IOException {
        int fieldNum = fieldNames.length;
        double[][] stats = new double[fieldNum][7];
        CSVIterator csvIter = new CSVIterator(csvReader);
        String[] nxt = null;
        HashSet<String> hs = new HashSet<String>();
        double[] count = new double[fieldNum];
        for(int i=0;i<1000 && csvIter.hasNext();i++){
            nxt = csvIter.next();
            for(int j=0;j<fieldNum;j++) {
                if(!nxt[j].equals("")) {
                    count[j]++;
                    stats[j][0] += isInteger(nxt[j]);
                    stats[j][1] += isDouble(nxt[j]);
                    stats[j][2] += isChar(nxt[j]);
                    stats[j][3] += isMixed(nxt[j]);
                    stats[j][4] += hasAt(nxt[j]);
                    stats[j][5] += hasInternet(nxt[j]);
                    stats[j][6] += isDistinct(nxt[j], hs);
                }
            }
        }

        boolean[][] feats = new boolean[fieldNum+1][7];
        for(int i=0;i<fieldNum;i++)
            for(int j=0;j<7;j++)
                if(j!=6 && stats[i][j]>count[i]/2 || j==6 && stats[i][j]<count[i]/10)
                    feats[i][j] = true;
        return feats;
    }

    private double isMixed(String s) {
        s = s.replaceAll("[()\\s-]+", "");
        if(!StringUtils.isNumeric(s) && !StringUtils.isAlpha(s) && StringUtils.isAlphanumeric(s) )
            return 1;
        else
            return 0;
    }

    private int isInteger(String s) {
        s = s.replaceAll("[()\\s-]+", "");
        try {
            double num = Double.parseDouble(s);
            if (Math.floor(num) == num && !Double.isInfinite(num))
                return 1;
            else
                return 0;
        }
        catch(Exception e){
            return 0;
        }

    }

    private int isDouble(String s) {
        s = s.replaceAll("[()\\s-]+", "");
        try {
            double num = Double.parseDouble(s);
            if (Math.floor(num) != num && !Double.isInfinite(num))
                return 1;
            else
                return 0;
        }
        catch (Exception e){
            return 0;
        }
    }

    private int isChar(String s) {
        s = s.replaceAll("[()\\s-]+", "");
        if(StringUtils.isAlpha(s))
            return 1;
        else
            return 0;
    }

    private int hasAt(String s) {
        if(s.contains("@"))
            return 1;
        else
            return 0;
    }

    private int hasInternet(String s) {
        if(s.contains("www") || s.contains("http"))
            return 1;
        else
            return 0;
    }

    private int isDistinct(String s, HashSet<String> hs) {
    if(hs.contains(s))
        return 0;
    else {
        hs.add(s);
        return 1;
        }
    }

    private int whichEquals(String[] fieldNames, String key) {
        for(int i=0;i<fieldNames.length;i++)
            if(fieldNames[i].toUpperCase().equals(key.toUpperCase()))
                return i;
        return fieldNames.length;
    }

    private void addToFields(String key, Map<String,String> attrs, boolean[] feats) {
        Field fld = new Field(key,attrs,feats);
        fields.add(fld);
        updateKeywords(fld);
    }

    private void updateKeywords(Field fld) {
        for(Predicate pred:predicates)
            if(pred.getName().equals(fld.getMatchingPred()))
                pred.addKeyword(fld.getName());
    }

    private void addToPredicates(Map<String,String> attrs) {
        Predicate pred = new Predicate((String)attrs.get("predicate"));
        predicates.add(pred);
    }

    private boolean predicateExists(Map<String,String> attrs) {
        String predName = attrs.get("predicate");
        for(Predicate pred:predicates)
            if(pred.getName().equals(predName.toUpperCase()))
                return true;
        return false;
    }
    //trains one classifier for each predicate
    public void trainClassifiers(){
        for(Predicate pred:predicates)
            pred.trainClassifier(fields);
    }

    //for a given input field, runs the classifiers for each predicate
    //and selects the one that returns the highest matching probability
    public String predict(Field fld){
        double[] probs = new double[predicates.size()];
        for(int i=0;i<predicates.size();i++)
            probs[i] = predicates.get(i).predict(fld);
        double max = Double.NEGATIVE_INFINITY;
        int maxInd = -1;
        for(int i=0;i<probs.length;i++)
            if(probs[i]>max){
                max = probs[i];
                maxInd = i;
            }
        return(predicates.get(maxInd).getName());
    }

    //evaluates the model through leave-one-out cross validation
    public double evaluate(String outPath) {
        Field test = null;
        ArrayList<String> preds = new ArrayList<String>();
        String pred;
        double acc = 0;
        for (int i = 0; i < fields.size(); i++) {
            test = removeField(fields,i);
            trainClassifiers();
            pred = predict(test);
            if (pred.equals(test.getMatchingPred()))
                acc++;
            preds.add(predict(test));
            reAddField(fields,test,i);
        }
        acc /= fields.size();
        System.out.println(acc);
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(outPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for(int i=0;i<fields.size();i++)
            pw.println(fields.get(i).getName()+" -> "+preds.get(i));
        pw.close();
        return (acc);
    }

   /* private void printConfusionMatrix(ArrayList<Field> fields, ArrayList<String> preds,double acc) {
        double[][] confMat = new double[predicates.size()][predicates.size()];
        for (int i = 0; i < fields.size(); i++) {
            confMat[findInPredicates(predicates, fields.get(i).getMatchingPred())][findInPredicates(predicates, preds.get(i))]++;
        }
        PrintWriter pw = null;
        try {
            pw = new PrintWriter("/home/pant/Desktop/slipo/out");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < confMat.length; i++) {
            for (int j = 0; j < confMat.length; j++){
                pw.print(confMat[i][j]);
                pw.print(" ");
            }
            pw.println();
        }
        pw.close();
        try {
            pw = new PrintWriter("/home/pant/Desktop/slipo/preds");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for(Predicate pred:predicates)
            pw.println(pred.getName());
        pw.close();
    }
    */
    private int findInPredicates(ArrayList<Predicate> predicates, String matchingPred) {
        for(int i=0;i<predicates.size();i++)
            if(predicates.get(i).getName().equals(matchingPred))
                return i;
        return -1;
    }


    private void reAddField(ArrayList<Field> fields, Field fld,int i) {
        fields.add(i,fld);
        for(Predicate pred:predicates)
            if(fld.getMatchingPred().equals(pred.getName()))
                pred.addKeyword(fld.getName());
    }

    private Field removeField(ArrayList<Field> fields, int i) {
        Field fld = fields.remove(i);
        for(Predicate pred:predicates)
            pred.removeField(fld);
        return(fld);
    }

    public void readAll(ArrayList<String> yamls, ArrayList<String> csvs) throws IOException {
        for(int i=0;i<yamls.size();i++)
            readSingleDataset(yamls.get(i), csvs.get(i));
    }

    public void readAllFromFolder(String folderPath) throws IOException {

    }

    //constructs features for a new fields and calls predict
    public void giveMatchings(String csvPath,String outPath) throws IOException {
        FileReader filereader = null;
        filereader = new FileReader(csvPath);
        CSVReader csvReader = new CSVReader(filereader,'|');
        String[] fieldNames = csvReader.readNext();
        boolean[][] feats = scanCSV(csvReader,fieldNames);
        ArrayList<Field> fields = new ArrayList<Field>();
        for(int i=0;i<fieldNames.length;i++)
            fields.add(new Field(fieldNames[i], feats[i]));
        ArrayList<String> preds = new ArrayList<String>();
        for(Field fld:fields)
            preds.add(predict(fld));
        PrintWriter pw = new PrintWriter(outPath);
        for(int i=0;i<fields.size();i++)
            pw.println(fields.get(i).getName()+" -> "+preds.get(i));
        pw.close();
    }
}

