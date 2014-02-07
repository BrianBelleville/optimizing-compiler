package ir.instructions;

import ir.Value;
import ir.Opcode;

public class Write extends ir.UnaryInstruction {

    public Write(Value arg) {
        super(Opcode.write, arg);
    }
}
