package ir.instructions;

import java.util.ArrayList;

import ir.Instruction;
import ir.NaryInstruction;
import ir.Opcode;
import support.Identifier;

public class Call extends NaryInstruction {
    private Identifier functionName;
    public Call( Identifier function, ArrayList<Instruction> args) {
        super(Opcode.call, args);
	functionName = function;
    }
}
