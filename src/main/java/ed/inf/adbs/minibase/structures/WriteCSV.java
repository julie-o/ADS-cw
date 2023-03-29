package ed.inf.adbs.minibase.structures;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WriteCSV {
    private static WriteCSV writeCSV = null;
    String outputPath;
    File file;
    FileWriter writer;

    public static WriteCSV getWriteCSV(String outputPath) {
        if (writeCSV==null){
            writeCSV.writeCSV = new WriteCSV();
            writeCSV.outputPath = outputPath;

            writeCSV.file = new File(outputPath);
            try {
                writeCSV.file.createNewFile();
                writeCSV.writer = new FileWriter(outputPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return writeCSV;
        } else {
            return writeCSV;
        }
    }

    public static WriteCSV getWriteCSV() {
        return writeCSV;
    }

    public void closeWriter() throws IOException {
        writer.close();
        writeCSV = null;
    }

    public void write(Tuple tuple){
        try {
            writer.write(tuple.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
