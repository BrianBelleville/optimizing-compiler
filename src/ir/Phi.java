package ir;

import java.io.Writer;
import ir.base.BinaryInstruction;
import ir.base.Instruction;
import ir.base.Opcode;
import ir.base.Value;
import support.Identifier;

public class Phi extends BinaryInstruction {
    private Identifier variable;
    public Phi(Identifier var, Value a1, Value a2) {
        super(Opcode.phi, a1, a2);
	variable = var;
    }

    public Identifier getVariable() {
	return variable;
    }

    @Override
    public void printInstruction(Writer w) throws Exception {
	super.printInstruction(w);
	w.write(" \\\"" + variable.getString() + "\\\"");
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
        // if the phi is unused, or if it chooses between the same
        // values, it isn't necessary.
        return super.isDeadCode() || getArg1().equals(getArg2());
    }        
    

}
