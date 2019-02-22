package eu.slipo.triplegeo.ml.mappings;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import com.opencsv.CSVIterator;
import com.opencsv.CSVParser;
import com.opencsv.CSVReader;

/**
 * Class that produces predicate matchings for each column of a CSV file.
 */
public class FieldMatcher implements Serializable {

    private static final long serialVersionUID = 2L;

    private char delimiter = '|';

    private char quote = '"';

    private ArrayList<Predicate> predicates;

    private ArrayList<Field> fields;

    public FieldMatcher() {
        predicates = new ArrayList<Predicate>();
        fields = new ArrayList<Field>();
    }

    public static FieldMatcher create(String modelFileName) throws IOException, ClassNotFoundException {
        try (
            FileInputStream fileInputStream = new FileInputStream(modelFileName);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);
        ) {
            return (FieldMatcher) objectInputStream.readObject();
        }
    }

    public static FieldMatcher create(File file) throws IOException, ClassNotFoundException {
        try (
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);
        ) {
            return  (FieldMatcher) objectInputStream.readObject();
        }
    }

    public char getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    public char getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        if (quote == null) {
            this.quote = CSVParser.NULL_CHARACTER;
        } else {
            this.quote = quote.charAt(0);
        }
    }

    /**
     * Reads a dataset and adds observed predicates and fields
     *
     * @param yamlPath The path to YAML mapping file
     * @param csvPath The path to CSV data file
     *
     * @throws IOException If an I/O error occurs
     */
    private void readSingleDataset(String yamlPath, String csvPath) throws IOException {
        // reads everything
        Yaml yaml = new Yaml();
        InputStream inputStream = new FileInputStream(yamlPath);
        FileReader filereader = new FileReader(csvPath);
        Map<String, Object> data = yaml.load(inputStream);
        Set<String> keys = data.keySet();
        CSVReader csvReader = new CSVReader(filereader, delimiter, quote);
        String[] fieldNames = csvReader.readNext();

        // produces features from field content
        boolean[][] feats = scanCSV(csvReader, fieldNames);

        // for each csv field add to fields and predicates
        for (String key : keys) {
            Map<String, String> attrs = (Map<String, String>) data.get(key);
            if (!predicateExists(attrs)) {
                addToPredicates(attrs);
            }
            addToFields(key, attrs, feats[whichEquals(fieldNames, key)]);
        }

    }

    /**
     * Creates features from the contents of each field
     *
     * @param csvReader The reader for reading a CSV data file
     * @param fieldNames A list of field names
     *
     * @return
     * @throws IOException
     */
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
        for(int i=0;i<fieldNum;i++) {
            for(int j=0;j<7;j++) {
                if(j!=6 && stats[i][j]>count[i]/2 || j==6 && stats[i][j]<count[i]/10) {
                    feats[i][j] = true;
                }
            }
        }
        return feats;
    }

    private double isMixed(String s) {
        s = s.replaceAll("[()\\s-]+", "");
        if(!StringUtils.isNumeric(s) && !StringUtils.isAlpha(s) && StringUtils.isAlphanumeric(s) ) {
            return 1;
        } else {
            return 0;
        }
    }

    private int isInteger(String s) {
        s = s.replaceAll("[()\\s-]+", "");
        try {
            double num = Double.parseDouble(s);
            if (Math.floor(num) == num && !Double.isInfinite(num)) {
                return 1;
            } else {
                return 0;
            }
        }
        catch(Exception e){
            return 0;
        }

    }

    private int isDouble(String s) {
        s = s.replaceAll("[()\\s-]+", "");
        try {
            double num = Double.parseDouble(s);
            if (Math.floor(num) != num && !Double.isInfinite(num)) {
                return 1;
            } else {
                return 0;
            }
        }
        catch (Exception e){
            return 0;
        }
    }

    private int isChar(String s) {
        s = s.replaceAll("[()\\s-]+", "");
        if(StringUtils.isAlpha(s)) {
            return 1;
        } else {
            return 0;
        }
    }

    private int hasAt(String s) {
        if(s.contains("@")) {
            return 1;
        } else {
            return 0;
        }
    }

    private int hasInternet(String s) {
        if(s.contains("www") || s.contains("http")) {
            return 1;
        } else {
            return 0;
        }
    }

    private int isDistinct(String s, HashSet<String> hs) {
    if(hs.contains(s)) {
        return 0;
    } else {
        hs.add(s);
        return 1;
        }
    }

    private int whichEquals(String[] fieldNames, String key) {
        for(int i=0;i<fieldNames.length;i++) {
            if(fieldNames[i].toUpperCase().equals(key.toUpperCase())) {
                return i;
            }
        }
        return fieldNames.length;
    }

    private void addToFields(String key, Map<String,String> attrs, boolean[] feats) {
        Field fld = new Field(key,attrs,feats);
        fields.add(fld);
        updateKeywords(fld);
    }

    private void updateKeywords(Field fld) {
        for(Predicate pred:predicates) {
            if(pred.getName().equals(fld.getMatchingPred())) {
                pred.addKeyword(fld.getName());
            }
        }
    }

    private void addToPredicates(Map<String,String> attrs) {
        Predicate pred = new Predicate(attrs.get("predicate"));
        predicates.add(pred);
    }

    private boolean predicateExists(Map<String,String> attrs) {
        String predName = attrs.get("predicate");
        for(Predicate pred:predicates) {
            if(pred.getName().equals(predName.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    //trains one classifier for each predicate
    private void trainClassifiers() throws Exception {
        for(Predicate pred:predicates) {
            pred.trainClassifier(fields);
        }
    }

    //for a given input field, runs the classifiers for each predicate, returns predicate:probability map
    private LinkedHashMap<String,Double> predict(Field fld) throws Exception {
        LinkedHashMap<String,Double> res = new LinkedHashMap<String, Double>();
        for(Predicate pred:predicates) {
            res.put(pred.getName(),pred.predict(fld));
        }
        return sortPreds(res);
    }

    private LinkedHashMap<String,Double> sortPreds(LinkedHashMap<String,Double> res) {


        List<Map.Entry<String, Double>> entries = new ArrayList<Map.Entry<String, Double>>(res.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> a, Map.Entry<String, Double> b) {
                return b.getValue().compareTo(a.getValue());
            }
        });

        LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> entry : entries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }



        //evaluates the model through leave-one-out cross validation
   /*public double evaluate(String outPath) throws Exception {
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

    private void printConfusionMatrix(ArrayList<Field> fields, ArrayList<String> preds,double acc) {
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
        for(int i=0;i<predicates.size();i++) {
            if(predicates.get(i).getName().equals(matchingPred)) {
                return i;
            }
        }
        return -1;
    }


    private void reAddField(ArrayList<Field> fields, Field fld,int i) {
        fields.add(i,fld);
        for(Predicate pred:predicates) {
            if(fld.getMatchingPred().equals(pred.getName())) {
                pred.addKeyword(fld.getName());
            }
        }
    }

    private Field removeField(ArrayList<Field> fields, int i) {
        Field fld = fields.remove(i);
        for(Predicate pred:predicates) {
            pred.removeField(fld);
        }
        return fld;
    }

    private void readAll(String folderPath, HashMap<String, String> nameMap) throws IOException {
        for(Map.Entry<String,String> name:nameMap.entrySet()) {
            readSingleDataset(folderPath+name.getKey(), folderPath+name.getValue());
        }
    }

    //Reads all .yml .csv from a file. It matches each .yml to a .csv . If not matching .csv exists it does not use the .yml.
    private void readAllFromFolder(String folderPath) throws IOException {

        File f = new File(folderPath);
        String[] namesAr = f.list();
        ArrayList<String> names = new ArrayList<String>(Arrays.asList(namesAr));
        ArrayList<String> yamls = getYamls(names);
        ArrayList<String> csvs = getCsvs(names);
        HashMap<String,String> nameMap = alignLists(yamls,csvs);

        readAll(folderPath,nameMap);

    }

   private HashMap alignLists(ArrayList<String> yamls, ArrayList<String> csvs) {
        HashMap<String,String> nameMap = new HashMap<String, String>();
        int ind;
        for(String y:yamls) {
            ind = matchCsv(y,csvs);
            if ( ind > -1 ) {
                nameMap.put(y,csvs.get(ind));
            }
        }
        return nameMap;
    }



    private int matchCsv(String y, ArrayList<String> csvs) {
        for(int i=0;i<csvs.size();i++) {
            if(stripEnd(y).equals(stripEnd(csvs.get(i)))) {
                return i;
            }
        }
        return -1;
    }

    private String stripEnd(String s) {
        return s.substring(0,s.length()-4);
    }

    private ArrayList<String> getCsvs(ArrayList<String> names) {
        ArrayList<String> csvs = new ArrayList<String>();
        for(String n:names) {
            if(n.contains(".csv")) {
                csvs.add(n);
            }
        }
        return csvs;
    }

    private ArrayList<String> getYamls(ArrayList<String> names) {
        ArrayList<String> yamls = new ArrayList<String>();
        for(String n:names) {
            if(n.contains(".yml")) {
                yamls.add(n);
            }
        }
        return yamls;
    }

    /**
     * Constructs features for new fields and calls predict
     *
     * @param csvPath The path to a CSV data file
     * @return An instance of {@link Mappings} with the file fields mappings
     *
     * @throws Exception
     */
    public Mappings giveMatchings(String csvPath) throws Exception {

        //read
        FileReader filereader = null;
        filereader = new FileReader(csvPath);
        CSVReader csvReader = new CSVReader(filereader, delimiter, quote);
        String[] fieldNames = csvReader.readNext();

        //preproc
        boolean[][] feats = scanCSV(csvReader,fieldNames);
        ArrayList<Field> fields = new ArrayList<Field>();
        for(int i=0;i<fieldNames.length;i++) {
            fields.add(new Field(fieldNames[i], feats[i]));
        }

        //predict
        Mappings maps = new Mappings();
        for(Field fld:fields) {
            maps.addFieldMap(fld.getName(),predict(fld));
        }

        return maps;

    }

    private void writeFMToFile(String path) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(path);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(this);
        out.close();
        fileOut.close();
    }

    /**
     * Reads all YAML configuration files and CSV data files from the input folder. For
     * each configuration file, a matching data file is expected with the same name as the
     * former one.
     *
     * The current instance of the {@link FieldMatcher} object containing the trained
     * classifiers is serialized and stored in the output file.
     *
     * @param inFolderPath The folder with input files
     * @param outModelsPath The mode output file name
     * @throws Exception
     */
    public void makeModels(String inFolderPath,String outModelsPath) throws Exception {
        readAllFromFolder(inFolderPath);
        trainClassifiers();
        writeFMToFile(outModelsPath);
    }
}

