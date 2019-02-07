package eu.slipo.triplegeo.ml.mappings;
import java.io.*;

//class with main to run from IDE
public class FieldMatcherExec {

    public static void main(String[] args) throws Exception {

    //FieldMatcher fm = new FieldMatcher();
    //fm.makeModels(args[0],args[1]);

    FieldMatcher fm = readFMFromFile(args[0]);
    Mappings maps = fm.giveMatchings(args[1]);

    }

    private static FieldMatcher readFMFromFile(String path) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(path);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);
        FieldMatcher fm = (FieldMatcher)objectInputStream.readObject();
        objectInputStream.close();
        fileInputStream.close();
        return fm;
    }


}
