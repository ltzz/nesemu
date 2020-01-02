package system;

import java.awt.image.BufferedImage;

public class DrawTask {
    public static BufferedImage refreshFrameBuffer(NESSystem system, int w, int h){
        BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        try {
            for(int y = 0; y < 20; y++) {
                for (int x = 0; x < 320; x++) {
                    int value = (system.ram.CHR_ROM[y * 320 + x] & 0xFF) << 16
                            | (system.ram.CHR_ROM[y * 320 + x] & 0xFF) << 8
                            | (system.ram.CHR_ROM[y * 320 + x] & 0xFF);
                    bufferedImage.setRGB(x, y, value);
                }
            }

            for(int y = 0; y < 64; y++) {
                for (int x = 0; x < 256; x++) {
                    int value = (system.ppu.ppuRam[y * 256 + x] & 0xFF) << 16
                            | (system.ppu.ppuRam[y * 256 + x] & 0xFF) << 8
                            | (system.ppu.ppuRam[y * 256 + x] & 0xFF);
                    bufferedImage.setRGB(x, y + 20, value);
                }
            }

            for(int y = 0; y < 240; y++) {
                for (int x = 0; x < 256; x++) {
                    int value = (system.frameBuffer[y * 256 + x] & 0xFF) << 16
                            | (system.frameBuffer[y * 256 + x] & 0xFF) << 8
                            | (system.frameBuffer[y * 256 + x] & 0xFF);
                    bufferedImage.setRGB(x, y + 100, value);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bufferedImage;
    }
}
