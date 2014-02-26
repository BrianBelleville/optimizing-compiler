package ir;


public class Read extends NoArgInstruction {

    public Read(){
        super(Opcode.read);
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
}
