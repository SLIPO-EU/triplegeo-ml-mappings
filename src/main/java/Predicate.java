import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import java.util.*;

public class Predicate {

    private String name;
    private ArrayList<String> keywords;
    private ArrayList<String> keywordsUnique;

    private Logistic mdl;
    private Instances trainSet;

    public Predicate(String name) {
        this.name=name.toUpperCase();
        keywords = new ArrayList<String>();
        keywords.add(strip(name).toUpperCase());
        keywordsUnique = new ArrayList<String>();
        keywordsUnique.add(strip(name).toUpperCase());
        mdl = new Logistic();
        mdl.setRidge(0);
    }

    private String strip(String name) {
        return name.substring(name.indexOf(':')+1);
    }

    public void addKeyword(String kw){
        keywords.add(kw.toUpperCase());
        if(!keywordsUnique.contains(kw.toUpperCase()))
            keywordsUnique.add(kw.toUpperCase());
    }

   /*public void trainClassifier(ArrayList<Field> fields) {

        if(name.equals("slipo:fax")) {
            System.out.println("check");
        }


        ArrayList<Attribute> trainAtts = new ArrayList<Attribute>();
        trainAtts.add(new Attribute("StringDistance"));
        trainAtts.add(new Attribute("MatchLabel",Arrays.asList("0", "1")));
        trainSet = new Instances(name,trainAtts,0);
        trainSet.setClass(trainAtts.get(1));
        Instance inst;
        double[] sim;
        double[][] buf;

        for(Field fld:fields) {
            sim = calcAllSims(fld.name);
            inst = new DenseInstance(2);
            inst.setDataset(trainSet);
            if(fld.matchingPred.equals(name)){
                //select second maximum, skip same key
                inst.setValue(0,sim[sim.length-2]);
                inst.setValue(1,"1");
            }
            else{
                inst.setValue(0,sim[sim.length-1]);
                inst.setValue(1,"0");
            }
        //    System.out.println(name+" " +fld.name+" "+inst);
            trainSet.add(inst);
        }

        try {
            mdl.buildClassifier(trainSet);
            performCV();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }*/

    public void trainClassifier(ArrayList<Field> fields) {

        ArrayList<Attribute> trainAtts = new ArrayList<Attribute>();
        for(String kw:keywordsUnique)
            trainAtts.add(new Attribute(kw+"Sim"));
        trainAtts.add(new Attribute("MatchLabel",Arrays.asList("0", "1")));

        trainSet = new Instances(name,trainAtts,0);
        trainSet.setClass(trainAtts.get(trainAtts.size()-1));

        Instance inst;
        double[] sims;
        for(Field fld:fields) {

            sims = calcAllSimsUnique(fld.getName());

        //  sims = correctField(fld,sims);

            inst = new DenseInstance(keywordsUnique.size()+1);
            inst.setDataset(trainSet);
            for(int i=0;i<sims.length;i++)
                inst.setValue(i,sims[i]);
            if(fld.getMatchingPred().equals(name))
                inst.setValue(sims.length,"1");
            else
                inst.setValue(sims.length,"0");
            trainSet.add(inst);

        }

        try {
            mdl.buildClassifier(trainSet);
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

    private double[] calcAllSims(String name) {
        double[] sim = new double[keywords.size()];
        for(int i=0;i<sim.length;i++)
            sim[i] = BigramSimCalculator.calcBigramSim(keywords.get(i), name);
        Arrays.sort(sim);
        return sim;
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

    public double predict(Field fld){
        double[] sims = calcAllSimsUnique(fld.getName());
        Instance inst = new DenseInstance(keywordsUnique.size()+1);
        inst.setDataset(trainSet);
        for(int i=0;i<sims.length;i++)
            inst.setValue(i,sims[i]);
        double[] pred = null;
        try {
            pred = mdl.distributionForInstance(inst);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return(pred[1]);
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getKeywordsUnique() {
        return keywordsUnique;
    }
}
