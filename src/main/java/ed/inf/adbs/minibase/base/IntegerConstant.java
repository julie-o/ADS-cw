package ed.inf.adbs.minibase.base;

import java.util.Objects;

public class IntegerConstant extends Constant implements Comparable<IntegerConstant> {
    private Integer value;

    public IntegerConstant(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntegerConstant that = (IntegerConstant) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int compareTo(IntegerConstant o) {
        return this.getValue().compareTo(o.getValue());
    }
}
