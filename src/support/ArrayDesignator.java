package support;

import ir.base.Value;
import java.util.ArrayList;

public class ArrayDesignator extends Designator {
    private ArrayList<Value> index;

    public ArrayDesignator(Identifier varName, ArrayList<Value> index) {
        super(varName);
        this.index = index;
    }
    public ArrayList<Value> getIndex() {
        return index;
    }
}
