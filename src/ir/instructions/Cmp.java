package ir.instructions;

import ir.BinaryInstruction;
import ir.Instruction;
import ir.Opcode;

public class Cmp extends BinaryInstruction {

    public Cmp(Instruction a1, Instruction a2) {
        super(Opcode.cmp, a1, a2);
    }

}
