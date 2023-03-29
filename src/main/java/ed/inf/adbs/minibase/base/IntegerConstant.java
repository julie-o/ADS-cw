package ed.inf.adbs.minibase.base;

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
        if (o == this) return true;
        if (!(o instanceof IntegerConstant)) return false;

        return (this.getValue()).equals(((IntegerConstant) o).getValue());
    }

    @Override
    public int compareTo(IntegerConstant o) {
        return this.getValue().compareTo(o.getValue());
    }
}
