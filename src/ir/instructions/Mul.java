package ir.instructions;

import ir.BasicBlock;
import ir.BinaryInstruction;
import ir.Instruction;
import ir.Opcode;

public class Mul extends BinaryInstruction {

    public Mul(int num, BasicBlock b, Instruction a1, Instruction a2) {
        super(num, b, Opcode.mul, a1, a2);
    }

}
