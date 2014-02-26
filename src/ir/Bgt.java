package ir;


public class Bgt extends BranchInstruction {

    public Bgt( BasicBlock target) {
        super(Opcode.bgt, target);
    }
}
