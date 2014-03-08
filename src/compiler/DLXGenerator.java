package compiler;

// depends on the DLX simulator provided by the professor
import dlx.DLX;

import ir.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DLXGenerator extends CodeGenerator {
    private HashMap<BasicBlock, ArrayList<Integer>> fixup = new HashMap<BasicBlock, ArrayList<Integer>>();
    private HashMap<BasicBlock, Integer> labels = new HashMap<BasicBlock, Integer>();
    private ArrayList<Integer> code = new ArrayList<Integer>();
    private DLX dlx = new DLX();

    // definitions of special registers
    private static int retAddr = 31;
    private static int gbp = 30;
    private static int sp = 29;
    private static int fp = 28;

    // registers for spilled local variables
    private static int t1 = 27;
    private static int t2 = 26;

    // reuse for function return values
    private static int retVal = t1;

    // r0 is always 0
    private static int zero = 0;

    private static int registersAvailable = 8;

    private static int minAvail = zero + 1;
    private static int maxAvail = Math.max(t2 - 1, registersAvailable + minAvail - 1);


    @Override
    public int[] generate(ArrayList<Function> program) {
        int[] rval = new int[code.size()];
        for(int i = 0; i < code.size(); i++) {
            rval[i] = code.get(i);
        }
        return rval;
    }

    // position will determine if the 
    private int location(Value v, int position) {
        int reg = minAvail + v.getColor();
        
        if(reg > maxAvail) {
            // then this value has been spilled
            // todo: insert the code to fetch the spilled value
            return position == 1 ? t1 : t2;
        }
        return reg;
    }

    private int target(Value v) {
        int reg = maxAvail + v.getColor();
        if(reg > maxAvail) {
            // then this value has been spilled, will need to be put home later
            return t1;
        }
        return reg;
    }

    private void home(Value v) {
        int reg = maxAvail + v.getColor();
        if(reg > maxAvail) {
            // then this value has been spilled, the value is
            // currently in t1, insert the spill code now
        }
        // otherwise, do nothing, the value is home
    }

    private void emit(int instruction) {
        code.add(instruction);
    }

    private void emitCode(BasicBlock bb) throws Exception {
        for(Instruction i : bb.instructions) {
            switch(i.getOpcode()) {
            case add:
                {
                    Add s = (Add)i;
                    if(s.getArg1() instanceof Immediate) {
                        int c = ((Immediate)s.getArg1()).getValue();
                        emit(dlx.assemble(dlx.ADDI, target(s),
                                          location(s.getArg2()), c))
                            
                    } else if(s.getArg2() instanceof Immediate) {
                        int c = ((Immediate)s.getArg2()).getValue();
                        emit(dlx.assemble(dlx.ADDI, target(s),
                                          location(s.getArg1()), c));
                    } else {
                        emit(dlx.assemble(dlx.ADD, target(s),
                                          location(s.getArg1()),
                                          location(s.getArg2())));
                    }
                    home(s);                    
                }
                break;
            case sub:
                {
                    Sub s = (Sub)i;
                }
                break;
            case mul:
                {
                    Mul s = (Mul)i;
                }
                break;
            case div:
                {
                    Div s = (Div)i;
                }
                break;
            case cmp:
                {
                    Cmp s = (Cmp)i;
                }
                break;
            case adda:
                {
                     // no op, this will be incorperated into load
                }
                break;
            case load:
                {
                    Load s = (Load)i;
                }
                break;
            case store:
                {
                    Store s = (Store)i;
                }
                break;
            case move:
                {
                    Move s = (Move)i;
                }
                break;
            case phi:
                {
                    // no op, phis will have been resoved to moves by now...
                }
                break;
            case end:
                {
                    emit(dlx.assemble(dlx.RET, zero));
                }
                break;
            case bra:
                {
                    Bra s = (Bra)i;
                }
                break;
            case bne:
                {
                    Bne s = (Bne)i;
                }
                break;
            case beq:
                {
                    Beq s = (Beq)i;
                }
                break;
            case ble:
                {
                    Ble s = (Ble)i;
                }
                break;
            case blt:
                {
                    Blt s = (Blt)i;
                }
                break;
            case bge:
                {
                    Bge s = (Bge)i;
                }
                break;
            case bgt:
                {
                    Bgt s = (Bgt)i;
                }
                break;
            case read:
                {
                    Read s = (Read)i;
                    emit(dlx.assemble(dlx.RDI, target(s)));
                    home(s);
                }
                break;
            case write:
                {
                    Write s = (Write)i;
                }
                break;
            case wln:
                {
                    Wln s = (Wln)i;
                }
                break;
            case ret:
                {
                    Ret s = (Ret)i;
                }
                break;
            case call:
                {
                    Call s = (Call)i;
                }
                break;
            case fetch:
                {
                    Fetch s = (Fetch)i;
                }
                break;
            default:
                throw new Exception("Unrecognized ir opcode");
            }
        }
    }

    private void fixup(BasicBlock bb) throws Exception {
        ArrayList<Integer> fixupLocations = fixup.get(bb);
        if(fixupLocations == null) {
            return;
        }
        int address = labels.get(bb);
        for(Integer i : fixupLocations) {
            int instruction = code.get(i);
            // determine relative branch address
            int c = address - i;
            if(c < 0) c ^= 0xffff0000; // c will be sign extended from 16 bit value
            if((c & ~0xffff) != 0) {
                throw new Exception("Illegal operand for branch instruction");
            }
            instruction = (instruction & ~0xffff) | c;
            code.set(i, instruction); // replace instruction with the fixed up version
        }
    }
}
