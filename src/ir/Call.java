package ir;

import java.util.ArrayList;

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
}
