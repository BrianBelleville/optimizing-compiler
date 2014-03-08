package ir;

import support.Identifier;

// This instruction will fetch a function argument from wherever it
// will be acording to the calling convention, and will put it into a
// register acording to the register allocation. This makes handling
// function calls simpler.
public class Fetch extends UnaryInstruction {
    private int position;
    public Fetch(Identifier var, int position) {
        super(Opcode.fetch, new NamedValue(var.getString()));
        this.position = position;
    }

    @Override
    public boolean isCommonSubexpression(Instruction i) {
	return false;		// can never be common subexpressions
    }

    @Override
    public void performCSE() {
	return;			// no op, can never be common subexpressions
    }

    public int getPosition() {
        return position;
    }
}

