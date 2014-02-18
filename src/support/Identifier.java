package support;

public class Identifier {
    private int table_index;
    private IdentifierTable table;

    public Identifier(int index, IdentifierTable table) {
        table_index = index;
        this.table = table;
    }

    public String getString() {
        return table.getString(table_index);
    }

    public boolean equals(Identifier i) {
        return i != null && this.table == i.table && this.table_index == i.table_index;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Identifier) {
            return this.equals((Identifier)o);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new Integer(table_index).hashCode();
    }
}
