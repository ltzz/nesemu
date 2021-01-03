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

    public byte getRegA(){
        return regA;
    }
    public byte getRegX(){
        return regX;
    }
    public byte getRegY(){
        return regY;
    }
    public byte getRegP(){
        return regP;
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
    boolean getFlagN(){
        final boolean negativeFlag = !((regP & 0x80) == 0);
        return negativeFlag;
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
    boolean getFlagV(){
        final boolean overflowFlag = !((regP & 0x40) == 0);
        return overflowFlag;
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
                address = ((getIm8() & 0xFF) + (regX & 0xFF)) & 0xFF;
                break;
            case ZeroPageY:
                address = ((getIm8() & 0xFF) + (regY & 0xFF)) & 0xFF;
                break;
            case Absolute:
                address = getIm16();
                break;
            case AbsoluteX:
                address = (getIm16() + (regX & 0xFF)) & 0xFFFF;
                break;
            case AbsoluteY:
                address = (getIm16() + (regY & 0xFF)) & 0xFFFF;
                break;
            case Indirect:
                immediate16 = getIm16();
                tmpAddress = ram.getRAMValueInPage(immediate16);
                address = tmpAddress;
                break;
            case IndirectX:
                tmpAddress = (getIm8() & 0xFF) + (regX & 0xFF);
                address = ram.getRAMValue16ByAddress8(tmpAddress & 0xFF);
                break;
            case Indirect_Y:
                tmpAddress = (getIm8() & 0xFF);
                address = ram.getRAMValue16ByAddress8(tmpAddress) + (regY & 0xFF);
                address &= 0xFFFF;
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
            setFlagN(false);
        }else{
            setFlagN(true);
        }
        if( (data & 0xFF) == 0 ){
            setFlagZ(true);
        }
        else {
            setFlagZ(false);
        }
    }

    void opTXS(){
        regS = regX;
    }
    void opTSX(){
        evalNZ(regS);
        regX = regS;
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

    void opCPY(Addressing addressing){
        byte value = getOperand(addressing);
        setRegAtCompare(regY, value);
    }

    void opCMP(Addressing addressing){
        byte value = getOperand(addressing);
        setRegAtCompare(regA, value);
    }

    void opDCM(Addressing addressing){
        byte value = getOperand(addressing);
        byte resultValue = (byte)((value - 1) & 0xFF);
        ram.setRAMValue(getOperandAddress(addressing), resultValue);
        setRegAtCompare(regA, resultValue);
    }

    void opISC(Addressing addressing){
        byte value = getOperand(addressing);
        byte resultValue = (byte)((value + 1) & 0xFF);
        ram.setRAMValue(getOperandAddress(addressing), resultValue);
        calcAndSetRegSBC(resultValue);
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
        if( ((regValue - targetValue) & 0x80) > 0) {
            setFlagN(true);
        }else{
            setFlagN(false);
        }
    }

    void opBIT(Addressing addressing){
        final int address = getOperandAddress(addressing);
        byte value = ram.getRAMValue(address);
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

    void opORA(Addressing addressing) {
        final byte value = getOperand(addressing);
        final int resultValue = regA | value;
        regA = (byte)(resultValue);
        evalNZ(regA);
    }


    void opADC(Addressing addressing) {
        final byte value = getOperand(addressing);
        final int carry = regP & 0x01;
        final int resultValue = (regA & 0xFF) + (value & 0xFF) + carry;
        final byte regAOld = regA;
        regA = (byte)(resultValue);
        evalNZ(regA);
        if( resultValue >= 0x100 ) {
            setFlagC(true);
        }
        else{
            setFlagC(false);
        }
        if(((resultValue ^ value) & (resultValue ^ regAOld) & 0x80) !=0){
            setFlagV(true);
        }
        else {
            setFlagV(false);
        }
    }

    void opSBC(Addressing addressing) {
        final byte value = getOperand(addressing);
        calcAndSetRegSBC(value);
    }

    void calcAndSetRegSBC(byte value){
        final int carry = regP & 0x01;
        final int notCarry = carry > 0 ? 0 : 1;
        final int resultValue = (regA & 0xFF) - (value  & 0xFF) - notCarry;
        final byte regAOld = regA;
        regA = (byte)(resultValue);
        evalNZ(regA);
        if( resultValue < 0 ) { // TODO: ロジック確認してないので要確認
            setFlagC(false);
        }
        else{
            setFlagC(true);
        }

        final int resultValueByte = (byte)resultValue;
        final byte borrowedValue = (byte)(((value ^ 0xFF) + 0x100) & 0xFF);
        if(((resultValueByte ^ borrowedValue) & (resultValueByte ^ regAOld) & 0x80) > 0){
            setFlagV(true);
        }
        else {
            setFlagV(false);
        }
    }

    void opLSR() {
        regA = calcAndSetRegLSR(regA);
    }

    void opLSR(Addressing addressing){
        byte data = getOperand(addressing);
        data = calcAndSetRegLSR(data);
        ram.setRAMValue(getOperandAddress(addressing), data);
    }

    byte calcAndSetRegLSR(byte data){
        final int carry = data & 0x01;
        final int resultValue = (data & 0xFF) >> 1;
        data = (byte)(resultValue);
        evalNZ(data);
        if( carry > 0 ) { // TODO: ロジック確認してないので要確認
            setFlagC(true);
        }
        else{
            setFlagC(false);
        }
        return data;
    }

    void opROR() {
        regA = calcAndSetRegROR(regA);
    }

    void opROR(Addressing addressing){
        byte data = getOperand(addressing);
        data = calcAndSetRegROR(data);
        ram.setRAMValue(getOperandAddress(addressing), data);
    }

    byte calcAndSetRegROR(byte data){
        final int carry = getFlagC() ? 1 : 0;
        final int outputCarry = data & 0x01;
        final int resultValue = (data & 0xFF) >> 1;
        data = (byte)(resultValue);
        data |= carry << 7;
        evalNZ(data);
        if( outputCarry > 0 ) { // TODO: ロジック確認してないので要確認
            setFlagC(true);
        }
        else{
            setFlagC(false);
        }
        return data;
    }

    void opROL() {
        regA = calcAndSetRegROL(regA);
    }
    void opROL(Addressing addressing) {
        byte data = getOperand(addressing);
        data = calcAndSetRegROL(data);
        ram.setRAMValue(getOperandAddress(addressing), data);
    }

    byte calcAndSetRegROL(byte data){
        final int outputCarry = data & 0x80;
        final int resultValue = (data & 0xFF) << 1;
        data = (byte)(resultValue);
        data |= (getFlagC() ? 0x01: 0x00);
        evalNZ(data);
        if( outputCarry > 0 ) { // TODO: ロジック確認してないので要確認
            setFlagC(true);
        }
        else{
            setFlagC(false);
        }
        return data;
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


    void opASO(Addressing addressing) {
        final int address = getOperandAddress(addressing);
        byte value = ram.getRAMValue(address);
        int resultValue = (value & 0xFF) << 1;
        value = (byte)(resultValue);
        ram.setRAMValue(address, value);

        regA |= value;

        evalNZ(regA);
        if( resultValue >= 0x100 ) { // TODO: ロジック確認してないので要確認
            setFlagC(true);
        }
        else{
            setFlagC(false);
        }
    }

    void opRLA(Addressing addressing) {
        final int address = getOperandAddress(addressing);
        byte value = ram.getRAMValue(address);
        int resultValue = (value & 0xFF) << 1;
        value = (byte)(resultValue);
        value |= (getFlagC() ? 0x01: 0x00);
        ram.setRAMValue(address, value);

        regA &= value;

        evalNZ(regA);
        if( resultValue >= 0x100 ) { // TODO: ロジック確認してないので要確認
            setFlagC(true);
        }
        else{
            setFlagC(false);
        }
    }

    void opLSE(Addressing addressing) {
        final int address = getOperandAddress(addressing);
        byte value = ram.getRAMValue(address);
        int resultValue = (value & 0xFF) >> 1;
        final int outputCarry = value & 0x01;
        value = (byte)(resultValue);
        ram.setRAMValue(address, value);

        regA ^= value;

        evalNZ(regA);
        if( outputCarry > 0 ) { // TODO: ロジック確認してないので要確認
            setFlagC(true);
        }
        else{
            setFlagC(false);
        }
    }

    void opRRA(Addressing addressing) {
        opROR(addressing);
        opADC(addressing);
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
    void opCLD(){
        regP = (byte)(regP & 0xF7);
    }
    void opCLV(){
        regP = (byte)(regP & 0xBF);
    }
    void opSEC(){
        regP = (byte)(regP | 0x01);
    }

    void opSED(){
        regP = (byte)(regP | 0x08);
    }

    void opSTA(Addressing addressing){
//        System.out.println("STA " + getOperandAddress(addressing) + " " + regA);
        ram.setRAMValue(getOperandAddress(addressing), regA);
    }

    void opSTX(Addressing addressing){
        ram.setRAMValue(getOperandAddress(addressing), regX);
    }

    void opSTY(Addressing addressing){
        ram.setRAMValue(getOperandAddress(addressing), regY);
    }
    void opSAX(Addressing addressing){
        ram.setRAMValue(getOperandAddress(addressing), (byte)(regA & regX));
    }

    void opLDA(Addressing addressing){
//        System.out.println("LDA " + "getIm8()=" + getIm8() + " " + getOperandAddress(addressing) + " " + getOperand(addressing));
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
    void opLAX(Addressing addressing){
        final byte operand = getOperand(addressing);
        evalNZ(operand);
        regA = operand;
        regX = operand;
    }

    void opBNE(){
        final boolean zeroFlag = getFlagZ();
        if( !zeroFlag ){
            final int relative = getIm8();
            programCounter = programCounter + relative;
        }
    }
    void opBPL(){
        final boolean negativeFlag = getFlagN();
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
    void opBVS(){
        final boolean overflowFlag = getFlagV();
        if( overflowFlag ){
            final int relative = getIm8();
            programCounter = programCounter + relative;
        }
    }
    void opBVC(){
        final boolean overflowFlag = getFlagV();
        if( !overflowFlag ){
            final int relative = getIm8();
            programCounter = programCounter + relative;
        }
    }
    void opBMI(){
        final boolean negativeFlag = getFlagN();
        if( negativeFlag ){
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
        final byte value = (byte) (regP | 0x10); // ファミコンの仕様 PHPによってスタックに格納する状態フラグでは、ブレイクフラグをセット
        ram.setRAMValue(stackAddress, value);
        regS = (byte)(regS - 1);
    }

    void opPLP(){
        final int stackAddress = 0x100 + (regS & 0xFF) + 1;
        setRegP(ram.getRAMValue(stackAddress));
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

    void opRTI() {

        // Pをpull
        final int stackAddressP = 0x100 + (regS & 0xFF) + 1;
        setRegP(ram.getRAMValue(stackAddressP));
        regS = (byte)(regS + 1);

        // プログラムカウンタをpull
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
    void opJMP_Indirect(){
        int address = getOperandAddress(Addressing.Indirect);
        programCounter = address;
    }

    public void nextStep(){
        interpret(ram.getRAMValue(programCounter));
    }

    public void setRegP(byte value){
        value = (byte) (value & 0xEF); // ブレイクフラグは実際には存在しないためPへのセット時クリア
        value |= 0x20; // bit5: Rフラグはは常にセット
        regP = value;
    }

    public void interpret(byte opcode){
        byte immediate;
        if( false ) {
            System.out.println(Integer.toHexString(opcode & 0xFF)
                + String.format(" %04X", getIm16())
            );
        }

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
            case 0xB6://LDX(Zeropage,Y):メモリからXにロード(2バイト/4サイクル)
                opLDX(Addressing.ZeroPageY);
                programCounter += 2;
                break;
            case 0xAE://LDX(Absolute):メモリからXにロード(3バイト/4サイクル)
                opLDX(Addressing.Absolute);
                programCounter += 3;
                break;
            case 0xBE://LDX(Absolute, Y):メモリからXにロード(3バイト/4サイクル)
                opLDX(Addressing.AbsoluteY);
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
            case 0xB4://LDY(ZeroPageX):メモリからYにロード(2バイト/4サイクル)
                opLDY(Addressing.ZeroPageX);
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
            case 0xA7: // LAX ※拡張命令
                opLAX(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0xB7: // LAX ※拡張命令
                opLAX(Addressing.ZeroPageY);
                programCounter += 2;
                break;
            case 0xAF: // LAX ※拡張命令
                opLAX(Addressing.Absolute);
                programCounter += 3;
                break;
            case 0xBF: // LAX ※拡張命令
                opLAX(Addressing.AbsoluteY);
                programCounter += 3;
                break;
            case 0xA3: // LAX ※拡張命令
                opLAX(Addressing.IndirectX);
                programCounter += 2;
                break;
            case 0xB3: // LAX ※拡張命令
                opLAX(Addressing.Indirect_Y);
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
            case 0x9D://STA(AbsoluteX):Aからメモリにストア(3バイト/5サイクル)
                opSTA(Addressing.AbsoluteX);
                programCounter += 3;
                break;
            case 0x99://STA(AbsoluteY):Aからメモリにストア(3バイト/5サイクル)
                opSTA(Addressing.AbsoluteY);
                programCounter += 3;
                break;
            case 0x91://STA(Indirect_Y):Aからメモリにストア(2バイト/6サイクル)
                opSTA(Addressing.Indirect_Y);
                programCounter += 2;
                break;
            case 0x81://STA(Indirect,X):Aからメモリにストア(2バイト/6サイクル)
                opSTA(Addressing.IndirectX);
                programCounter += 2;
                break;
            case 0x86://STX(Zeropage):Xからメモリにストア(2バイト/3サイクル)
                opSTX(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0x96://STX(Zeropage,Y):Xからメモリにストア(2バイト/4サイクル)
                opSTX(Addressing.ZeroPageY);
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
            case 0x94://STY(ZeropageX):Yからメモリにストア(2バイト/4サイクル)
                opSTY(Addressing.ZeroPageX);
                programCounter += 2;
                break;
            case 0x87://SAX ※拡張命令
                opSAX(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0x97://SAX ※拡張命令
                opSAX(Addressing.ZeroPageY);
                programCounter += 2;
                break;
            case 0x8F://SAX ※拡張命令
                opSAX(Addressing.Absolute);
                programCounter += 3;
                break;
            case 0x83://SAX ※拡張命令
                opSAX(Addressing.IndirectX);
                programCounter += 2;
                break;
            case 0x9A:
                // TODO: Sに0を入れているROMがあり、うまく動作しない（あるいは入れる元の計算結果が誤り
                opTXS();
                programCounter++;
                break;
            case 0xBA:
                opTSX();
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
            case 0xC0:
                opCPY(Addressing.Immediate);
                programCounter += 2;
                break;
            case 0xC4:
                opCPY(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0xCC:
                opCPY(Addressing.Absolute);
                programCounter += 3;
                break;
            case 0xE0:
                opCPX(Addressing.Immediate);
                programCounter += 2;
                break;
            case 0xE4:
                opCPX(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0xEC:
                opCPX(Addressing.Absolute);
                programCounter += 3;
                break;
            case 0xC5:
                opCMP(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0xD5:
                opCMP(Addressing.ZeroPageX);
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
            case 0xDD:
                opCMP(Addressing.AbsoluteX);
                programCounter += 3;
                break;
            case 0xD9:
                opCMP(Addressing.AbsoluteY);
                programCounter += 3;
                break;
            case 0xC1:
                opCMP(Addressing.IndirectX);
                programCounter += 2;
                break;
            case 0xD1:
                opCMP(Addressing.Indirect_Y);
                programCounter += 2;
                break;
            case 0x2C:
                opBIT(Addressing.Absolute);
                programCounter += 3;
                break;
            case 0x24:
                opBIT(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0x29:
                opAND(Addressing.Immediate);
                programCounter += 2;
                break;
            case 0x25:
                opAND(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0x35:
                opAND(Addressing.ZeroPageX);
                programCounter += 2;
                break;
            case 0x2D:
                opAND(Addressing.Absolute);
                programCounter += 3;
                break;
            case 0x3D:
                opAND(Addressing.AbsoluteX);
                programCounter += 3;
                break;
            case 0x39:
                opAND(Addressing.AbsoluteY);
                programCounter += 3;
                break;
            case 0x21:
                opAND(Addressing.IndirectX);
                programCounter += 2;
                break;
            case 0x31:
                opAND(Addressing.Indirect_Y);
                programCounter += 2;
                break;
            case 0x49:
                opEOR(Addressing.Immediate);
                programCounter += 2;
                break;
            case 0x45:
                opEOR(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0x55:
                opEOR(Addressing.ZeroPageX);
                programCounter += 2;
                break;
            case 0x4D:
                opEOR(Addressing.Absolute);
                programCounter += 3;
                break;
            case 0x5D:
                opEOR(Addressing.AbsoluteX);
                programCounter += 3;
                break;
            case 0x59:
                opEOR(Addressing.AbsoluteY);
                programCounter += 3;
                break;
            case 0x41:
                opEOR(Addressing.IndirectX);
                programCounter += 2;
                break;
            case 0x51:
                opEOR(Addressing.Indirect_Y);
                programCounter += 2;
                break;
            case 0x09:
                opORA(Addressing.Immediate);
                programCounter += 2;
                break;
            case 0x05:
                opORA(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0x15:
                opORA(Addressing.ZeroPageX);
                programCounter += 2;
                break;
            case 0x0D:
                opORA(Addressing.Absolute);
                programCounter += 3;
                break;
            case 0x1D:
                opORA(Addressing.AbsoluteX);
                programCounter += 3;
                break;
            case 0x19:
                opORA(Addressing.AbsoluteY);
                programCounter += 3;
                break;
            case 0x01:
                opORA(Addressing.IndirectX);
                programCounter += 2;
                break;
            case 0x11:
                opORA(Addressing.Indirect_Y);
                programCounter += 2;
                break;
            case 0x69:
                opADC(Addressing.Immediate);
                programCounter += 2;
                break;
            case 0x61:
                opADC(Addressing.IndirectX);
                programCounter += 2;
                break;
            case 0x71:
                opADC(Addressing.Indirect_Y);
                programCounter += 2;
                break;
            case 0x65:
                opADC(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0x75:
                opADC(Addressing.ZeroPageX);
                programCounter += 2;
                break;
            case 0x6D:
                opADC(Addressing.Absolute);
                programCounter += 3;
                break;
            case 0x7D:
                opADC(Addressing.AbsoluteX);
                programCounter += 3;
                break;
            case 0x79:
                opADC(Addressing.AbsoluteY);
                programCounter += 3;
                break;
            case 0xE9:
                opSBC(Addressing.Immediate);
                programCounter += 2;
                break;
            case 0xE5:
                opSBC(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0xF5:
                opSBC(Addressing.ZeroPageX);
                programCounter += 2;
                break;
            case 0xED:
                opSBC(Addressing.Absolute);
                programCounter += 3;
                break;
            case 0xFD:
                opSBC(Addressing.AbsoluteX);
                programCounter += 3;
                break;
            case 0xF9:
                opSBC(Addressing.AbsoluteY);
                programCounter += 3;
                break;
            case 0xE1:
                opSBC(Addressing.IndirectX);
                programCounter += 2;
                break;
            case 0xF1:
                opSBC(Addressing.Indirect_Y);
                programCounter += 2;
                break;
            case 0x06:
                opASL(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0x16:
                opASL(Addressing.ZeroPageX);
                programCounter += 2;
                break;
            case 0x0E:
                opASL(Addressing.Absolute);
                programCounter += 3;
                break;
            case 0x1E:
                opASL(Addressing.AbsoluteX);
                programCounter += 3;
                break;
            case 0x0A:
                opASL();
                programCounter += 1;
                break;
            case 0x4A:
                opLSR();
                programCounter += 1;
                break;
            case 0x46:
                opLSR(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0x56:
                opLSR(Addressing.ZeroPageX);
                programCounter += 2;
                break;
            case 0x4E:
                opLSR(Addressing.Absolute);
                programCounter += 3;
                break;
            case 0x5E:
                opLSR(Addressing.AbsoluteX);
                programCounter += 3;
                break;
            case 0x6A:
                opROR();
                programCounter += 1;
                break;
            case 0x66:
                opROR(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0x76:
                opROR(Addressing.ZeroPageX);
                programCounter += 2;
                break;
            case 0x6E:
                opROR(Addressing.Absolute);
                programCounter += 3;
                break;
            case 0x7E:
                opROR(Addressing.AbsoluteX);
                programCounter += 3;
                break;
            case 0x2A:
                opROL();
                programCounter += 1;
                break;
            case 0x26:
                opROL(Addressing.ZeroPage);
                programCounter += 2;
                break;
            case 0x36:
                opROL(Addressing.ZeroPageX);
                programCounter += 2;
                break;
            case 0x2E:
                opROL(Addressing.Absolute);
                programCounter += 3;
                break;
            case 0x3E:
                opROL(Addressing.AbsoluteX);
                programCounter += 3;
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
            case 0xF6: // (2バイト/6サイクル)
                opINC(Addressing.ZeroPageX);
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
            case 0xD6:
                opDEC(Addressing.ZeroPageX);
                programCounter += 2;
                break;
            case 0xCE:
                opDEC(Addressing.Absolute);
                programCounter += 3;
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
            case 0x70:
                opBVS();
                programCounter += 2;
                break;
            case 0x50:
                opBVC();
                programCounter += 2;
                break;
            case 0x30:
                opBMI();
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
            case 0x40:
                opRTI();
                //programCounter += 1;
                break;
            case 0x4C:
                opJMP_Abs();
                //programCounter += 3;// FIXME: pcインクリメントしないといかん気がする→確認
                break;
            case 0x6C:
                opJMP_Indirect();
                //programCounter += 3;// FIXME: pcインクリメントしないといかん気がする→確認
                break;
            case 0x38:
                opSEC();
                programCounter++;
                break;
            case 0xF8: // SED ファミコン用6502ではフラグ変更のみ
                opSED();
                programCounter += 1;
                break;
            case 0x18:
                opCLC();
                programCounter++;
                break;
            case 0xD8: // CLD ファミコン用6502ではフラグ変更のみ
                opCLD();
                programCounter += 1;
                break;
            case 0xB8:
                opCLV();
                programCounter++;
                break;
            case 0xEB: // SBC ※拡張命令
                opSBC(Addressing.Immediate);
                programCounter+= 2;
                break;
            case 0xC7: // DCM(DCP) ※拡張命令
                opDCM(Addressing.ZeroPage);
                programCounter+= 2;
                break;
            case 0xD7: // DCM(DCP) ※拡張命令
                opDCM(Addressing.ZeroPageX);
                programCounter+= 2;
                break;
            case 0xCF: // DCM(DCP) ※拡張命令
                opDCM(Addressing.Absolute);
                programCounter+= 3;
                break;
            case 0xDF: // DCM(DCP) ※拡張命令
                opDCM(Addressing.AbsoluteX);
                programCounter+= 3;
                break;
            case 0xDB: // DCM(DCP) ※拡張命令
                opDCM(Addressing.AbsoluteY);
                programCounter+= 3;
                break;
            case 0xC3: // DCM(DCP) ※拡張命令
                opDCM(Addressing.IndirectX);
                programCounter+= 2;
                break;
            case 0xD3: // DCM(DCP) ※拡張命令
                opDCM(Addressing.Indirect_Y);
                programCounter+= 2;
                break;
            case 0xE7: // ISC ※拡張命令
                opISC(Addressing.ZeroPage);
                programCounter+= 2;
                break;
            case 0xF7: // ISC(ISB) ※拡張命令
                opISC(Addressing.ZeroPageX);
                programCounter+= 2;
                break;
            case 0xEF: // ISC(ISB) ※拡張命令
                opISC(Addressing.Absolute);
                programCounter+= 3;
                break;
            case 0xFF: // ISC(ISB) ※拡張命令
                opISC(Addressing.AbsoluteX);
                programCounter+= 3;
                break;
            case 0xFB: // ISC(ISB) ※拡張命令
                opISC(Addressing.AbsoluteY);
                programCounter+= 3;
                break;
            case 0xE3: // ISC(ISB) ※拡張命令
                opISC(Addressing.IndirectX);
                programCounter+= 2;
                break;
            case 0xF3: // ISC(ISB) ※拡張命令
                opISC(Addressing.Indirect_Y);
                programCounter+= 2;
                break;
            case 0xEA:
                // NOP
                programCounter++;
                break;
            case 0x03:
                // ASO/SLO 本来 未定義命令
                // memory = shift left memory, A = A OR memory
                opASO(Addressing.IndirectX);
                programCounter+= 2;
                break;
            case 0x07:
                // ASO/SLO 本来 未定義命令
                // memory = shift left memory, A = A OR memory
                opASO(Addressing.ZeroPage);
                programCounter+= 2;
                break;
            case 0x0F:
                // ASO/SLO 本来 未定義命令
                // memory = shift left memory, A = A OR memory
                opASO(Addressing.Absolute);
                programCounter+= 3;
                break;
            case 0x13:
                // ASO/SLO 本来 未定義命令
                // memory = shift left memory, A = A OR memory
                opASO(Addressing.Indirect_Y);
                programCounter+= 2;
                break;
            case 0x17:
                // ASO/SLO 本来 未定義命令
                // memory = shift left memory, A = A OR memory
                opASO(Addressing.ZeroPageX);
                programCounter+= 2;
                break;
            case 0x1B:
                // ASO/SLO 本来 未定義命令
                // memory = shift left memory, A = A OR memory
                opASO(Addressing.AbsoluteY);
                programCounter+= 3;
                break;
            case 0x1F:
                // ASO/SLO 本来 未定義命令
                // memory = shift left memory, A = A OR memory
                opASO(Addressing.AbsoluteX);
                programCounter+= 3;
                break;
            case 0x23:
                // RLA 本来 未定義命令
                // memory = rotate left memory, A = A AND memory
                opRLA(Addressing.IndirectX);
                programCounter+= 2;
                break;
            case 0x27:
                // RLA 本来 未定義命令
                // memory = rotate left memory, A = A AND memory
                opRLA(Addressing.ZeroPage);
                programCounter+= 2;
                break;
            case 0x2F:
                // RLA 本来 未定義命令
                // memory = rotate left memory, A = A AND memory
                opRLA(Addressing.Absolute);
                programCounter+= 3;
                break;
            case 0x33:
                // RLA 本来 未定義命令
                // memory = rotate left memory, A = A AND memory
                opRLA(Addressing.Indirect_Y);
                programCounter+= 2;
                break;
            case 0x37:
                // RLA 本来 未定義命令
                // memory = rotate left memory, A = A AND memory
                opRLA(Addressing.ZeroPageX);
                programCounter+= 2;
                break;
            case 0x3B:
                // RLA 本来 未定義命令
                // memory = rotate left memory, A = A AND memory
                opRLA(Addressing.AbsoluteY);
                programCounter+= 3;
                break;
            case 0x3F:
                // RLA 本来 未定義命令
                // memory = rotate left memory, A = A AND memory
                opRLA(Addressing.AbsoluteX);
                programCounter+= 3;
                break;
            case 0x43:
                // SRE/LSE 本来 未定義命令
                // memory = shift right memory, A = A EOR memory
                opLSE(Addressing.IndirectX);
                programCounter+= 2;
                break;
            case 0x47:
                // SRE/LSE 本来 未定義命令
                // memory = shift right memory, A = A EOR memory
                opLSE(Addressing.ZeroPage);
                programCounter+= 2;
                break;
            case 0x4F:
                // SRE/LSE 本来 未定義命令
                // memory = shift right memory, A = A EOR memory
                opLSE(Addressing.Absolute);
                programCounter+= 3;
                break;
            case 0x53:
                // SRE/LSE 本来 未定義命令
                // memory = shift right memory, A = A EOR memory
                opLSE(Addressing.Indirect_Y);
                programCounter+= 2;
                break;
            case 0x57:
                // SRE/LSE 本来 未定義命令
                // memory = shift right memory, A = A EOR memory
                opLSE(Addressing.ZeroPageX);
                programCounter+= 2;
                break;
            case 0x5B:
                // SRE/LSE 本来 未定義命令
                // memory = shift right memory, A = A EOR memory
                opLSE(Addressing.AbsoluteY);
                programCounter+= 3;
                break;
            case 0x5F:
                // SRE/LSE 本来 未定義命令
                // memory = shift right memory, A = A EOR memory
                opLSE(Addressing.AbsoluteX);
                programCounter+= 3;
                break;
            case 0x63:
                // SRE/LSE 本来 未定義命令
                // memory = shift right memory, A = A EOR memory
                opRRA(Addressing.IndirectX);
                programCounter+= 2;
                break;
            case 0x67:
                // SRE/LSE 本来 未定義命令
                // memory = shift right memory, A = A EOR memory
                opRRA(Addressing.ZeroPage);
                programCounter+= 2;
                break;
            case 0x6F:
                // SRE/LSE 本来 未定義命令
                // memory = shift right memory, A = A EOR memory
                opRRA(Addressing.Absolute);
                programCounter+= 3;
                break;
            case 0x73:
                // SRE/LSE 本来 未定義命令
                // memory = shift right memory, A = A EOR memory
                opRRA(Addressing.Indirect_Y);
                programCounter+= 2;
                break;
            case 0x77:
                // SRE/LSE 本来 未定義命令
                // memory = shift right memory, A = A EOR memory
                opRRA(Addressing.ZeroPageX);
                programCounter+= 2;
                break;
            case 0x7B:
                // SRE/LSE 本来 未定義命令
                // memory = shift right memory, A = A EOR memory
                opRRA(Addressing.AbsoluteY);
                programCounter+= 3;
                break;
            case 0x7F:
                // SRE/LSE 本来 未定義命令
                // memory = shift right memory, A = A EOR memory
                opRRA(Addressing.AbsoluteX);
                programCounter+= 3;
                break;
            case 0x1A:
            case 0x3A:
            case 0x5A:
            case 0x7A:
            case 0xDA:
            case 0xFA:
                // 未実装2バイトNOP
                programCounter += 1;
                break;
            case 0x0C:
            case 0x1C:
            case 0x3C:
            case 0x5C:
            case 0x7C:
            case 0xDC:
            case 0xFC:
                // 未実装3バイトNOP
                programCounter += 3;
                break;
            case 0x04:
            case 0x44:
            case 0x64:
            case 0x14:
            case 0x34:
            case 0x54:
            case 0x74:
            case 0xD4:
            case 0xF4:
            case 0x80:
                // 未実装2バイトNOP
                programCounter += 2;
                break;
            default:
                System.out.println(Integer.toHexString(opcode & 0xFF));
                break;
        }
    }
}
