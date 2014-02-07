package ir;

import ir.base.NoArgInstruction;
import ir.base.Opcode;

public class Read extends NoArgInstruction {

    public Read(){
        super(Opcode.read);
    }

}
