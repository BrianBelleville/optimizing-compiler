package ir.instructions;

import java.util.ArrayList;

import ir.BasicBlock;
import ir.Instruction;
import ir.NaryInstruction;
import ir.Opcode;

public class Call extends NaryInstruction {

    public Call(BasicBlock b,  ArrayList<Instruction> args) {
        super(b, Opcode.call, args);
    }
}
