package system.cpu;
import system.Ram;
public class cpu6502 {

    public Ram ram;
    public int programCounter;
    private byte regA;
    private byte regX;
    private byte regY;
    private byte regS;
    private byte regP;

    enum Addressing {
        Implied,
        Accumulator,
        Immediate,
        Absolute,
        AbsoluteX,
        AbsoluteY,
        ZeroPage,
        ZeroPageX,
        ZeroPageY,
        Relative,
        Indirect,
        IndirectX,
        Indirect_Y
    }

    public cpu6502(Ram ram){
        this.ram = ram;
    }

    void init(){
    }

    void setFlagI(boolean value){
        setP(value, 2);
    }

    void setFlagN(boolean value){
        setP(value, 7);
    }

    void setFlagZ(boolean value){
        setP(value, 1);
    }

    private void setP(boolean flag, int bitpos){
        if(flag){
            regP |= (byte)(1 << bitpos);
        }
        else {
            regP &= (byte)~(1 << bitpos);
        }
    }

    int getIm16(){
        return ram.getRAMValue16(programCounter + 1);
    }

    byte getIm8(){
        return ram.getRAMValue(programCounter + 1);
    }

    int getOperandAddress(Addressing addressing){
        int immediate16;
        int address = 0x0000;
        int tmpAddress;

        switch(addressing){
            case Immediate:
                break;
            case ZeroPage:
                address = getIm8() & 0xFF;
                break;
            case ZeroPageX:
                address = (getIm8() & 0xFF) + (regX & 0xFF);
                break;
            case ZeroPageY:
                address = (getIm8() & 0xFF) + (regY & 0xFF);
                break;
            case Absolute:
                address = getIm16();
                break;
            case AbsoluteX:
                address = getIm16() + (regX & 0xFF);
                break;
            case AbsoluteY:
                address = getIm16() + (regY & 0xFF);
                break;
            case Indirect:
                immediate16 = getIm16();
                tmpAddress = ram.getRAMValue(immediate16);
                address = ram.getRAMValue(tmpAddress);
                break;
            case IndirectX:
                address = ram.getRAMValue((getIm8() & 0xFF) + (regX & 0xFF));
                break;
            case Indirect_Y:
                address = ram.getRAMValue((getIm8() & 0xFF)) + (regY & 0xFF);
                break;
        }
        return address;
    }

    byte getOperand(Addressing addressing){
        byte data = 0x00;

        switch(addressing){
            case Immediate:
                data = getIm8();
                break;
            case ZeroPage:
                data = ram.getRAMValue(getOperandAddress(Addressing.ZeroPage));
                break;
            case ZeroPageX:
                data = ram.getRAMValue(getOperandAddress(Addressing.ZeroPageX));
                break;
            case ZeroPageY:
                data = ram.getRAMValue(getOperandAddress(Addressing.ZeroPageY));
                break;
            case Absolute:
                data = ram.getRAMValue(getOperandAddress(Addressing.Absolute));
                break;
            case AbsoluteX:
                data = ram.getRAMValue(getOperandAddress(Addressing.AbsoluteX));
                break;
            case AbsoluteY:
                data = ram.getRAMValue(getOperandAddress(Addressing.AbsoluteY));
                break;
            case Indirect:
                data = ram.getRAMValue(getOperandAddress(Addressing.Indirect));
                break;
            case IndirectX:
                data = ram.getRAMValue(getOperandAddress(Addressing.IndirectX));
                break;
            case Indirect_Y:
                data = ram.getRAMValue(getOperandAddress(Addressing.Indirect_Y));
                break;
        }
        return data;
    }

    void opTXS(){
        if((regX & 0xFF) < 128){
            setFlagN(true);
        }else{
            setFlagN(false);
        }
        if( (regX & 0xFF) == 0 ){
            setFlagZ(true);
        }
        else {
            setFlagZ(false);
        }
        regS = regX;
    }

    void opINX(){
        regX = (byte)(regX + 1);
        if((regX & 0xFF) < 128){
            setFlagN(true);
        }else{
            setFlagN(false);
        }
        if( (regX & 0xFF) == 0 ){
            setFlagZ(true);
        }
        else {
            setFlagZ(false);
        }
    }

    void opDEY(){
        regY = (byte)(regY - 1);
        if((regY & 0xFF) < 128){
            setFlagN(true);
        }else{
            setFlagN(false);
        }
        if( (regY & 0xFF) == 0 ){
            setFlagZ(true);
        }
        else {
            setFlagZ(false);
        }
    }

    void opSTA(Addressing addressing){
        ram.setRAMValue(getOperandAddress(addressing), regA);
    }


    void opLDA(Addressing addressing){
        byte operand = getOperand(addressing);
        if((operand & 0xFF) < 128){
            setFlagN(true);
        }else{
            setFlagN(false);
        }
        if( (operand & 0xFF) == 0 ){
            setFlagZ(true);
        }
        else {
            setFlagZ(false);
        }
        regA = operand;
    }

    void opLDX(Addressing addressing){
        byte operand = getOperand(addressing);
        if((operand & 0xFF) < 128){
            setFlagN(true);
        }
        else{
            setFlagN(false);
        }
        if( (operand & 0xFF) == 0 ){
            setFlagZ(true);
        }
        else {
            setFlagZ(false);
        }
        regX = operand;
    }

    void opBNE(){
        boolean zeroFlag = !((regP & 0x02) == 0);
        if( !zeroFlag ){
            int relative = getIm8(); // TODO: ここは符号付きでキャスト？
            programCounter = programCounter + relative;
        }
    }

    void opJMP_Abs(){
        int absolute = ram.getRAMValue(getIm16()) & 0xFF;
        programCounter = programCounter + absolute;
    }

    void opLDY(Addressing addressing){
        byte operand = getOperand(addressing);
        if((operand & 0xFF) < 128){
            setFlagN(true);
        }else{
            setFlagN(false);
        }
        if( (operand & 0xFF) == 0 ){
            setFlagZ(true);
        }
        else {
            setFlagZ(false);
        }
        regY = operand;
    }

    public void interpret(byte opcode){
        byte immediate;
        System.out.println(Integer.toHexString(opcode & 0xFF));

        int opcodeInt = opcode & 0xFF;
        switch(opcodeInt){
            case 0xA2://LDX(Immediate):メモリからXにロード(2バイト/2サイクル)
                opLDX(Addressing.Immediate);
                programCounter += 2;
                break;
            case 0x78://SEI:IRQ割り込みの禁止(1バイト/2サイクル)
                setFlagI(true);
                programCounter++;
                break;
            case 0xA9://LDA(Immediate):メモリからAにロード(2バイト/2サイクル)
                opLDA(Addressing.Immediate);
                programCounter += 2;
                break;
            case 0xA5://LDA(Zeropage):メモリからAにロード(2バイト/3サイクル)
                opLDA(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0xB5://LDA(ZeropageX):メモリからAにロード(2バイト/4サイクル)
                opLDA(Addressing.ZeroPageX);
                programCounter += 2;
                break;
            case 0xAD://LDA(Absolute):メモリからAにロード(3バイト/4サイクル)
                opLDA(Addressing.Absolute);
                programCounter += 3;
                break;
            case 0xBD://LDA(AbsoluteX):メモリからAにロード(3バイト/4サイクル)
                opLDA(Addressing.AbsoluteX);
                programCounter += 3;
                break;
            case 0xB9://LDA(AbsoluteY):メモリからAにロード(3バイト/4サイクル)
                opLDA(Addressing.AbsoluteY);
                programCounter += 3;
                break;
            case 0xA1://LDA(IndirectX):メモリからAにロード(2バイト/6サイクル)
                opLDA(Addressing.IndirectX);
                programCounter += 2;
                break;
            case 0xB1://LDA(Indirect_Y):メモリからAにロード(2バイト/5サイクル)
                opLDA(Addressing.Indirect_Y);
                programCounter += 2;
                break;
            case 0xA0://LDY(Immediate):メモリからAにロード(2バイト/2サイクル)
                opLDY(Addressing.Immediate);
                programCounter += 2;
                break;
            case 0x85://STA(Zeropage):Aからメモリにストア(2バイト/3サイクル)
                opSTA(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0x95://STA(ZeropageX):Aからメモリにストア(2バイト/4サイクル)
                opSTA(Addressing.ZeroPageX);
                programCounter += 2;
                break;
            case 0x8D://STA(Absolute):Aからメモリにストア(3バイト/4サイクル)
                opSTA(Addressing.Absolute);
                programCounter += 3;
                break;
            case 0x9A:
                opTXS();
                programCounter++;
                break;
            case 0xE8:
                opINX();
                programCounter++;
                break;
            case 0x88:
                opDEY();
                programCounter++;
                break;
            case 0xD0:
                opBNE();
                programCounter += 2;
                break;
            case 0x4C:
                opJMP_Abs();
                // programCounter;
                // FIXME: pcインクリメントしないといかん気がする
                break;
        }
    }
}
