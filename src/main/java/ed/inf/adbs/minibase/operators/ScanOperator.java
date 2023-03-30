package ed.inf.adbs.minibase.operators;

import ed.inf.adbs.minibase.base.Constant;
import ed.inf.adbs.minibase.base.IntegerConstant;
import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.base.StringConstant;
import ed.inf.adbs.minibase.utils.DatabaseCatalog;
import ed.inf.adbs.minibase.utils.Tuple;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class for scan operations. The constructor (or reset) sets the reader to the start
 * of the file. Then as getNextTuple() is called parse() is used to parse a line from file.
 */
public class ScanOperator extends Operator {
    /** Reader for reading the database files line by line */
    private BufferedReader reader;
    /** Path to the file that stores the table that will be scanned */
    private final String path;
    /** Schema for the table that will be scanned */
    private final List<Class<? extends Constant>> schema;
    /** The base Relational Atom that "prompted" the file scan */
    private final RelationalAtom atom;

    /**
     * Constructor for the ScanOperator class.
     *
     * @param fileName name of the relation
     * @param atom base RelationalAtom
     * @throws FileNotFoundException if there is no file with the given relation name
     */
    public ScanOperator(String fileName, RelationalAtom atom) throws FileNotFoundException {
        DatabaseCatalog catalog = DatabaseCatalog.getCatalog();
        if (catalog.getSchema(fileName)==null) {
            throw new IllegalArgumentException("Filename does not exist in schema");
        } else {
            this.schema = catalog.getSchema(fileName);
        }
        this.path = catalog.getDirectory() + "/files/" + fileName + ".csv";
        this.reader = new BufferedReader(new FileReader(this.path));
        this.atom = atom;
    }

    /**
     * Method for reading the next tuple from the file.
     *
     * @return returns a tuple from the database
     * @throws IOException throws an error if reading from file was unsuccessful
     */
    @Override
    public Tuple getNextTuple() throws IOException {
        String nextLine = reader.readLine();
        if (nextLine == null) {
            return null;
        } else {
            return parse(nextLine);
        }
    }

    /**
     * Method for parsing the string read from file into a Tuple object.
     *
     * @param line string as read from file
     * @return returns a Tuple object
     */
    private Tuple parse(String line) {
        // fields in the tuple
        List<Constant> fields = new ArrayList<>();

        // input string split by commas
        List<String> getParts = new ArrayList<>(Arrays.asList(line.split(",")));

        if (getParts.size()!=schema.size()) throw new IllegalArgumentException("Number of fields in file does not match schema");

        // iterate through comma split list and create objects of Constant subclasses
        for (int i = 0;i<getParts.size();i++){
            Class<? extends Constant> type = schema.get(i);
            String value = getParts.get(i).trim();
            Constant constant;

            if (type==StringConstant.class && value.charAt(0)=='\''){
                if (value.charAt(value.length() - 1)!='\''){
                    throw new IllegalArgumentException("String is not enclosed properly with ' ");
                }
                String strippedValue = value.substring(1, value.length() - 1);
                constant = new StringConstant(strippedValue);
            } else if (type==IntegerConstant.class) {
                try{
                    int num = Integer.parseInt(value);
                    constant = new IntegerConstant(num);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Value is not an integer");
                }
            } else {
                throw new IllegalArgumentException("Illegal type in schema");
            }
            fields.add(constant);
        }
        return new Tuple(fields);
    }

    /**
     * Method for resetting the ScanOperation. The next getNextTuple() call will start
     * reading at the beginning of the file.
     */
    @Override
    public void reset() {
        try {
            this.reader = new BufferedReader(new FileReader(this.path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method for retrieving the base RelationalAtom
     *
     * @return returns the base RelationalAtom
     */
    public RelationalAtom getAtom(){
        return this.atom;
    }
}
