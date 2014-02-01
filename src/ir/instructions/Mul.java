package ir.instructions;

import ir.BinaryInstruction;
import ir.Instruction;
import ir.Opcode;

public class Mul extends BinaryInstruction {

    public Mul(Instruction a1, Instruction a2) {
        super(Opcode.mul, a1, a2);
    }

}
