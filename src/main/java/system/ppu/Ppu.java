package system.ppu;

import system.Ram;

public class Ppu {
    public byte[] ppuRam;
    public byte[] ppuReg;
    public byte[] ppuCHR_ROM;
    public int ppuAddrCount; //TODO: 直せたら直す
    public int ppuAddr;
    public byte[] frameBuffer;

    public Ppu(byte[] frameBuffer){
        ppuReg = new byte[8];
        ppuRam = new byte[0x4000]; // TODO: 今は容量適当
        ppuAddrCount = 0;
        this.frameBuffer = frameBuffer;
    }

    public void IOAccess(){
        System.out.println("ppu_addr " + Integer.toHexString(ppuAddr));
        System.out.println("ppu_data " + Integer.toHexString(ppuReg[7] & 0Xff));
    }

    public void writePpuAddr(){
        if( ppuAddrCount == 0 ){
            ppuAddr = (ppuReg[6] & 0xFF) << 8;
        } else if( ppuAddrCount == 1 ){
            ppuAddr = ppuAddr + (ppuReg[6] & 0xFF);
            IOAccess();
        }
        ppuAddrCount = (ppuAddrCount + 1) % 2;
    }

    public void writePpuData(){
        ppuRam[ppuAddr] = ppuReg[7];
        ppuAddr += 1; // TODO: $2000の値によって32byteインクリメント
        IOAccess();
    }

    public void draw(){
        for(int addr = 0x2000; addr < 0x2200; ++addr){ // TODO: 後で範囲直す
            int nameIndex = ppuRam[addr];
            if( nameIndex > 0 ) {
                byte[] colorTable64 = new byte[64];
                final int bgOffsetAddr = (ppuReg[0] & 0x10) > 0 ? 1000 : 0;
                for(int chrIndex = 0; chrIndex < 8; chrIndex++){ // 縦
                    byte chrValue = ppuCHR_ROM[bgOffsetAddr + nameIndex * 16 + 8 + chrIndex];
                    for(int yIndex = 0; yIndex < 8; ++yIndex) {
                        colorTable64[yIndex * 8 + chrIndex] += (chrValue & (1 << yIndex)) >> yIndex;
                    }
                }
                
                for(int chrIndex = 0; chrIndex < 8; chrIndex++){ // 横
                    byte chrValue = ppuCHR_ROM[bgOffsetAddr + nameIndex * 16 + chrIndex];
                    for(int xIndex = 0; xIndex < 8; ++xIndex){
                        colorTable64[chrIndex * 8 + xIndex] += (chrValue & (1 << xIndex)) >> xIndex;
                    }
                }
                for(int colorTableIndex = 0; colorTableIndex < 64; colorTableIndex++){
                        int offsetX = (addr - 0x2000) % (256 / 8);
                        int offsetY = (addr - 0x2000) / (256 / 8);
                        int x = offsetX * 8 + (colorTableIndex % 8);
                        int y = offsetY * 8 + (colorTableIndex / 8);
                        int tmp = colorTable64[colorTableIndex];
                        byte value = (byte)(tmp * 255 / 3);
                        frameBuffer[y * 256 + x] = value;
                }
            }
        }
    }

    public void nextStep(){
        draw();
    }
}
