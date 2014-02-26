package ir;


public class Ble extends BranchInstruction {

    public Ble( BasicBlock target) {
        super(Opcode.ble, target);
    }
}
