package ir.instructions;

import ir.BasicBlock;
import ir.BinaryInstruction;
import ir.Instruction;
import ir.Opcode;

public class Mul extends BinaryInstruction {

    public Mul(BasicBlock b, Instruction a1, Instruction a2) {
        super(b, Opcode.mul, a1, a2);
    }

}
