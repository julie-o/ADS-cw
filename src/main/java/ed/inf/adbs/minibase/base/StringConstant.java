package ed.inf.adbs.minibase.base;

import java.util.Objects;

public class StringConstant extends Constant implements Comparable<StringConstant> {
    private String value;

    public StringConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "'" + value + "'";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof StringConstant)) return false;

        return (this.getValue()).equals(((StringConstant) o).getValue());
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public int compareTo(StringConstant o) {
        return this.getValue().compareTo(o.getValue());
    }
}