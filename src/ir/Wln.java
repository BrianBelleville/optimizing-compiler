package ir;


public class Wln extends NoArgInstruction {

    public Wln(){
        super(Opcode.wln);
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
