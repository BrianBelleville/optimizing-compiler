package ir.instructions;

import ir.BasicBlock;
import ir.BinaryInstruction;
import ir.Instruction;
import ir.Opcode;

public class Add extends BinaryInstruction {

    public Add(int num, BasicBlock b, Instruction a1, Instruction a2) {
        super(num, b, Opcode.add, a1, a2);
    }

}

