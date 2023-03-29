package ed.inf.adbs.minibase.structures;

import ed.inf.adbs.minibase.base.RelationalAtom;

import java.io.IOException;

public abstract class Operator {
    public abstract Tuple getNextTuple() throws IOException;

    public abstract void reset();

    public abstract RelationalAtom getAtom();

    public void dump() {
        try {
            Tuple nextTuple = getNextTuple();
            while (nextTuple != null) {
                if (WriteCSV.getWriteCSV()!=null){
                    WriteCSV writer = WriteCSV.getWriteCSV();
                    writer.write(nextTuple);
                } else {
                    System.out.print(nextTuple);
                }
                nextTuple = getNextTuple();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
