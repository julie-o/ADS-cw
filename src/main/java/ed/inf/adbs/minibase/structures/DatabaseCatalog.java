package ed.inf.adbs.minibase.structures;

import ed.inf.adbs.minibase.base.Constant;
import ed.inf.adbs.minibase.base.IntegerConstant;
import ed.inf.adbs.minibase.base.StringConstant;

import javax.management.relation.Relation;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.*;

public class DatabaseCatalog {
    public static DatabaseCatalog catalog = null;

    private String databaseDir;
    private Set<String> relations;
    private Map<String, List<Class<? extends Constant>> > schemas;

    public static DatabaseCatalog getCatalog(String databaseDir) {
        if (catalog != null) {
            return catalog;
        } else {
            catalog = new DatabaseCatalog();
            databaseDir = databaseDir;

            catalog.schemas = new HashMap<>();
            parseSchema(databaseDir + "/schema.txt");

            catalog.databaseDir = databaseDir;

            return catalog;
        }
    }

    public static DatabaseCatalog getCatalog() {
        if (catalog == null) {
            throw new RuntimeException("Catalog not initialised, call constructor with directory");
        } else {
            return catalog;
        }
    }

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

    public List<Class<? extends Constant>> getSchema(String schemaName){
        return schemas.get(schemaName);
    }

    public String getDirectory(){
        return databaseDir;
    }

    public void addRelation(String name){
        relations.add(name);
    }
    public boolean hasRelation(String name){
        return relations.contains(name);
    }
}
