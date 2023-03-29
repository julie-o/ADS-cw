package ed.inf.adbs.minibase.structures;

import ed.inf.adbs.minibase.base.Constant;

import java.util.List;
import java.util.Objects;

/*
* Class for database tuples
* */
public class Tuple {
    private List<Constant> fields;

    public Tuple(List<Constant> fields) {
        this.fields = fields;
    }

    public List<Constant> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        String s = "";
        for (Constant c:fields){
            s = s + c.toString() + ",";
        }
        return s.substring(0, s.length() - 1); // drops last comma
    }

    @Override
    public int hashCode() {
        return fields.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Tuple){
            Tuple comp = (Tuple) o;
            return this.fields.equals(comp.getFields());
        }
        return false;
    }
}
