package support;

import ir.BasicBlock;
import ir.NamedValue;
import ir.Value;

public abstract class Type {
    public abstract void assignValue(Designator d, BasicBlock cur, BasicBlock join, Environment env, Value newVal)
        throws Exception;
    public abstract Value getValue(Designator d, BasicBlock cur, Environment env)
        throws Exception;
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
