package support;

import java.util.ArrayList;

public class IdentifierTable {
    private ArrayList<String> string_tab;
    public IdentifierTable() {
        string_tab = new ArrayList<String>();
    }

    public String getString(int table_index) {
        return string_tab.get(table_index);
    }
    public Identifier addToTable(String s) {
        int index = string_tab.indexOf(s);
        if(index == -1) {
            index = string_tab.size();
            string_tab.add(s);
        }
        return new Identifier(index, this);
    }
}
