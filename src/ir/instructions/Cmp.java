package ir.instructions;

import ir.BasicBlock;
import ir.BinaryInstruction;
import ir.Instruction;
import ir.Opcode;

public class Cmp extends BinaryInstruction {

    public Cmp(BasicBlock b, Instruction a1, Instruction a2) {
        super(b, Opcode.cmp, a1, a2);
    }

}
