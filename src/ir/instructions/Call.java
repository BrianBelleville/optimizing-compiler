package ir.instructions;

import java.util.ArrayList;

import ir.BasicBlock;
import ir.Instruction;
import ir.NaryInstruction;
import ir.Opcode;
import support.Identifier;

public class Call extends NaryInstruction {
    private Identifier functionName;
    public Call(BasicBlock b,  Identifier function, ArrayList<Instruction> args) {
        super(b, Opcode.call, args);
	functionName = function;
    }
}
