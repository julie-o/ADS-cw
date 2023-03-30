package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.minimizer.MinimizerUtils;
import ed.inf.adbs.minibase.parser.QueryParser;

import java.nio.file.Paths;
import java.util.*;
import java.io.FileWriter;
import java.io.File;


/**
 *
 * Minimization of conjunctive queries
 *
 */
public class CQMinimizer {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: CQMinimizer input_file output_file");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        parsingExample(inputFile);

        minimizeCQ(inputFile, outputFile);
    }

    /**
     * CQ minimization procedure
     *
     * The main loop iterates until no more changes are made. If removing an atom
     * results in a query homomorphism, the atom is removed from the original query
     * and a new iteration starts.
     */
    public static void minimizeCQ(String inputFile, String outputFile) {
        try {
            Query query = QueryParser.parse(Paths.get(inputFile));
            boolean change = true;

            while(change){
                change = false;
                for (Atom atom: query.getBody()){
                    if (MinimizerUtils.homomorphism(query,(RelationalAtom) atom)){
                        query.getBody().remove(atom);
                        change = true;
                        break;
                    }
                }
            }

            // Write to file
            try{
                File file = Paths.get(outputFile).toFile();
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write(query.toString() + "\n");
                writer.close();
            } catch (Exception e) {
                System.err.println("Exception occurred while writing to file.");
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
    }

    /**
     * Example method for getting started with the parser.
     * Reads CQ from a file and prints it to screen, then extracts Head and Body
     * from the query and prints them to screen.
     */
    public static void parsingExample(String filename) {

        try {
            Query query = QueryParser.parse(Paths.get(filename));
            // Query query = QueryParser.parse("Q(x, y) :- R(x, z), S(y, z, w)");
            // Query query = QueryParser.parse("Q(x) :- R(x, 'z'), S(4, z, w)");

            System.out.println("Entire query: " + query);
            Head head = query.getHead();
            System.out.println("Head: " + head);
            List<Atom> body = query.getBody();
            System.out.println("Body: " + body);
        }
        catch (Exception e)
        {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
    }

}
