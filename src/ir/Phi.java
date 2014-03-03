package ir;

import java.io.Writer;
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

    public void updateArgument(Value old, Value newVal, int branch)
        throws Exception {
        if(branch < 1 || branch > 2) {
            throw new Exception("Incorrect branch number");
        }
        if(branch == 1) {
            if(!getArg1().equals(old)) {
                throw new Exception("Old value does not match the Phi argument");
            }
            setArg1(newVal);
        }
        else if(branch == 2) {
            if(!getArg2().equals(old)) {
                throw new Exception("Old value does not match the Phi argument");
            }
            setArg2(newVal);
        }
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
}
