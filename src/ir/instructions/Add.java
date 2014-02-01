package ir.instructions;

import ir.BinaryInstruction;
import ir.Instruction;
import ir.Opcode;

public class Add extends BinaryInstruction {

    public Add(Instruction a1, Instruction a2) {
        super(Opcode.add, a1, a2);
    }

}

