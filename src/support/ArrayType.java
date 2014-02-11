package support;

import ir.BasicBlock;
import ir.base.Value;
import java.util.ArrayList;

public class ArrayType extends Type {
    private ArrayList<Integer> dimension;
    private boolean global;

    public ArrayType(ArrayList<Integer> dimension, boolean global) {
        this.dimension = dimension;
        this.global = global;
    }

    @Override
    public void assignValue(Designator d, BasicBlock cur, BasicBlock join, Environment env, Value newVal)
        throws Exception {
        // emit store
    }

    @Override
    public Value getValue(Designator d, BasicBlock cur, Environment env)
        throws Exception {
        // emit load
        return null;
    }

    private void emitIndexingCode(BasicBlock cur, Environment env) {
        // emit the offset calculation and adda instruction
    }

}
