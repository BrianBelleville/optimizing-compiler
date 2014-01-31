package ir.instructions;

import ir.BasicBlock;
import ir.BinaryInstruction;
import ir.Instruction;
import ir.Opcode;

public class Sub extends BinaryInstruction {

    public Sub(int num, BasicBlock b, Instruction a1, Instruction a2) {
        super(num, b, Opcode.sub, a1, a2);
    }

}
