package ir.instructions;

import ir.BinaryInstruction;
import ir.Instruction;
import ir.Opcode;

public class Sub extends BinaryInstruction {

    public Sub(Instruction a1, Instruction a2) {
        super(Opcode.sub, a1, a2);
    }

}
