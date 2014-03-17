package compiler;

// depends on the DLX simulator provided by the professor
import dlx.DLX;

import ir.*;
import support.*;
import transform.AnalyzeRegistersUsed;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class DLXGenerator extends CodeGenerator {
    private HashMap<Object, ArrayList<Integer>> fixup = new HashMap<Object, ArrayList<Integer>>();
    private HashMap<Object, Integer> labels = new HashMap<Object, Integer>();
    private ArrayList<Integer> code = new ArrayList<Integer>();

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
    private static int maxAvail = Math.min(t2 - 1, registersAvailable + minAvail - 1);

    // stack to be used to keep track of the order that registers need
    // to be restored.
    private LinkedList<Integer> regStack = new LinkedList<Integer>();
    private Function currentFunction;
    @Override
    public int[] generate(ArrayList<Function> program) throws Exception {
        // the last function should be main
        if(!program.get(program.size() - 1).name.getString().equals("__MAIN__")) {
            throw new Exception("Main not found");
        }

        // first instructions will be setting the stack pointer and
        // frame pointer to point past the program code
        // will need to be fixed up once the code size is known
        emit(DLX.assemble(DLX.ADDI, sp, zero, 0));
        // fp <- sp
        emit(DLX.assemble(DLX.ADD, fp, zero, sp));

        boolean main = true;    // first function will be main
        for(int i = program.size() - 1; i >= 0; i--) {
            Function f = program.get(i);
            currentFunction = f;
            makeLabel(f.name);

            // see what registers will be saved and how many memory cells will be needed
            AnalyzeRegistersUsed analysis = new AnalyzeRegistersUsed(registersAvailable, retAddr - minAvail);
            f.entryPoint.runPass(analysis);
            f.locals.allocateMemoryCells(analysis.memNeeded);

            // clear the record of registers pushed onto stack from previous function
            regStack.clear();

            // set up stack frame for local variables
            if(!main) {
                push(fp);
                // fp <- sp
                emit(DLX.assemble(DLX.ADD, fp, zero, sp));
            }
            if(f.locals.getSize() > 0) {
                emit(DLX.assemble(DLX.ADDI, sp, sp, f.locals.getSize()));
            }

            if(!main) {
                // save registers onto stack
                for(Integer reg : analysis.registersUsed) {
                    int realReg = reg + minAvail;
                    regStack.push(realReg); // keep track of order registers are pushed
                    push(realReg);
                }
            }

            // emit code for the function body
            emitCode(f.entryPoint);
            main = false;       // after the first function, it's not main
        }

        // fix the first instruction now that the size of the text is
        // known.
        int spSet = code.get(0);
        spSet = setC(spSet, 4 * code.size());
        code.set(0, spSet);
        // copy the code into an int array
        int[] rval = new int[code.size()];
        for(int i = 0; i < code.size(); i++) {
            rval[i] = code.get(i);
        }
        // clear code in case we use this object to generate code for
        // multiple programs
        code.clear();
        return rval;
    }

    // position will determine if the value is put into t1 or t2
    private int location(Value v, int position) throws Exception {
        if(isSpilled(v)) {
            int target = position == 1 ? t1 : t2;
            // if this is a special spill case
            if(v.getColor() < 0) {
                if(!(v instanceof Fetch)) {
                    throw new Exception("No case to handle special spill");
                }
                int address = getFetchAddress((Fetch)v);
                emit(DLX.assemble(DLX.LDW, target, fp, address));
            } else {
                int address = getSpillAddress(v);
                emit(DLX.assemble(DLX.LDW, target, fp, address));
            }
            return target;
        }
        int reg = minAvail + v.getColor();
        return reg;
    }

    private int location1(Value v) throws Exception {
        return location(v, 1);
    }

    private int location2(Value v) throws Exception {
        return location(v, 2);
    }

    private int target(Value v) {
        if(isSpilled(v)) {
            // then this value has been spilled, will need to be put home later
            return t1;
        }
        int reg = minAvail + v.getColor();
        return reg;
    }

    private boolean isSpilled(Value v) {
        int reg = minAvail + v.getColor();
        return reg > maxAvail || v.getColor() < 0;
    }

    private int getSpillAddress(Value v) throws Exception {
        int color = v.getColor();
        return currentFunction.locals.getCellAddress(color - maxAvail);
    }

    private void home(Value v) throws Exception {
        if(isSpilled(v)) {
            // then this value has been spilled, the value is
            // currently in t1, insert the spill code now

            // if the color is less than 0, it is 'spilled', but
            // doesn't need to be put into a home since it is never
            // used
            if(v.getColor() < 0) {
                return;
            }

            int address = getSpillAddress(v);
            emit(DLX.assemble(DLX.STW, t1, fp, address));
        }
        // otherwise, do nothing, the value is home
    }

    private void emit(int instruction) {
        code.add(instruction);
    }

    private void push(int register) {
        emit(DLX.assemble(DLX.PSH, register, sp, Type.getWordSize()));
    }

    private void pop(int register) {
        emit(DLX.assemble(DLX.POP, register, sp, 0 - Type.getWordSize()));
    }

    private void emitCode(BasicBlock bb) throws Exception {
        makeLabel(bb);
        for(Instruction i : bb.instructions) {
            switch(i.getOpcode()) {
            case add:
                {
                    Add s = (Add)i;
                    if(s.getArg1() instanceof Immediate) {
                        int c = ((Immediate)s.getArg1()).getValue();
                        emit(DLX.assemble(DLX.ADDI, target(s),
                                          location1(s.getArg2()), c));

                    } else if(s.getArg2() instanceof Immediate) {
                        int c = ((Immediate)s.getArg2()).getValue();
                        emit(DLX.assemble(DLX.ADDI, target(s),
                                          location1(s.getArg1()), c));
                    } else {
                        emit(DLX.assemble(DLX.ADD, target(s),
                                          location1(s.getArg1()),
                                          location2(s.getArg2())));
                    }
                    home(s);
                }
                break;
            case sub:
                {
                    Sub s = (Sub)i;
                    if(s.getArg1() instanceof Immediate) {
                        // need to move arg1 value into t1
                        emit(DLX.assemble(DLX.ADDI, t1, zero, ((Immediate)s.getArg1()).getValue()));
                        emit(DLX.assemble(DLX.SUB, target(s), t1, location2(s.getArg2())));
                    } else if(s.getArg2() instanceof Immediate) {
                        int c = ((Immediate)s.getArg2()).getValue();
                        emit(DLX.assemble(DLX.SUBI, target(s),
                                          location1(s.getArg1()), c));
                    } else {
                        emit(DLX.assemble(DLX.SUB, target(s),
                                          location1(s.getArg1()),
                                          location2(s.getArg2())));
                    }
                    home(s);
                }
                break;
            case mul:
                {
                    Mul s = (Mul)i;
                    if(s.getArg1() instanceof Immediate) {
                        int c = ((Immediate)s.getArg1()).getValue();
                        emit(DLX.assemble(DLX.MULI, target(s),
                                          location1(s.getArg2()), c));

                    } else if(s.getArg2() instanceof Immediate) {
                        int c = ((Immediate)s.getArg2()).getValue();
                        emit(DLX.assemble(DLX.MULI, target(s),
                                          location1(s.getArg1()), c));
                    } else {
                        emit(DLX.assemble(DLX.MUL, target(s),
                                          location1(s.getArg1()),
                                          location2(s.getArg2())));
                    }
                    home(s);
                }
                break;
            case div:
                {
                    Div s = (Div)i;
                    if(s.getArg1() instanceof Immediate) {
                        // need to move arg1 value into t1
                        emit(DLX.assemble(DLX.ADDI, t1, zero, ((Immediate)s.getArg1()).getValue()));
                        emit(DLX.assemble(DLX.DIV, target(s), t1, location2(s.getArg2())));
                    } else if(s.getArg2() instanceof Immediate) {
                        int c = ((Immediate)s.getArg2()).getValue();
                        emit(DLX.assemble(DLX.DIVI, target(s),
                                          location1(s.getArg1()), c));
                    } else {
                        emit(DLX.assemble(DLX.DIV, target(s),
                                          location1(s.getArg1()),
                                          location2(s.getArg2())));
                    }
                    home(s);
                }
                break;
            case cmp:
                {
                    Cmp s = (Cmp)i;
                    // the comparison is statically determined,
                    // haven't implemented the control flow
                    // simplification that this is possible, so put
                    // the result of the comparison into a register
                    if(s.getArg1() instanceof Immediate && s.getArg2() instanceof Immediate) {
                        int r = ((Immediate)s.getArg1()).getValue() - ((Immediate)s.getArg2()).getValue();
                        if(r < 0) {
                            r = -1;
                        } else if(r > 0) {
                            r = 1;
                        } // don't need to do anything special if r == 0
                        emit(DLX.assemble(DLX.ADDI, t1, zero, r));
                    } else if (s.getArg1() instanceof Immediate) {
                        emit(DLX.assemble(DLX.ADDI, t1, zero, ((Immediate)s.getArg1()).getValue()));
                        emit(DLX.assemble(DLX.CMP, t1, t1, location2(s.getArg2())));
                    } else if (s.getArg2() instanceof Immediate) {
                        emit(DLX.assemble(DLX.CMPI, t1, location1(s.getArg1()), ((Immediate)s.getArg2()).getValue()));
                    } else {
                        emit(DLX.assemble(DLX.CMP, t1, location1(s.getArg1()), location2(s.getArg2())));
                    }
                }
                break;
            case adda:
                {
                    // no op, this will be incorperated into load/store
                }
                break;
            case load:
                {
                    Load s = (Load)i;
                    Adda address = (Adda)s.getArg(); // will always be Adda
                    emitLoadOrStore(DLX.LDX, target(s), address);
                    // todo: if loads are spilled, just access it from
                    // its location in memory when it is used
                    home(s);
                }
                break;
            case store:
                {
                    Store s = (Store)i;
                    Adda address = (Adda)s.getArg1(); // will always be Adda
                    Value v = s.getArg2();
                    if(v instanceof Immediate) {
                        emit(DLX.assemble(DLX.ADDI, t1, zero, ((Immediate)v).getValue()));
                        emitLoadOrStore(DLX.STX, t1, address);
                    } else {
                        emitLoadOrStore(DLX.STX, location1(s.getArg2()), address);
                    }
                }
                break;
            case move:
                {
                    // todo: Need to be able to handle the
                    // case where it is an anonymous temporary
                    // introduced to break phi cycles.
                    Move s = (Move)i;
                    if(s.getArg() instanceof Immediate) {
                        emit(DLX.assemble(DLX.ADDI, target(s),
                                          zero, ((Immediate)s.getArg()).getValue()));
                        home(s);
                    } else {
                        // move is only needed if the value isn't
                        // already in the correct register, or if the
                        // value is actually going to be used
                        if((s.getColor() != s.getArg().getColor()) && (s.getColor() >= 0)) {
                            if(isSpilled(s)) {
                                int spillAddress = getSpillAddress(s);
                                emit(DLX.assemble(DLX.STW, location1(s.getArg()), fp, spillAddress));
                            }
                            emit(DLX.assemble(DLX.ADD, target(s), zero, location2(s.getArg())));
                            home(s);
                        }
                    }

                }
                break;
            case phi:
                {
                    // no op, phis will have been resoved to moves by now...
                }
                break;
            case end:
                {
                    emit(DLX.assemble(DLX.RET, zero));
                }
                break;
            case bra:
                {
                    Bra s = (Bra)i;
                    emit(DLX.assemble(DLX.BEQ, zero, getBranchOffset(s.getTarget())));
                }
                break;
            case bne:
                {
                    Bne s = (Bne)i;
                    emit(DLX.assemble(DLX.BNE, t1, getBranchOffset(s.getTarget())));
                }
                break;
            case beq:
                {
                    Beq s = (Beq)i;
                    emit(DLX.assemble(DLX.BEQ, t1, getBranchOffset(s.getTarget())));
                }
                break;
            case ble:
                {
                    Ble s = (Ble)i;
                    emit(DLX.assemble(DLX.BLE, t1, getBranchOffset(s.getTarget())));
                }
                break;
            case blt:
                {
                    Blt s = (Blt)i;
                    emit(DLX.assemble(DLX.BLT, t1, getBranchOffset(s.getTarget())));
                }
                break;
            case bge:
                {
                    Bge s = (Bge)i;
                    emit(DLX.assemble(DLX.BGE, t1, getBranchOffset(s.getTarget())));
                }
                break;
            case bgt:
                {
                    Bgt s = (Bgt)i;
                    emit(DLX.assemble(DLX.BGT, t1, getBranchOffset(s.getTarget())));
                }
                break;
            case read:
                {
                    Read s = (Read)i;
                    emit(DLX.assemble(DLX.RDI, target(s)));
                    home(s);
                }
                break;
            case write:
                {
                    Write s = (Write)i;
                    if(s.getArg() instanceof Immediate) {
                        emit(DLX.assemble(DLX.ADDI, t1, zero, ((Immediate)s.getArg()).getValue()));
                        emit(DLX.assemble(DLX.WRD, t1));
                    } else {
                        emit(DLX.assemble(DLX.WRD, location1(s.getArg())));
                    }
                }
                break;
            case wln:
                {
                    emit(DLX.assemble(DLX.WRL));
                }
                break;
            case ret:
                {
                    Ret s = (Ret)i;

                    // put return value in register, do this before
                    // restoring registers and deleting local
                    // variables
                    if(!(s.getArg() instanceof NoValue)) {
                        if(s.getArg() instanceof Immediate) {
                            emit(DLX.assemble(DLX.ADDI, retVal, zero,
                                              ((Immediate)s.getArg()).getValue()));
                        } else {
                            // need to move return address into retVal register
                            int argLoc = location1(s.getArg());
                            // if the return val was spilled, finding the
                            // location might have put it where we need it (into t1)
                            if(argLoc != retVal) {
                                emit(DLX.assemble(DLX.ADD, retVal, zero, argLoc));
                            }
                        }
                    }

                    // restore registers
                    for(Integer reg : regStack) {
                        pop(reg);
                    }

                    // destroy stack frame
                    emit(DLX.assemble(DLX.ADD, sp, zero, fp));
                    pop(fp);

                    emit(DLX.assemble(DLX.RET, retAddr));
                }
                break;
            case call:
                {
                    Call s = (Call)i;

                    // push args onto the stack
                    ArrayList<Value> args = s.getArguments();
                    // go backwards through args
                    for(int j = args.size() -1; j >= 0; j--) {
                        Value v = args.get(j);
                        if(v instanceof Immediate) {
                            emit(DLX.assemble(DLX.ADDI, t1, zero, ((Immediate)v).getValue()));
                            push(t1);
                        } else {
                            push(location1(v));
                        }
                    }

                    // branch to function call
                    emit(DLX.assemble(DLX.BSR, getBranchOffset(s.getFunctionName())));

                    // after return, pop all arguments off of the stack
                    if(args.size() > 0) {
                        emit(DLX.assemble(DLX.SUBI, sp, sp,
                                          args.size() * Type.getWordSize()));
                    }
                    Integer color = s.getColor();
                    // if the return value is used
                    if(color != null) {
                        // if the value isn't in the correct register,
                        // need to move it. What this really does is
                        // avoid emiting an uneccessary move if the
                        // value has been spilled, since for moving
                        // the value 'home' we need it to be in t1.
                        if(target(s) != retVal) {
                            emit(DLX.assemble(DLX.ADD, target(s), zero, retVal));
                        }
                        home(s);
                    }
                }
                break;
            case fetch:
                {
                    Fetch s = (Fetch)i;
                    // if it is spilled, the argument will be accessed
                    // from the location it was passed on the stack
                    if(isSpilled(s)) {
                        continue;
                    }
                    int address = getFetchAddress(s);
                    emit(DLX.assemble(DLX.LDW, target(s), fp, address));
                    // todo: need to ensure that if a fetch is
                    // spilled, we just only access it from its
                    // orriginal position on the stack
                    home(s);

                }
                break;
            default:
                throw new Exception("Unrecognized ir opcode");
            }
        }
        BasicBlock next = bb.getNext();
        if(next != null) {
            emitCode(next);
        }

    }

    private int getFetchAddress(Fetch f) {
        int position = f.getPosition();
        int address = (position + 1) * -4;
        return address;
    }

    // opcode is the opcode of the indexed addressing instruction, ie LDX/SDX,
    // and if both of the parameters are registers, the opcode will be
    // decremented to get to the offset addressing LDW/SDW.
    //
    // a is the parameter to use as a, so for loads it is the target,
    // for stores it is the register containing the value to be stored
    //
    // There is the implicit assumption that all loads or stores will
    // be offset from either the GBP or the FP, so one of the
    // arguments will already be in a register. Because of this, if
    // the other argument is spilled, it will always end up in t2 (I
    // am only using location2()). This way, you can put the value to
    // be stored into location1 (if it has been spilled).
    private void emitLoadOrStore(int opcode, int a, Adda address) throws Exception {
        Value v = address.getArg1();
        int b, c;
        if(v instanceof Immediate) {
            c = ((Immediate)v).getValue();
            opcode = opcode - 1;
            v = address.getArg2();
            if(v.equals(Type.getGBP())) {
                b = gbp;
            } else if(v.equals(Type.getFP())) {
                b = fp;
            } else {
                b = location2(v);
            }
        } else {
            if(v.equals(Type.getGBP())) {
                b = gbp;
            } else if(v.equals(Type.getFP())) {
                b = fp;
            } else {
                b = location2(v);
            }
            v = address.getArg2();
            if(v instanceof Immediate) {
                c = ((Immediate)v).getValue();
                opcode = opcode - 1;
            } else if(v.equals(Type.getGBP())) {
                c = gbp;
            } else if(v.equals(Type.getFP())) {
                c = fp;
            } else {
                c = location2(v);
            }
        }
        emit(DLX.assemble(opcode, a, b, c));
    }

    private void makeLabel(Object bb) throws Exception {
        labels.put(bb, code.size());
        fixup(bb);
    }

    private void fixup(Object bb) throws Exception {
        ArrayList<Integer> fixupLocations = fixup.get(bb);
        if(fixupLocations == null) {
            return;
        }
        int address = labels.get(bb);
        for(Integer i : fixupLocations) {
            int instruction = code.get(i);
            // determine relative branch address
            int c = address - i;
            instruction = setC(instruction, c);
            code.set(i, instruction); // replace instruction with the fixed up version
        }
        fixupLocations.remove(bb);       // remove bb from the fixup locations
    }

    // this will set the 'c' value for any instruction with the F1 format
    private int setC(int instruction, int c) throws Exception {
        if(c < 0) c ^= 0xffff0000; // c will be sign extended from 16 bit value
        if((c & ~0xffff) != 0) {
            throw new Exception("Illegal operand for instruction");
        }
        instruction = (instruction & ~0xffff) | c;
        return instruction;
    }

    // either returns the offset or adds the current location to the fixup list
    private int getBranchOffset(Object to) {
        Integer label = labels.get(to);
        if(label == null) {
            label = 0; // should result in an infinite loop if it is not fixed up
            ArrayList<Integer> fix = fixup.get(to);
            if(fix == null) {
                fix = new ArrayList<Integer>();
                fixup.put(to, fix);
            }
            // this will be the index of the next instruction emited,
            // which should be our branch
            fix.add(code.size());
        }
        return label - code.size();
    }
}
