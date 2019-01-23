import java.io.IOException;
import java.util.ArrayList;

/*Class that exposes the functionality of FieldMatcher. It is is called in the following two ways:

FieldMatcherExec.jar -train folder_in models
FieldMatcherExec.jar -predict models file_in.csv predictions out

-train receives a folder with csv files and their yaml mappings, and produces a file with serialized models
-predict receives a file with serialized models and a csv and produces a human readable text file with mappings, which is read by triple-geo */
public class FieldMatcherExec {

    public static void main(String[] args) throws IOException {


        if(args[1].equals("-train")){
            FieldMatcher fm = new FieldMatcher();
            fm.readAllFromFolder(args[2]);
            fm.trainClassifiers();
            writeFMToFile(fm,args[3]);
        }

        if(args[1].equals("-test")) {
            FieldMatcher fm = readFMFromFile(args[2]);
            fm.giveMatchings(args[3],args[4]);
        }

    }

    private static FieldMatcher readFMFromFile(String path) {
        return null;
    }

    private static void writeFMToFile(FieldMatcher fm, String path) {

    }
}
