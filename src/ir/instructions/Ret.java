package ir.instructions;

import ir.Instruction;
import ir.Opcode;
import ir.UnaryInstruction;

public class Ret extends UnaryInstruction {

    public Ret(Instruction arg) {
        super(Opcode.ret, arg);
    }
}
