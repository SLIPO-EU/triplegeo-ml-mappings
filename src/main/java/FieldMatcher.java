import org.yaml.snakeyaml.Yaml;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

public class FieldMatcher {

    ArrayList<Predicate> predicates;
    ArrayList<Field> fields;

    Logistic mdl;
    Instances trainSet;


    public FieldMatcher(){
        predicates = new ArrayList<Predicate>();
        fields = new ArrayList<Field>();
    }

    public void readSingleDataset(String yamlPath, String csvPath){

        Yaml yaml = new Yaml();
        InputStream inputStream = null;
        try {
           inputStream = new FileInputStream(yamlPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Map<String, Object> data = yaml.load(inputStream);
        Set<String> keys = data.keySet();

        for(String key:keys) {
            Map<String, String> attrs = (Map<String, String>) data.get(key);
            if (!predicateExists(attrs))
                addToPredicates(attrs);
            addToFields(key, attrs);
        }

    }

     private void addToFields(String key, Map<String,String> attrs) {
        Field fld = new Field(key,attrs);
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

    public void trainClassifiers(){
      // trainSingleMdl();
         for(Predicate pred:predicates)
             pred.trainClassifier(fields);
    }

    public void trainSingleMdl(){

        mdl = new Logistic();
        mdl.setRidge(0);
        ArrayList<Attribute> trainAtts = new ArrayList<Attribute>();

        for(Predicate pred:predicates)
            for (String kw : pred.getKeywordsUnique())
                trainAtts.add(new Attribute(kw + "Sim"));

        trainAtts.add(new Attribute("MatchLabel", getAllPredNames()));

        trainSet = new Instances("data", trainAtts, 0);
        trainSet.setClass(trainAtts.get(trainAtts.size()-1));

        Instance inst;
        ArrayList<Double> sims;
        for (Field fld : fields) {
            sims = new ArrayList<Double>();
            for (Predicate pred : predicates) {
                int count=0;
                for (String kw : pred.getKeywordsUnique()) {
                    if(fld.getName().equals(kw) && count==0) {
                        sims.add(0.0);
                        count++;
                    }
                    else
                        sims.add(BigramSimCalculator.calcBigramSim(fld.getName(), kw));
                }
            }
            inst = new DenseInstance(sims.size() + 1);
            inst.setDataset(trainSet);
            for (int i = 0; i < sims.size(); i++)
                inst.setValue(i, sims.get(i));
            inst.setValue(sims.size(), fld.getMatchingPred());
            trainSet.add(inst);
        }
        try {
            mdl.buildClassifier(trainSet);
            performBigCV();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void performBigCV(){
        Logistic mdl = new Logistic();
        Evaluation eval = null;
        try {
            eval = new Evaluation(trainSet);
            eval.crossValidateModel(mdl, trainSet, trainSet.size(), new Random(1));
            System.out.println(eval.toSummaryString());
            System.out.println(eval.toMatrixString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

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

    private ArrayList<String> getAllPredNames() {
        ArrayList<String> allPredNames = new ArrayList<String>();
        for(Predicate pred:predicates)
            allPredNames.add(pred.getName());
        return(allPredNames);
    }

    public double evaluate() {
        Field test = null;
        ArrayList<String> preds = new ArrayList<String>();
        String pred;
        double acc = 0;
        for (int i = 0; i < fields.size(); i++) {
            System.out.println(i);
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
        return (acc);
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

    public void readAll() {
        readSingleDataset("/home/pant/Desktop/slipo/sample_YML_mappings/sample1.yml","");
        readSingleDataset("/home/pant/Desktop/slipo/sample_YML_mappings/sample2.yml","");
        readSingleDataset("/home/pant/Desktop/slipo/sample_YML_mappings/sample3.yml","");
        readSingleDataset("/home/pant/Desktop/slipo/sample_YML_mappings/sample4.yml","");
        readSingleDataset("/home/pant/Desktop/slipo/sample_YML_mappings/sample5.yml","");
        readSingleDataset("/home/pant/Desktop/slipo/sample_YML_mappings/sample6.yml","");
        readSingleDataset("/home/pant/Desktop/slipo/sample_YML_mappings/sample7.yml","");
        readSingleDataset("/home/pant/Desktop/slipo/sample_YML_mappings/sample8.yml","");
    }
}

