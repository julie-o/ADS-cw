package ed.inf.adbs.minibase.utils;

import ed.inf.adbs.minibase.base.Constant;
import ed.inf.adbs.minibase.base.IntegerConstant;
import ed.inf.adbs.minibase.base.StringConstant;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Class for storing information about the database. The class uses a singleton pattern.
 */
public class DatabaseCatalog {
    /** Class instance */
    private static DatabaseCatalog catalog = null;
    /** Path to database as String */
    private String databaseDir;

    /** Map storing the schemas. The key is a table name and the value is a
     * list of Constant subclass types.*/
    private Map<String, List<Class<? extends Constant>> > schemas;

    /**
     * Method for creating and retrieving the class instance.
     *
     * @param databaseDir path to the database
     * @return returns the class instance
     */
    public static DatabaseCatalog getCatalog(String databaseDir) {
        if (catalog == null) {
            catalog = new DatabaseCatalog();
            catalog.schemas = new HashMap<>();
            parseSchema(databaseDir + "/schema.txt");
            catalog.databaseDir = databaseDir;
        }
        return catalog;
    }

    /**
     * Method for retrieving the class instance without passing in a path. Requires
     * the class to have been initialised beforehand.
     *
     * @return returns the class instance
     */
    public static DatabaseCatalog getCatalog() {
        if (catalog == null) throw new RuntimeException("DatabaseCatalog not initialised");
        return catalog;
    }

    /**
     * Private method for parsing the schema file. Used by getCatalog() when initialising
     * the class instance. Stores the schemas to the private 'schemas' variable.
     *
     * @param path path to the schema file
     */
    private static void parseSchema(String path){
        BufferedReader reader;
        try{
            reader = new BufferedReader(new FileReader(path));
            String line = reader.readLine();

            while (line != null) {
                List<String> getParts = new ArrayList<>(Arrays.asList(line.split(" ")));
                String name = getParts.get(0);
                List<Class<? extends Constant>> types = new ArrayList<>();
                for (int i = 1;i<getParts.size();i++){
                    String type = getParts.get(i);
                    if (type.equals("int")){
                        types.add(IntegerConstant.class);
                    } else if (type.equals("string")){
                        types.add(StringConstant.class);
                    } else {
                        throw new IllegalArgumentException("Illegal type in schema");
                    }
                }
                (catalog.schemas).put(name,types);
                line = reader.readLine();
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method for retrieving the schema of a particular table.
     *
     * @param schemaName name of the table
     * @return returns the schema as a list of Constant subclass types
     */
    public List<Class<? extends Constant>> getSchema(String schemaName){
        return schemas.get(schemaName);
    }

    /**
     * Method for retrieving the path to the database.
     *
     * @return returns the path to the database as a String
     */
    public String getDirectory(){
        return databaseDir;
    }
}
