package ir;


public class Ret extends UnaryInstruction {

    public Ret(Value arg) {
        super(Opcode.ret, arg);
    }

    public Ret() {
        // don't want null values, so use a named value stand in
        super(Opcode.ret, new NamedValue("")); 
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
