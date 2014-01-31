package ir.instructions;

import ir.NoArgInstruction;
import ir.Opcode;

public class Read extends NoArgInstruction {

    public Read(int num) {
        super(num, Opcode.read);
    }

}
