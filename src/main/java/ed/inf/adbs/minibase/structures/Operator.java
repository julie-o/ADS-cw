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
                System.out.println(nextTuple);
                nextTuple = getNextTuple();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
