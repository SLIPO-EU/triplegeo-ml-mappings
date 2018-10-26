
public class Main {

    public static void main(String[] args){

        FieldMatcher fm = new FieldMatcher();
        fm.readAll();
        fm.trainClassifiers();
        fm.evaluate();

    }

}
