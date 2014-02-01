package ir.instructions;

import ir.Instruction;
import ir.Opcode;

public class Write extends ir.UnaryInstruction {

    public Write(Instruction arg) {
        super(Opcode.write, arg);
    }
}
