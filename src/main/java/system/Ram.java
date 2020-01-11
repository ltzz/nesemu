package system;

import system.joypad.JoyPad;
import system.ppu.Ppu;
import system.rom.Rom;

public final class Ram {
    byte[] wram;
    Ppu ppu;
    byte[] apuIoReg;
    public byte[] PRG_ROM;
    public byte[] CHR_ROM;
    int PRG_ROM_SIZE;
    JoyPad joyPad;

    public Ram(Ppu ppu, Rom rom, JoyPad joyPad) {
        wram = new byte[0x800];
        this.ppu = ppu;
        apuIoReg = new byte[0x020];
        this.joyPad = joyPad;

        PRG_ROM = rom.PRG_ROM;
        CHR_ROM = rom.CHR_ROM;
        PRG_ROM_SIZE = rom.PRG_ROM_SIZE;
    };

    public byte getRAMValue(int address){
        if(0x0000 <= address && address < 0x2000){
            //WRAM MIRROR * 3
            return wram[address % 0x800];
        }
        else if (address < 0x2008){
            // ppu i/o
            if(address == 0x2002){
                return ppu.ppuReg[2];
            }
            else if(address == 0x2006){
                ppu.IOAccess();
            }
            else if(address == 0x2007){
                ppu.readPPUData();
                ppu.IOAccess();
            }
        }
        else if (address < 0x4000){
            // ppu i/o mirror * 1023
        }
        else if (address < 0x4020){
            // apu i/o, pad
            if( address == 0x4016 ){
                byte value = 0;
                value |= joyPad.buttonReadFromIO() ? 0x01 : 0x00;
                return value;
            }
        }
        else if (address < 0x6000){
            // exrom
        }
        else if (address < 0x8000){
            // exram
        }
        else if (address < 0xC000){
            // prg-rom low
            return PRG_ROM[(address - 0x8000) % PRG_ROM_SIZE];
        }
        else if (address <= 0xFFFF){
            // prg-ram high
            return PRG_ROM[(address - 0x8000) % PRG_ROM_SIZE];
        }
        else{
            // ppu
        }
        return 0x00;
    }

    public int getRAMValue16(int address){
        int lower, upper;
        lower = getRAMValue((address + 0) & 0xFFFF) & 0xFF;
        upper = getRAMValue((address + 1) & 0xFFFF) & 0xFF;
        final int value = (upper << 8) | lower;
        return value;
    }


    public int getRAMValueInPage(int address){
        int lower, upper;
        int page = address >> 8;
        lower = getRAMValue((page << 8) | ((address + 0) & 0xFF)) & 0xFF;
        upper = getRAMValue((page << 8) | ((address + 1) & 0xFF)) & 0xFF;
        final int value = (upper << 8) | lower;
        return value;
    }

    public int getRAMValue16ByAddress8(int address){
        int lower, upper;
        lower = getRAMValue((address + 0) & 0xFF) & 0xFF;
        upper = getRAMValue((address + 1) & 0xFF) & 0xFF;
        final int value = (upper << 8) | lower;
        return value;
    }

    public void setRAMValue(int address, byte value) {
        if (0x0000 <= address && address < 0x2000) {
            //WRAM MIRROR * 3
            wram[address % 0x800] = value;
        } else if (address < 0x2008) {
            // ppu i/o
            ppu.ppuReg[address - 0x2000] = value;
            if (address == 0x2006) {
                ppu.writePpuAddr();
            }
            else if(address == 0x2007) {
                ppu.writePPUData();
            }
            else if( address == 0x2003 ){
                final int a = 1;
            }
            else if( address == 0x2004 ){
                final int a = 1;
            }
            else if( address == 0x2005 ){
                final int a = 1;
            }
        } else if( address == 0x4014 ){
            ppu.spriteDMA(value, wram);
        } else if( address == 0x4016 ){
            joyPad.buttonResetFromIO();
        }
    }
}
