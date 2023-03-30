package ed.inf.adbs.minibase.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Method for encapsulating the FileWriter. Allows us to keep the writer open so
 * that each tuple can be written immediately when it is dumped. Uses a singleton
 * pattern and can be reset to null.
 */
public class WriteCSV {
    /** Singleton instance of the class */
    private static WriteCSV writeCSV = null;

    /** Output file */
    private File file;
    /** The open FileWriter */
    private FileWriter writer;

    /**
     * Method for initiating and getting an instance of the class.
     *
     * @param outputPath output file
     * @return returns an instance of the class
     */
    public static WriteCSV getWriteCSV(String outputPath) {
        if (writeCSV==null){
            writeCSV = new WriteCSV();
            writeCSV.file = new File(outputPath);
            try {
                writeCSV.file.createNewFile();
                writeCSV.writer = new FileWriter(outputPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return writeCSV;
    }

    /**
     * Method for getting an instance of the class without passing in the
     * output file. Requires the class instance to have been instantiated.
     *
     * @return returns the class instance
     */
    public static WriteCSV getWriteCSV() {
        if (writeCSV==null) throw new NullPointerException("WriteCSV instance has not been instantiated.");
        return writeCSV;
    }

    /**
     * Method for closing the FileWriter and resetting the class instance.
     */
    public void closeWriter() {
        try {
            writer.close();
        } catch (IOException e){
            e.printStackTrace();
        }
        writeCSV = null;
    }

    /**
     * Method for writing a tuple to the file.
     *
     * @param tuple tuple that will be written to file
     */
    public void write(Tuple tuple){
        try {
            writer.write(tuple.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
