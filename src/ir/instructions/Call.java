package ir.instructions;

import java.util.ArrayList;
import ir.NaryInstruction;
import ir.Opcode;
import ir.Value;
import support.Identifier;

public class Call extends NaryInstruction {
    private Identifier functionName;
    public Call( Identifier function, ArrayList<Value> args) {
        super(Opcode.call, args);
	functionName = function;
    }
}
