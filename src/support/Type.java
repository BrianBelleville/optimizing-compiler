package support;

import ir.BasicBlock;
import ir.NamedValue;
import ir.Value;

public abstract class Type {
    public static int getWordSize() {
        return  4;  // assume 32 bit machine, may have to change this, or make it configurable
    }

    public abstract void assignValue(Designator d, BasicBlock cur, BasicBlock join, Environment env, Value newVal)
        throws Exception;
    public abstract Value getValue(Designator d, BasicBlock cur, Environment env)
        throws Exception;

    // the size of values of this type in bytes
    public abstract int getSize();

    public void freeze() {
        // default is nop
    }
    protected Value getFP() {
        return new NamedValue("FP");
    }
    protected Value getGBP() {
        return new NamedValue("GBP");
    }
}
