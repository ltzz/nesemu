package system.ppu;

import java.util.Arrays;

public final class Ppu {
    public byte[] ppuRam;
    public byte[] ppuOAM;
    public byte[] ppuReg;
    public byte[] ppuCHR_ROM;
    public int ppuAddrCount; //TODO: 直せたら直す
    public int ppuAddr;
    public int[] frameBuffer;
    public final byte[][] bgColorTables = new byte[0x1000][64];
    public final byte[][] spColorTables = new byte[0x1000][64];
    public final byte[] attributeTableCache =  new byte[16 * 16]; // 各16x16pixelの画面領域で使うパレット
    public byte timing = 0; // TODO: 正式なScanline変数にする
    // TODO: PPURAMWrite作る ミラー領域とかの考慮のため

    public Ppu(int[] frameBuffer){
        ppuReg = new byte[8];
        ppuRam = new byte[0x4000]; // TODO: 今は容量適当
        ppuOAM = new byte[0x100];
        ppuAddrCount = 0;
        this.frameBuffer = frameBuffer;
    }

    public void IOAccess(){
        if( false ) {
            System.out.println("ppu_addr " + Integer.toHexString(ppuAddr));
            System.out.println("ppu_data " + Integer.toHexString(ppuReg[7] & 0xFF));
        }
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

    public byte readPPUData(){
        return ppuRam[ppuAddr];
    }

    public void writePPUData(){
        if(ppuAddr == 0x3F00 || ppuAddr == 0x3F10){
            ppuRam[0x3F00] = ppuReg[7];
            ppuRam[0x3F10] = ppuReg[7]; // mirror
        }
        else if(ppuAddr == 0x3F04 || ppuAddr == 0x3F14){
            ppuRam[0x3F04] = ppuReg[7];
            ppuRam[0x3F14] = ppuReg[7]; // mirror
        }
        else if(ppuAddr == 0x3F08 || ppuAddr == 0x3F18){
            ppuRam[0x3F08] = ppuReg[7];
            ppuRam[0x3F18] = ppuReg[7]; // mirror
        }
        else if(ppuAddr == 0x3F0C || ppuAddr == 0x3F1C){
            ppuRam[0x3F0C] = ppuReg[7];
            ppuRam[0x3F1C] = ppuReg[7]; // mirror
        }
        else {
            ppuRam[ppuAddr] = ppuReg[7];
        }
        int addressInc = 1;
        if((ppuReg[0] & 0x04) > 0){ // $2000の値によって32byteインクリメント
            addressInc = 32;
        }
        ppuAddr += addressInc;
        IOAccess();
    }

    public void spriteDMA(byte addressUpper, byte[] cpuRam){
        System.arraycopy(cpuRam, ((addressUpper & 0xFF) << 8), ppuOAM, 0, 0x100);
    }

    public void refreshColorTables(){
        final int bgOffsetAddr = ((ppuReg[0] & 0x10) > 0) ? 0x1000 : 0;
        final int spOffsetAddr = ((ppuReg[0] & 0x08) > 0) ? 0x1000 : 0;
        for(int tileId=0; tileId < 0x100; ++tileId){
            final int tileIdOffsetAddress = tileId * 16;

            // 初期化
            Arrays.fill(bgColorTables[tileId], (byte) 0x00);

            for(int chrIndex = 0; chrIndex < 8; chrIndex++){ // 前半
                final byte chrValue = ppuCHR_ROM[bgOffsetAddr + tileIdOffsetAddress + chrIndex];
                final int yCacheIndex = chrIndex * 8;
                for(int xIndex = 0; xIndex < 8; ++xIndex){
                    final int shift = 7 - xIndex;
                    bgColorTables[tileId][yCacheIndex + xIndex] += (chrValue & (1 << shift)) >> shift;
                }
            }
            for(int chrIndex = 0; chrIndex < 8; chrIndex++){ // 後半
                byte chrValue = ppuCHR_ROM[bgOffsetAddr + tileIdOffsetAddress + 8 + chrIndex];
                final int yCacheIndex = chrIndex * 8;
                for(int xIndex = 0; xIndex < 8; ++xIndex){
                    final int shift = 7 - xIndex;
                    bgColorTables[tileId][yCacheIndex + xIndex] += ((chrValue & (1 << shift)) >> shift) * 2;
                }
            }
        }
        for(int tileId=0; tileId < 0x100; ++tileId){
            final int tileIdOffsetAddress = tileId * 16;

            // 初期化
            Arrays.fill(spColorTables[tileId], (byte) 0x00);

            for(int chrIndex = 0; chrIndex < 8; chrIndex++){ // 前半
                final byte chrValue = ppuCHR_ROM[spOffsetAddr + tileIdOffsetAddress + chrIndex];
                final int yCacheIndex = chrIndex * 8;
                for(int xIndex = 0; xIndex < 8; ++xIndex){
                    final int shift = 7 - xIndex;
                    spColorTables[tileId][yCacheIndex + xIndex] += (chrValue & (1 << shift)) >> shift;
                }
            }
            for(int chrIndex = 0; chrIndex < 8; chrIndex++){ // 後半
                byte chrValue = ppuCHR_ROM[spOffsetAddr + tileIdOffsetAddress + 8 + chrIndex];
                final int yCacheIndex = chrIndex * 8;
                for(int xIndex = 0; xIndex < 8; ++xIndex){
                    final int shift = 7 - xIndex;
                    spColorTables[tileId][yCacheIndex + xIndex] += ((chrValue & (1 << shift)) >> shift) * 2;
                }
            }
        }
    }

    private byte getBGColorId(int palette, int num){
        final int paletteTableBGAddr = 0x3F00;
        return ppuRam[paletteTableBGAddr + 4 * palette + num];
    }

    private byte getSPColorId(int palette, int num){
        final int paletteTableSPAddr = 0x3F10;
        return ppuRam[paletteTableSPAddr + 4 * palette + num];
    }

    private int convertToRGB24(int octal){
        int b = octal & 0x07;
        int g = (octal & (0x07 << 3)) >> 3;
        int r = (octal & (0x07 << 6)) >> 6;
        int RGB = (((b * 255) / 7) << 16) |
                (((g * 255) / 7) << 8) |
                ((r * 255) / 7);
        return RGB;
    }

    private int convertToGrayscale24(byte value){
        return value << 16
                | value << 8
                | value;
    }

    private int getColor(byte color){
        final int colorPaletteOctal[] = {
                0333,0014,0006,0326,0403,0503,0510,0420,0320,0120,0031,0040,0022,0000,0000,0000,
                0555,0036,0027,0407,0507,0704,0700,0630,0430,0140,0040,0053,0044,0000,0000,0000,
                0777,0357,0447,0637,0707,0737,0740,0750,0660,0360,0070,0276,0077,0000,0000,0000,
                0777,0567,0657,0757,0747,0755,0764,0772,0773,0572,0473,0276,0467,0000,0000,0000
        };

        return convertToRGB24(colorPaletteOctal[color]);
    }

    public void refreshAttributeTable(){
        final int mainScreen = ppuReg[0] & 0x03;
        final int startAddr = 0x2000 + (mainScreen * 0x400);
        final int endAddr = startAddr + 32 * 30;
        final int attributeTableStartAddr = startAddr + 0x3C0;
        for (int attributeTableAddr = attributeTableStartAddr; attributeTableAddr < endAddr; attributeTableAddr++){
            final int value = ppuRam[attributeTableAddr] & 0xFF;
            final int topLeft = value & 0x03;
            final int topRight = (value & 0x0C) >> 2;
            final int bottomLeft = (value & 0x30) >> 4;
            final int bottomRight = (value & 0xC0) >> 6;
            final int attributeTableY = attributeTableAddr / 8;
            final int areaY = 2 * attributeTableY;
            final int attributeTableX = attributeTableAddr % 8;
            final int areaX = 2 * attributeTableX;
            attributeTableCache[areaY * 8 + areaX] = (byte)topLeft;
            attributeTableCache[areaY * 8 + areaX + 1] = (byte)topRight;
            attributeTableCache[(areaY + 1) * 8 + areaX] = (byte)bottomLeft;
            attributeTableCache[(areaY + 1) * 8 + areaX + 1] = (byte)bottomRight;
        }
    }

    public void drawScanLine(){
        final int blockWidth = (256 / 8);
        final int mainScreen = ppuReg[0] & 0x03;
        final int startAddr = 0x2000 + (mainScreen * 0x400);
        final int endAddr = startAddr + 32 * 30;
        final int startLineAddr = startAddr + timing * 8;
        final int endLineAddr = startLineAddr + 8;

        refreshAttributeTable();

        // BG描画
        for(int addr = startLineAddr; addr < endLineAddr; ++addr){
            final int tileId = ppuRam[addr];
            if( tileId > 0 ) {
                final int offsetBlockX = (addr - 0x2000) % blockWidth;
                final int offsetBlockY = (addr - 0x2000) / blockWidth;
                final int offsetX = offsetBlockX * 8;
                final int offsetY = offsetBlockY * 8;
                for(int colorTableIndex = 0; colorTableIndex < 64; colorTableIndex++){
                    final int x = offsetX + (colorTableIndex % 8);
                    final int y = offsetY + (colorTableIndex / 8);
                    final int attX = offsetX % 16;
                    final int attY = offsetY / 16;
                    final int palette = attributeTableCache[attX * 16 + attY];
                    final int tmp = bgColorTables[tileId][colorTableIndex] & 0xFF;
                    //final byte value = (byte)(tmp * 255 / 3);
                    //frameBuffer[y * 256 + x] = convertToGrayscale24(value);
                    frameBuffer[y * 256 + x] = getColor(getBGColorId(palette, tmp));
                }
            }
        }
    }

    public void draw(){ // TODO: scanlineで処理するように
        final int blockWidth = (256 / 8);
        final int mainScreen = ppuReg[0] & 0x03;
        final int startAddr = 0x2000 + (mainScreen * 0x400);
        final int endAddr = startAddr + 32 * 30;
        Arrays.fill(frameBuffer, 0);

        refreshAttributeTable();

        // BG描画
        for(int addr = startAddr; addr < endAddr; ++addr){
            final int tileId = ppuRam[addr];
            if( tileId > 0 ) {
                final int offsetBlockX = (addr - 0x2000) % blockWidth;
                final int offsetBlockY = (addr - 0x2000) / blockWidth;
                final int offsetX = offsetBlockX * 8;
                final int offsetY = offsetBlockY * 8;
                for(int colorTableIndex = 0; colorTableIndex < 64; colorTableIndex++){
                    final int x = offsetX + (colorTableIndex % 8);
                    final int y = offsetY + (colorTableIndex / 8);
                    final int attX = offsetX % 16;
                    final int attY = offsetY / 16;
                    final int palette = attributeTableCache[attX * 16 + attY];
                    final int tmp = bgColorTables[tileId][colorTableIndex] & 0xFF;
                    //final byte value = (byte)(tmp * 255 / 3);
                    //frameBuffer[y * 256 + x] = convertToGrayscale24(value);
                    frameBuffer[y * 256 + x] = getColor(getBGColorId(palette, tmp));
                }
            }
        }

        // Sprite描画
        for(int spriteAddr = 0; spriteAddr < 0x100; spriteAddr += 4) {
            int tileY       = ppuOAM[spriteAddr + 0] & 0xFF;
            int tileX       = ppuOAM[spriteAddr + 3] & 0xFF;
            if( tileY >= 240 || tileX >= 256 ){
                break;
            }

            int tileId = 0x00;
            if((ppuReg[0] & 0x20) > 0){ // スプライトサイズ8 * 16の場合
                tileId = ppuOAM[spriteAddr + 1] & 0xF7;
                // TODO: スプライト下半分の処理
                // tileId = (ppuOAM[spriteAddr + 1] & 0xF7) | 1;
            }
            else {
                tileId = ppuOAM[spriteAddr + 1] & 0xFF;
            }
            int attr    = ppuOAM[spriteAddr + 2] & 0xFF;
            if( tileId > 0 ) {
                final int palette = attr & 0x03;

                for(int colorTableIndex = 0; colorTableIndex < 64; colorTableIndex++){
                    final int x = tileX + (colorTableIndex % 8);
                    final int y = tileY + (colorTableIndex / 8);
                    final int tmp = spColorTables[tileId][colorTableIndex] & 0xFF;
                    //final byte value = (byte)(tmp * 255 / 3);
                    //frameBuffer[y * 256 + x] = convertToGrayscale24(value);
                    final byte colorId = getSPColorId(palette, tmp);
                    if( colorId > 0 ){ // 背景色ではない
                        int color = getColor(colorId);
                        frameBuffer[y * 256 + x] = color;
                    }
                }
            }
        }
    }

    public void nextStep(){
        refreshAttributeTable();
        refreshColorTables();
        draw();
        timing = (byte)((timing + 1) % 2);
        if((timing & 0xFF) >= 1){
            ppuReg[2] = (byte)(ppuReg[2] | 0x80); // TODO: 暫定処理
        }
        else {
            ppuReg[2] = (byte)(ppuReg[2] & 0x7F); // TODO: 暫定処理
        }
    }

    public void nextStepWithScanLine(){
        if(timing == 0) {
            refreshAttributeTable();
            refreshColorTables();
        }
        draw();
        timing = (byte)((timing + 1) % 256);
        if((timing & 0xFF) >= 240){
            ppuReg[2] = (byte)(ppuReg[2] | 0x80); // TODO: 暫定処理
        }
        else {
            ppuReg[2] = (byte)(ppuReg[2] & 0x7F); // TODO: 暫定処理
        }
    }
}
