package ir;

import ir.base.Value;
import java.io.Writer;

public class Immediate extends Value {
    private int value;

    public Immediate(int value){
	this.value = value;
    }

    public int getValue() {
	return value;
    }

    @Override
    public boolean equals(Object o) {
	if(o instanceof Immediate) {
	    Immediate v = (Immediate)o;
	    return this.value == v.value;
	}
	return false;
    }

    @Override
    public int hashCode() {
	return new Integer(value).hashCode();
    }

    @Override
    public void printAsArg(Writer w) throws Exception {
	w.write(" #" + Integer.toString(value));
    }
}
