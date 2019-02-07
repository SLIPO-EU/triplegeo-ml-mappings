package eu.slipo.triplegeo.ml.mappings;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import java.util.*;

/*
Contains the model for a single predicate
It contains the model, that you want to change.
It also contains a List with all the field names previously matched to this predicate.
Uses the WEKA library.
 */
public class Predicate {

    private String name;
    private ArrayList<String> keywords;
    private ArrayList<String> keywordsUnique;

    //this is the model
    //currently logistic regression with L2 regularizer (ridge)
    //regularization is important because data are few
    private Logistic mdl;

    private Instances trainSet;

    //HINT: this class is the one that you have to change in order to test additional models
    public Predicate(String name) {
        this.name = name.toUpperCase();
        keywords = new ArrayList<String>();
        keywords.add(strip(name).toUpperCase());
        keywordsUnique = new ArrayList<String>();
        keywordsUnique.add(strip(name).toUpperCase());

        //model initialization
        //for parameter selection you may want to do cross validation
        //current data-size does not allow for very extensive model-parameter search
        mdl = new Logistic();
        mdl.setRidge(0.5);
    }

    private String strip(String name) {
        return name.substring(name.indexOf(':')+1);
    }

    public void addKeyword(String kw){
        keywords.add(kw.toUpperCase());
        if(!keywordsUnique.contains(kw.toUpperCase()))
            keywordsUnique.add(kw.toUpperCase());
    }

    //constructs the training set and trains the model
    //recieves as input all observed fields, keep only those that match
    //the specific predicate
    public void trainClassifier(ArrayList<Field> fields) {

        //constructs the training set in the way require for WEKA
        //you should check a tutorial for WEKA

        //first creates an attribute list with each feature of the model and the label
        ArrayList<Attribute> trainAtts = new ArrayList<Attribute>();
        for(String kw:keywordsUnique)
            trainAtts.add(new Attribute(kw+"_sim"));
        for(int i=0;i<fields.get(0).getFeats().length;i++)
            trainAtts.add(new Attribute("feat"+i));

        trainAtts.add(new Attribute("MatchLabel",Arrays.asList("0", "1")));

        //creates trainset
        //initialize with attribute list
        trainSet = new Instances(name,trainAtts,0);
        trainSet.setClass(trainAtts.get(trainAtts.size()-1));

        Instance inst;
        double[] sims;

        //builds feature vector for each observed field
        //feature vector contains similarity with predicates keywords
        //and features concerning the content of the field
        for(Field fld:fields) {

            sims = calcAllSimsUnique(fld.getName());

            inst = new DenseInstance(keywordsUnique.size()+fld.getFeats().length+1);
            inst.setDataset(trainSet);

            // adds similarities with all keywords
            for(int i=0;i<sims.length;i++)
                inst.setValue(i,sims[i]);
            //adds content features
            for(int i=0;i<fld.getFeats().length;i++)
                inst.setValue(sims.length+i,fld.getFeat(i));
            //adds label
            if(fld.getMatchingPred().equals(name))
                inst.setValue(sims.length+fld.getFeats().length,"1");
            else
                inst.setValue(sims.length+fld.getFeats().length,"0");

            //fills trainSet
            trainSet.add(inst);

        }

        //builds model
        //also prints the model
        //can also perform cross validation to evaluate it
        try {
            mdl.buildClassifier(trainSet);
            System.out.println(mdl);
        //  performCV();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private double[] correctField(Field fld, double[] sims) {
        for(int i=0;i<keywordsUnique.size();i++)
            if (fld.getName().equals(keywordsUnique.get(i)) && countOccurences(keywords,fld.getName())<2)
                sims[i] = removeVal(sims,i);
        return sims;
    }

    public void removeField(Field fld){
         if(countOccurences(keywords,fld.getName())<2)
            keywordsUnique.remove(fld.getName());
        keywords.remove(fld.getName());
    }

    public int countOccurences(ArrayList<String> keywords, String name) {
        int count = 0;
        for(String kw:keywords)
            if(kw.equals(name))
                count++;
        return(count);
    }

    private double removeVal(double[] sims, int i) {
        double sum=0;
        for(int j=0;j<sims.length;j++)
            if(j!=i)
                sum+=sims[j];
        return(sum/(sims.length-1));
    }

    private double[] calcAllSimsUnique(String name) {
        double[] sim = new double[keywordsUnique.size()];
        for(int i=0;i<sim.length;i++)
            sim[i] = BigramSimCalculator.calcBigramSim(keywordsUnique.get(i), name);
        return sim;
    }

    public void performCV(){
        Logistic mdl = new Logistic();
        Evaluation eval = null;
        try {
            eval = new Evaluation(trainSet);
            eval.crossValidateModel(mdl, trainSet, trainSet.size(), new Random(1));
          //System.out.println(eval.toSummaryString());
            System.out.println(name);
            System.out.println(eval.toMatrixString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //predict performs the same procedure as train but for only one field
    public double predict(Field fld){
        double[] sims = calcAllSimsUnique(fld.getName());
        Instance inst = new DenseInstance(keywordsUnique.size()+fld.getFeats().length+1);
        inst.setDataset(trainSet);
        for(int i=0;i<sims.length;i++)
            inst.setValue(i,sims[i]);
        for(int i=0;i<fld.getFeats().length;i++)
            inst.setValue(sims.length+i,fld.getFeat(i));
        double[] pred = null;
        try {
            pred = mdl.distributionForInstance(inst);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //return probability of positive label
        return(pred[1]);
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getKeywordsUnique() {
        return keywordsUnique;
    }
}
