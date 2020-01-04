package system;

import java.awt.image.BufferedImage;

public final class DrawTask {
    int[] tmpRGB;

    public DrawTask(int w, int h){
        tmpRGB = new int[w * h];
    }

    public BufferedImage refreshFrameBuffer(NESSystem system, int w, int h){
        BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        int[] tmpRGB = new int[w * h];
        try {
            for(int y = 0; y < 20; y++) {
                final int yCacheIndex = y * 320;
                final int yCache = y * w;
                for (int x = 0; x < 320; x++) {
                    final int orginValue = (system.ram.CHR_ROM[yCacheIndex + x] & 0xFF);
                    final int value = orginValue << 16
                            | orginValue << 8
                            | orginValue;
                    tmpRGB[yCache + x] = value;
                }
            }

            for(int y = 0; y < 64; y++) {
                final int yCacheIndex = y * 256;
                final int yCache = (y + 20) * w;
                for (int x = 0; x < 256; x++) {
                    final int orginValue = (system.ppu.ppuRam[yCacheIndex + x] & 0xFF);
                    final int value = orginValue << 16
                            | orginValue << 8
                            | orginValue;
                    tmpRGB[yCache + x] = value;
                }
            }


            for(int y = 0; y < 240; y++) {
                final int yCacheIndex = y * 256;
                final int yCache = (y + 100) * w;
                for (int x = 0; x < 256; x++) {
                    final int orginValue = (system.frameBuffer[yCacheIndex + x] & 0xFF) ;
                    final int value = orginValue << 16
                            | orginValue << 8
                            | orginValue;
                    tmpRGB[yCache + x] = value;
                }
            }
            bufferedImage.setRGB(0, 0, w, h, tmpRGB, 0, w);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bufferedImage;
    }
}
