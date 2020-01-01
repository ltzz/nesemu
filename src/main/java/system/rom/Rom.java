package system.rom;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class Rom {

    public byte[] PRG_ROM;
    public byte[] CHR_ROM;

    public Rom(String filename){
        loadPRGROM(filename);
    }

    public void loadPRGROM(String filename){
        File file = new File(filename);
        try {
            byte[] rom = Rom.loadFile(file);
            // TODO headerチェック
            int PRG_ROM_sizeKB = rom[4] * 16;
            int CHR_ROM_sizeKB = rom[5] * 8;

            PRG_ROM = Arrays.copyOfRange(rom, 0x10, 0x10 + PRG_ROM_sizeKB * 1024);
            int addr = 0x10 + PRG_ROM_sizeKB * 1024;
            CHR_ROM = Arrays.copyOfRange(rom, addr, addr + CHR_ROM_sizeKB * 1024);

        } catch (Exception e) {
            PRG_ROM = new byte[1];
            CHR_ROM = new byte[1];
        }
    }

    public static byte[] loadFile(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }
}
