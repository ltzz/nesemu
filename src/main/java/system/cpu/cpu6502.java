package system.cpu;
import system.Ram;
public final class cpu6502 {

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

    public void init(){
        regP = (byte)0x34;
        regS = (byte)0xFD;
    }

    void setFlagI(boolean value){
        setP(value, 2);
    }
    void setFlagB(boolean value){
        setP(value, 4);
    }

    void setFlagN(boolean value){
        setP(value, 7);
    }

    void setFlagZ(boolean value){
        setP(value, 1);
    }
    boolean getFlagZ(){
        final boolean zeroFlag = !((regP & 0x02) == 0);
        return zeroFlag;
    }

    void setFlagC(boolean value){
        setP(value, 0);
    }
    boolean getFlagC(){
        final boolean carryFlag = !((regP & 0x01) == 0);
        return carryFlag;
    }

    void setFlagV(boolean value){
        setP(value, 6);
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
                tmpAddress = ram.getRAMValue16(immediate16);
                address = tmpAddress;
                break;
            case IndirectX:
                tmpAddress = (getIm8() & 0xFF) + (regX & 0xFF);
                address = ram.getRAMValue16(tmpAddress);
                break;
            case Indirect_Y:
                tmpAddress = (getIm8() & 0xFF);
                address = ram.getRAMValue16(tmpAddress) + (regY & 0xFF);
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

    void evalNZ(final byte data){
        if((data & 0xFF) < 128){
            setFlagN(true);
        }else{
            setFlagN(false);
        }
        if( (data & 0xFF) == 0 ){
            setFlagZ(true);
        }
        else {
            setFlagZ(false);
        }
    }

    void opTXS(){
        evalNZ(regX);
        regS = regX;
    }
    void opTAX(){
        evalNZ(regA);
        regX = regA;
    }
    void opTXA(){
        evalNZ(regX);
        regA = regX;
    }
    void opTYA(){
        evalNZ(regY);
        regA = regY;
    }
    void opTAY(){
        evalNZ(regA);
        regY = regA;
    }

    void opCPX(Addressing addressing){
        byte value = getOperand(addressing);
        setRegAtCompare(regX, value);
    }

    void opCMP(Addressing addressing){
        byte value = getOperand(addressing);
        setRegAtCompare(regA, value);
    }

    void setRegAtCompare(byte reg, byte value){
        final int regValue = reg & 0xFF;
        final int targetValue = value & 0xFF;
        if( regValue >= targetValue ) {
            setFlagC(true);
        }else{
            setFlagC(false);
        }
        if( regValue == targetValue ) {
            setFlagZ(true);
        }else{
            setFlagZ(false);
        }
        if( regValue < targetValue ) {
            setFlagN(true);
        }else{
            setFlagN(false);
        }
    }

    void opBITAbs(){
        final int absolute = getIm16();
        byte value = ram.getRAMValue(absolute);
        if( (value & 0x80) > 0 ){
            setFlagN(true);
        }
        else{
            setFlagN(false);
        }
        if( (value & 0x40) > 0 ){
            setFlagV(true);
        }
        else{
            setFlagV(false);
        }
        if( (regA & value) == 0 ) { // TODO: ロジック確認してないので要確認
            setFlagZ(true);
        }
        else{
            setFlagZ(false);
        }
    }


    void opAND(Addressing addressing) {
        final byte value = getOperand(addressing);
        final int resultValue = regA & value;
        regA = (byte)(resultValue);
        evalNZ(regA);
    }

    void opEOR(Addressing addressing) {
        final byte value = getOperand(addressing);
        final int resultValue = regA ^ value;
        regA = (byte)(resultValue);
        evalNZ(regA);
    }


    void opADC(Addressing addressing) {
        final byte value = getOperand(addressing);
        final int carry = regP & 0x01;
        final int resultValue = (regA & 0xFF) + value + carry;
        regA = (byte)(resultValue);
        evalNZ(value);
        if( resultValue >= 0x100 ) { // TODO: ロジック確認してないので要確認
            setFlagC(true);
        }
        else{
            setFlagC(false);
        }
    }

    void opSBC(Addressing addressing) {
        final byte value = getOperand(addressing);
        final int carry = regP & 0x01;
        final int notCarry = carry > 0 ? 0 : 1;
        final int resultValue = (regA & 0xFF) - value - notCarry;
        regA = (byte)(resultValue);
        evalNZ(value);
        if( resultValue < 0 ) { // TODO: ロジック確認してないので要確認
            setFlagC(false);
        }
        else{
            setFlagC(true);
        }
    }

    void opASL() {
        final int resultValue = (regA & 0xFF) << 1;
        regA = (byte)(resultValue);
        evalNZ(regA);
        if( resultValue >= 0x100 ) { // TODO: ロジック確認してないので要確認
            setFlagC(true);
        }
        else{
            setFlagC(false);
        }
    }

    void opASL(Addressing addressing) {
        final int address = getOperandAddress(addressing);
        byte value = ram.getRAMValue(address);
        int resultValue = (value & 0xFF) << 1;
        value = (byte)(resultValue);
        ram.setRAMValue(address, value);
        evalNZ(value);
        if( resultValue >= 0x100 ) { // TODO: ロジック確認してないので要確認
            setFlagC(true);
        }
        else{
            setFlagC(false);
        }
    }

    void opINX(){
        regX = (byte)(regX + 1);
        evalNZ(regX);
    }

    void opINC(Addressing addressing){
        final int address = getOperandAddress(addressing);
        byte value = ram.getRAMValue(address);
        value = (byte)(value + 1);
        ram.setRAMValue(address, value);
        evalNZ(value);
    }

    void opDEC(Addressing addressing){
        final int address = getOperandAddress(addressing);
        byte value = ram.getRAMValue(address);
        value = (byte)(value - 1);
        ram.setRAMValue(address, value);
        evalNZ(value);
    }

    void opINY(){
        regY = (byte)(regY + 1);
        evalNZ(regY);
    }

    void opDEX(){
        regX = (byte)(regX - 1);
        evalNZ(regX);
    }

    void opDEY(){
        regY = (byte)(regY - 1);
        evalNZ(regY);
    }

    void opCLC(){
        regP = (byte)(regP & 0xFE);
    }
    void opSEC(){
        regP = (byte)(regP | 0x01);
    }

    void opSTA(Addressing addressing){
        ram.setRAMValue(getOperandAddress(addressing), regA);
    }

    void opSTX(Addressing addressing){
        ram.setRAMValue(getOperandAddress(addressing), regX);
    }

    void opSTY(Addressing addressing){
        ram.setRAMValue(getOperandAddress(addressing), regY);
    }

    void opLDA(Addressing addressing){
        final byte operand = getOperand(addressing);
        evalNZ(operand);
        regA = operand;
    }
    void opLDX(Addressing addressing){
        final byte operand = getOperand(addressing);
        evalNZ(operand);
        regX = operand;
    }
    void opLDY(Addressing addressing){
        final byte operand = getOperand(addressing);
        evalNZ(operand);
        regY = operand;
    }

    void opBNE(){
        final boolean zeroFlag = getFlagZ();
        if( !zeroFlag ){
            final int relative = getIm8();
            programCounter = programCounter + relative;
        }
    }
    void opBPL(){
        final boolean negativeFlag = !((regP & 0x80) == 0);
        if( !negativeFlag ){
            final int relative = getIm8();
            programCounter = programCounter + relative;
        }
    }
    void opBCC(){
        final boolean carryFlag = getFlagC();
        if( !carryFlag ){
            final int relative = getIm8();
            programCounter = programCounter + relative;
        }
    }
    void opBCS(){
        final boolean carryFlag = getFlagC();
        if( carryFlag ){
            final int relative = getIm8();
            programCounter = programCounter + relative;
        }
    }
    void opBEQ(){
        final boolean zeroFlag = getFlagZ();
        if( zeroFlag ){
            final int relative = getIm8();
            programCounter = programCounter + relative;
        }
    }

    void opJSR(){
        final int absolute = getIm16();
        final int returnAddress = programCounter + 2; // この命令の最後のアドレスをpush
        final byte upper = (byte)((returnAddress >> 8) & 0xFF);
        final byte lower = (byte)(returnAddress & 0xFF);
        final int stackAddress = 0x100 + (regS & 0xFF);
        ram.setRAMValue(stackAddress, upper);
        ram.setRAMValue(stackAddress- 1, lower);
        regS = (byte)(regS - 2);
        programCounter = absolute;
    }

    void opPHA(){
        final int stackAddress = 0x100 + (regS & 0xFF);
        ram.setRAMValue(stackAddress, regA);
        regS = (byte)(regS - 1);
    }

    void opPHP(){
        final int stackAddress = 0x100 + (regS & 0xFF);
        ram.setRAMValue(stackAddress, regP);
        regS = (byte)(regS - 1);
    }
    void opPLP(){
        final int stackAddress = 0x100 + (regS & 0xFF) + 1;
        regP = ram.getRAMValue(stackAddress);
        regS = (byte)(regS + 1);
    }

    void opRTS(){
        final int stackAddress = 0x100 + (regS & 0xFF) + 1;
        final byte lower = ram.getRAMValue(stackAddress);
        final byte upper = ram.getRAMValue(stackAddress + 1);
        programCounter = ((upper & 0xFF) << 8) | (lower & 0xFF);
        //programCounter = (upper << 8) | lower;
        regS = (byte)(regS + 2);
    }

    void opPLA(){
        final int address = 0x100 + (regS & 0xFF) + 1;
        final byte value = ram.getRAMValue(address);
        regA = value;
        evalNZ(regA);
        regS = (byte)(regS + 1);
    }

    void opBRK(){
        setFlagI(true);
        setFlagB(true);
    }

    void opJMP_Abs(){
        int absolute = getIm16();
        programCounter = absolute;
    }

    public void interpret(byte opcode){
        byte immediate;
        // System.out.println(Integer.toHexString(opcode & 0xFF));

        int opcodeInt = opcode & 0xFF;
        switch(opcodeInt){
            case 0xA2://LDX(Immediate):メモリからXにロード(2バイト/2サイクル)
                opLDX(Addressing.Immediate);
                programCounter += 2;
                break;
            case 0xA6://LDX(Zeropage):メモリからXにロード(2バイト/3サイクル)
                opLDX(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0xAE://LDX(Absolute):メモリからXにロード(3バイト/4サイクル)
                opLDX(Addressing.Absolute);
                programCounter += 3;
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
            case 0xA0://LDY(Immediate):メモリからYにロード(2バイト/2サイクル)
                opLDY(Addressing.Immediate);
                programCounter += 2;
                break;
            case 0xA4://LDY(ZeroPage):メモリからYにロード(2バイト/3サイクル)
                opLDY(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0xAC://LDY(Absolute):メモリからAにロード(3バイト/4サイクル)
                opLDY(Addressing.Absolute);
                programCounter += 3;
                break;
            case 0xBC://LDY(Absolute, X):メモリからAにロード(3バイト/4サイクル)
                opLDY(Addressing.AbsoluteX);
                programCounter += 3;
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
            case 0x9D://STA(AbsoluteX):Aからメモリにストア(3バイト/5サイクル)
                opSTA(Addressing.AbsoluteX);
                programCounter += 3;
                break;
            case 0x91://STA(Indirect_Y):Aからメモリにストア(2バイト/6サイクル)
                opSTA(Addressing.Indirect_Y);
                programCounter += 2;
                break;
            case 0x86://STX(Zeropage):Xからメモリにストア(2バイト/3サイクル)
                opSTX(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0x8E://STX(Absolute):Xからメモリにストア(3バイト/4サイクル)
                opSTX(Addressing.Absolute);
                programCounter += 3;
                break;
            case 0x8C://STY(Absolute):Yからメモリにストア(3バイト/4サイクル)
                opSTY(Addressing.Absolute);
                programCounter += 3;
                break;
            case 0x84://STY(Zeropage):Yからメモリにストア(2バイト/3サイクル)
                opSTY(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0x9A:
                // TODO: Sに0を入れているROMがあり、うまく動作しない（あるいは入れる元の計算結果が誤り
                opTXS();
                programCounter++;
                break;
            case 0xAA:
                opTAX();
                programCounter++;
                break;
            case 0x8A:
                opTXA();
                programCounter++;
                break;
            case 0x98:
                opTYA();
                programCounter++;
                break;
            case 0xA8:
                opTAY();
                programCounter++;
                break;
            case 0xE0:
                opCPX(Addressing.Immediate);
                programCounter += 2;
                break;
            case 0xE4:
                opCPX(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0xC5:
                opCMP(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0xC9:
                opCMP(Addressing.Immediate);
                programCounter += 2;
                break;
            case 0xCD:
                opCMP(Addressing.Absolute);
                programCounter += 3;
                break;
            case 0x2C:
                opBITAbs();
                programCounter += 3;
                break;
            case 0x29:
                opAND(Addressing.Immediate);
                programCounter += 2;
                break;
            case 0x49:
                opEOR(Addressing.Immediate);
                programCounter += 2;
                break;
            case 0x69:
                opADC(Addressing.Immediate);
                programCounter += 2;
                break;
            case 0x65:
                opADC(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0xE9:
                opSBC(Addressing.Immediate);
                programCounter += 2;
                break;
            case 0xE5:
                opSBC(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0xED:
                opSBC(Addressing.Absolute);
                programCounter += 3;
                break;
            case 0x06:
                opASL(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0x0A:
                opASL();
                programCounter += 1;
                break;
            case 0xE8:
                opINX();
                programCounter++;
                break;
            case 0xC8:
                opINY();
                programCounter++;
                break;
            case 0xE6: // (2バイト/5サイクル)
                opINC(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0xEE: // (3バイト/6サイクル)
                opINC(Addressing.Absolute);
                programCounter += 3;
                break;
            case 0xFE: // (3バイト/7サイクル)
                opINC(Addressing.AbsoluteX);
                programCounter += 3;
                break;
            case 0xCA:
                opDEX();
                programCounter++;
                break;
            case 0x88:
                opDEY();
                programCounter++;
                break;
            case 0xC6:
                opDEC(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0xDE:
                opDEC(Addressing.AbsoluteX);
                programCounter += 3;
                break;
            case 0xD0:
                opBNE();
                programCounter += 2;
                break;
            case 0x10:
                opBPL();
                programCounter += 2;
                break;
            case 0x90:
                opBCC();
                programCounter += 2;
                break;
            case 0xB0:
                opBCS();
                programCounter += 2;
                break;
            case 0xF0:
                opBEQ();
                programCounter += 2;
                break;
            case 0x20:
                opJSR();
                break;
            case 0x48:
                opPHA();
                programCounter += 1;
                break;
            case 0x08:
                opPHP();
                programCounter += 1;
                break;
            case 0x28:
                opPLP();
                programCounter += 1;
                break;
            case 0x68:
                opPLA();
                programCounter += 1;
                break;
            case 0x60:
                opRTS();
                programCounter += 1;
                break;
            case 0x4C:
                opJMP_Abs();
                //programCounter += 3;// FIXME: pcインクリメントしないといかん気がする→確認
                break;
            case 0x38:
                opSEC();
                programCounter++;
                break;
            case 0x18:
                opCLC();
                programCounter++;
                break;
            case 0xEA:
                // NOP
                programCounter++;
                break;
            case 0xD8: // CLD ファミコン用6502では命令なし
            case 0xF8: // SED ファミコン用6502では命令なし
                programCounter += 1;
                break;
            default:
                System.out.println(Integer.toHexString(opcode & 0xFF));
                break;
        }
    }
}
