package system.joypad;

public final class JoyPad {
    public boolean buttonA;
    public boolean buttonB;
    public boolean buttonSelect;
    public boolean buttonStart;
    public boolean buttonUp;
    public boolean buttonDown;
    public boolean buttonLeft;
    public boolean buttonRight;

    public int readCount = 0;

    public void buttonResetFromIO(){
        readCount = 0;
    }

    public boolean buttonReadFromIO(){
        readCount++;
        if( 1 == readCount ) {
            return buttonA;
        }
        else if( 2 == readCount ) {
            return buttonB;
        }
        else if( 3 == readCount ) {
            return buttonSelect;
        }
        else if( 4 == readCount ) {
            return buttonStart;
        }
        else if( 5 == readCount ) {
            return buttonUp;
        }
        else if( 6 == readCount ) {
            return buttonDown;
        }
        else if( 7 == readCount ) {
            return buttonLeft;
        }
        else if( 8 == readCount ) {
            return buttonRight;
        }
        return false;
    }

}
