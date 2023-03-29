package ed.inf.adbs.minibase.base;

import ed.inf.adbs.minibase.Utils;

import java.util.List;
import java.util.Objects;

public class RelationalAtom extends Atom {
    private String name;

    private List<Term> terms;

    public RelationalAtom(String name, List<Term> terms) {
        this.name = name;
        this.terms = terms;
    }

    public String getName() {
        return name;
    }

    public List<Term> getTerms() {
        return terms;
    }

    @Override
    public String toString() {
        return name + "(" + Utils.join(terms, ", ") + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelationalAtom that = (RelationalAtom) o;
        return Objects.equals(name, that.name) && Objects.equals(terms, that.terms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, terms);
    }
}
