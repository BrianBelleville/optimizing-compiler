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

    public Boolean equals(Identifier i) {
	return table == i.table && table_index == i.table_index;
    }
}
