package ed.inf.adbs.minibase;

import ed.inf.adbs.minibase.base.Atom;
import ed.inf.adbs.minibase.base.Query;
import ed.inf.adbs.minibase.base.Head;
import ed.inf.adbs.minibase.parser.QueryParser;
import ed.inf.adbs.minibase.utils.QueryPlan;
import ed.inf.adbs.minibase.utils.WriteCSV;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;



/**
 * In-memory database system
 *
 */
public class Minibase {

    public static void main(String[] args) {

        if (args.length != 3) {
            System.err.println("Usage: Minibase database_dir input_file output_file");
            return;
        }

        String databaseDir = args[0];
        String inputFile = args[1];
        String outputFile = args[2];

        evaluateCQ(databaseDir, inputFile, outputFile);
    }

    /**
     * Method for starting the query evaluation. First parses the query and initialises
     * the query planner and file writer, and then peforms the query by calling dump()
     * to print the results to file.
     *
     * @param databaseDir path to database directory
     * @param inputFile path to input file
     * @param outputFile path to output file
     */
    public static void evaluateCQ(String databaseDir, String inputFile, String outputFile) {
        try {
            Query query = QueryParser.parse(Paths.get(inputFile));
            QueryPlan plan = QueryPlan.getQueryPlan(databaseDir,query);
            WriteCSV writer = WriteCSV.getWriteCSV(outputFile);

            plan.getRoot().dump();

            writer.closeWriter();
        } catch (IOException ioe) {
            ioe.printStackTrace();
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
            // Query query = QueryParser.parse("Q(x, y) :- R(x, z), S(y, z, w), z < w");
            // Query query = QueryParser.parse("Q(SUM(x * 2 * x)) :- R(x, 'z'), S(4, z, w), 4 < 'test string' ");

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
