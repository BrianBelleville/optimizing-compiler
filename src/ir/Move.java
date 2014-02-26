package ir;


public class Move extends UnaryInstruction {

    public Move(Value arg) {
        super(Opcode.move, arg);
    }

    @Override
    public boolean isCommonSubexpression(Instruction i) {
	return false;		// can never be common subexpressions
    }

    @Override
    public void performCSE() {
        // no op, can never be common subexpressions, moves should be
        // deleted before cse anyways, but will still be traversed
	return;
    }
}
