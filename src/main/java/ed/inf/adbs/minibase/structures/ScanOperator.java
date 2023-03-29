package ed.inf.adbs.minibase.structures;

import ed.inf.adbs.minibase.base.Constant;
import ed.inf.adbs.minibase.base.IntegerConstant;
import ed.inf.adbs.minibase.base.RelationalAtom;
import ed.inf.adbs.minibase.base.StringConstant;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScanOperator extends Operator {
    private BufferedReader reader;
    private String relationName;
    private final String path;
    private List<Class<? extends Constant>> schema;
    private RelationalAtom atom;

    public ScanOperator(String fileName, RelationalAtom atom) throws FileNotFoundException {
        DatabaseCatalog catalog = DatabaseCatalog.getCatalog();
        if (catalog.getSchema(fileName)==null) {
            throw new IllegalArgumentException("Filename does not exist in schema");
        } else {
            this.schema = catalog.getSchema(fileName);
        }
        this.path = catalog.getDirectory() + "/files/" + fileName + ".csv";
        this.reader = new BufferedReader(new FileReader(this.path));
        this.relationName = fileName;
        this.atom = atom;
    }

    @Override
    public Tuple getNextTuple() throws IOException {
        String nextLine = reader.readLine();
        if (nextLine == null) {
            return null;
        } else {
            return this.parse(nextLine);
        }
    }

    @Override
    public void reset() {
        try {
            this.reader = new BufferedReader(new FileReader(this.path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // parse a line from the file into a Tuple object
    private Tuple parse(String line) {
        List<Constant> fields = new ArrayList<>();

        List<String> getParts = new ArrayList<>(Arrays.asList(line.split(",")));

        if (getParts.size()!=schema.size()) throw new IllegalArgumentException("Number of fields in file does not match schema");

        for (int i = 0;i<getParts.size();i++){
            Class<? extends Constant> type = schema.get(i);
            String value = getParts.get(i).trim(); // trim removes whitespace before and after
            Constant constant;

            if (type==StringConstant.class && value.charAt(0)=='\''){
                if (value.charAt(value.length() - 1)!='\''){
                    throw new IllegalArgumentException("String is not enclosed properly with \' ");
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

    public RelationalAtom getAtom(){
        return this.atom;
    }
}
