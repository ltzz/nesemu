package system.ppu;

import system.Ram;

public final class Ppu {
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
        System.out.println("ppu_data " + Integer.toHexString(ppuReg[7] & 0xFF));
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
        int addressInc = 1;
        if((ppuReg[0] & 0x04) > 0){ // $2000の値によって32byteインクリメント
            addressInc = 32;
        }
        ppuAddr += addressInc;
        IOAccess();
    }

    public void draw(){
        final int blockWidth = (256 / 8);
        final int mainScreen = ppuReg[0] & 0x03;
        final int startAddr = 0x2000 + (mainScreen * 0x400);
        final int endAddr = 0x2400 + (mainScreen * 0x400);

        for(int addr = startAddr; addr < endAddr; ++addr){ // TODO: 後で範囲直す
            final int tileId = ppuRam[addr];
            final int tileIdOffsetAddress = tileId * 16;
            if( tileId > 0 ) {
                byte[] colorTable64 = new byte[64];
                final int bgOffsetAddr = (ppuReg[0] & 0x10) > 0 ? 1000 : 0;

                for(int chrIndex = 0; chrIndex < 8; chrIndex++){ // 前半
                    final byte chrValue = ppuCHR_ROM[bgOffsetAddr + tileIdOffsetAddress + chrIndex];
                    final int yCacheIndex = chrIndex * 8;
                    for(int xIndex = 0; xIndex < 8; ++xIndex){
                        final int shift = 7 - xIndex;
                        colorTable64[yCacheIndex + xIndex] += (chrValue & (1 << shift)) >> shift;
                    }
                }
                for(int chrIndex = 0; chrIndex < 8; chrIndex++){ // 後半
                    byte chrValue = ppuCHR_ROM[bgOffsetAddr + tileIdOffsetAddress + 8 + chrIndex];
                    final int yCacheIndex = chrIndex * 8;
                    for(int xIndex = 0; xIndex < 8; ++xIndex){
                        final int shift = 7 - xIndex;
                        colorTable64[yCacheIndex + xIndex] += (chrValue & (1 << shift)) >> shift;
                    }
                }
                final int offsetBlockX = (addr - 0x2000) % blockWidth;
                final int offsetBlockY = (addr - 0x2000) / blockWidth;
                final int offsetX = offsetBlockX * 8;
                final int offsetY = offsetBlockY * 8;
                for(int colorTableIndex = 0; colorTableIndex < 64; colorTableIndex++){
                    final int x = offsetX + (colorTableIndex % 8);
                    final int y = offsetY + (colorTableIndex / 8);
                    final int tmp = colorTable64[colorTableIndex];
                    final byte value = (byte)(tmp * 255 / 3);
                    frameBuffer[y * 256 + x] = value;
                }
            }
        }
    }

    public void nextStep(){
        draw();
    }
}
