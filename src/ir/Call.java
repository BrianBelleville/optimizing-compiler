package ir;

import java.util.ArrayList;
import java.io.Writer;
import ir.base.Instruction;
import ir.base.NaryInstruction;
import ir.base.Opcode;
import ir.base.Value;
import support.Identifier;

public class Call extends NaryInstruction {
    private Identifier functionName;
    public Call( Identifier function, ArrayList<Value> args) {
        super(Opcode.call, args);
	functionName = function;
    }

    @Override
    public void printInstruction(Writer w) throws Exception {
	w.write(Integer.toString(getNumber()) + ": " + getOpcode().toString());
	w.write(" " + functionName.getString());
	ArrayList<Value> args = getArguments();
	for(Value v : args) {
	    v.printAsArg(w);
	}
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
