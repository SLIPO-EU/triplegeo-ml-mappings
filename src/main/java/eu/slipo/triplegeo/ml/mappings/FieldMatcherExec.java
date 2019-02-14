package eu.slipo.triplegeo.ml.mappings;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class FieldMatcherExec {

    public static void main(String[] args) throws Exception {
        // Configure options
        Options options = new Options();

        options.addOption("d", "data", true, "folder with csv data and yml mapping files");
        options.addOption("o", "output", true, "model output filename");
        options.addOption("m", "model", true, "the model to use for computing field mappings");
        options.addOption("f", "file", true, "input file for computing field mappings");

        // Check arguments
        if (args.length == 0) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("field-matcher", options);
        }

        // Parse arguments
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        // Execute
        if ((cmd.hasOption("d")) && (cmd.hasOption("o"))) {
            // Train
            FieldMatcher fm = new FieldMatcher();
            fm.makeModels(cmd.getOptionValue("d"), cmd.getOptionValue("o"));
        } else if ((cmd.hasOption("m")) && (cmd.hasOption("f"))) {
            // Suggest
            FieldMatcher fm = readFMFromFile(cmd.getOptionValue("m"));
            Mappings mappings = fm.giveMatchings(cmd.getOptionValue("f"));
            for (String field : mappings.maps.keySet()) {
                System.out.printf(" * %-20s %n", field);
                for (String key : mappings.maps.get(field).keySet()) {
                    System.out.printf("   %-20s %-20s %n", key, mappings.maps.get(field).get(key));
                }
            }
        }

    }

    private static FieldMatcher readFMFromFile(String path) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(path);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);
        FieldMatcher fm = (FieldMatcher) objectInputStream.readObject();
        objectInputStream.close();
        fileInputStream.close();
        return fm;
    }

}
