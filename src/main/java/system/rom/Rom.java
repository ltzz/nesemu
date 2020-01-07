package system.rom;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public final class Rom {

    public byte[] PRG_ROM;
    public byte[] CHR_ROM;
    public int PRG_ROM_SIZE;

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
            int mirroring = rom[6] & 0x01;

            PRG_ROM_SIZE = PRG_ROM_sizeKB * 1024;

            byte[] tmpRom = Arrays.copyOfRange(rom, 0x10, 0x10 + PRG_ROM_sizeKB * 1024);
            if(mirroring > 0){
                // TODO: ちゃんとミラーされるようにコピーをやめる
                // TODO: アクセスの際に上位ビット無視すればミラーと同じ挙動？
                byte[] mirrorRom = new byte[32 * 1024];
                System.arraycopy(tmpRom, 0, mirrorRom, 0, tmpRom.length);
                if(tmpRom.length < 32 * 1024 && tmpRom.length + tmpRom.length <= 32 * 1024){
                    System.arraycopy(tmpRom, 0, mirrorRom, tmpRom.length, tmpRom.length);
                }
                PRG_ROM = mirrorRom;
            }
            else{
                PRG_ROM = tmpRom;
            }
            int addr = 0x10 + PRG_ROM_sizeKB * 1024;
            CHR_ROM = Arrays.copyOfRange(rom, addr, addr + CHR_ROM_sizeKB * 1024);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] loadFile(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }
}
