package ir.instructions;

import java.util.ArrayList;

import ir.BasicBlock;
import ir.Instruction;
import ir.NaryInstruction;
import ir.Opcode;

public class Call extends NaryInstruction {

    public Call(int num, BasicBlock b,  ArrayList<Instruction> args) {
        super(num, b, Opcode.call, args);
    }

}
