package cf.grcq.processor.database;

public class Filter {

    public final String o1;
    public final Object o2;

    protected Filter(String o1, Object o2) {
        this.o1 = o1;
        this.o2 = o2;
    }

    public static Filter equals(String o1, Object o2) {
        return new Filter(o1, o2);
    }

}
