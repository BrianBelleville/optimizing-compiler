package ir;

import ir.base.Value;

import java.io.IOException;
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
    public void printAsArg(Writer w) throws IOException {
	w.write(" #" + Integer.toString(value));
    }
}
