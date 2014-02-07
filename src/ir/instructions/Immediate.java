package ir.instructions;

import ir.Value;

public class Immediate extends Value {
    private int value;

    public Immediate(int value){
	this.value = value;
    }

    public int getValue() {
	return value;
    }
}
