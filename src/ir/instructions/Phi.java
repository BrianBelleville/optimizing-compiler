package ir.instructions;

import ir.BasicBlock;
import ir.BinaryInstruction;
import ir.Instruction;
import ir.Opcode;

public class Phi extends BinaryInstruction {

    public Phi(int num, BasicBlock b, Instruction a1, Instruction a2) {
        super(num, b, Opcode.phi, a1, a2);
    }

}
