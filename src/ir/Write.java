package ir;


public class Write extends ir.UnaryInstruction {

    public Write(Value arg) {
        super(Opcode.write, arg);
    }

    @Override
    public boolean isCommonSubexpression(Instruction i) {
	return false;		// can never be common subexpressions
    }

    @Override
    public void performCSE() {
	return;			// no op, can never be common subexpressions
    }

    @Override
    public boolean isDeadCode() {
        return false;           // can not be dead code
    }

    @Override
    public boolean needsRegister() {
        return false;
    }

}
