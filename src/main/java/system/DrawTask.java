package system;

import java.awt.image.BufferedImage;

public final class DrawTask {
    int[] tmpRGB;

    public DrawTask(int w, int h){
        tmpRGB = new int[w * h];
    }

    public BufferedImage refreshFrameBuffer(NESSystem system, int w, int h, BufferedImage bufferedImage){
        int[] tmpRGB = new int[w * h];
        try {

            // テーブル描画
            for(int chrIdx = 0; chrIdx < 0x100; chrIdx++) {
                for (int y = 0; y < 8; y++) {
                    for (int x = 0; x < 8; x++) {
                        final int originValue = (system.ppu.bgColorTables[chrIdx][y * 8 + x] & 0xFF) * 255 / 3;
                        final int value = originValue << 16
                                | originValue << 8
                                | originValue;
                        tmpRGB[((chrIdx / 32) * 8 + y) * 256 + ((chrIdx % 32) * 8 + x)] = value;
                    }
                }
            }

            for(int chrIdx = 0; chrIdx < 0x100; chrIdx++) {
                for (int y = 0; y < 8; y++) {
                    for (int x = 0; x < 8; x++) {
                        final int originValue = (system.ppu.spColorTables[chrIdx][y * 8 + x] & 0xFF) * 255 / 3;
                        final int value = originValue << 16
                                | originValue << 8
                                | originValue;
                        tmpRGB[((chrIdx / 32) * 8 + y + 64) * 256 + ((chrIdx % 32) * 8 + x)] = value;
                    }
                }
            }

            // RAM描画
            for(int y = 0; y < 64; y++) {
                final int yCacheIndex = y * 256;
                final int yCache = (y + 128) * w;
                for (int x = 0; x < 256; x++) {
                    final int orginValue = (system.ppu.ppuRam[yCacheIndex + x] & 0xFF);
                    final int value = orginValue << 16
                            | orginValue << 8
                            | orginValue;
                    tmpRGB[yCache + x] = value;
                }
            }

            // 実画面描画
            for(int y = 0; y < 240; y++) {
                final int yCacheIndex = y * 256;
                final int yCache = (y + 200) * w;
                for (int x = 0; x < 256; x++) {
                    //final int orginValue = (system.frameBuffer[yCacheIndex + x] & 0xFF) ;
                    //final int value = orginValue << 16
                    //        | orginValue << 8
                    //        | orginValue;
                    tmpRGB[yCache + x] = system.frameBuffer[yCacheIndex + x] ;
                }
            }
            bufferedImage.setRGB(0, 0, w, h, tmpRGB, 0, w);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bufferedImage;
    }
}
