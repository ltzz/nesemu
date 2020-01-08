package ui;

import system.joypad.JoyPad;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

public final class MainWindow extends JFrame implements KeyListener {
    public BufferedImage screenBuffer;
    private ScreenCanvas canvas;
    private JoyPad joyPad;

    @Override
    public void keyTyped(KeyEvent keyEvent) {
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        System.out.println("key pressed: " + keyEvent.getKeyCode());
        switch ( keyEvent.getKeyCode() ){
            case KeyEvent.VK_J:
                joyPad.buttonB = true;
                break;
            case KeyEvent.VK_K:
                joyPad.buttonA = true;
                break;
            case KeyEvent.VK_ENTER:
                joyPad.buttonStart = true;
                break;
            case KeyEvent.VK_SPACE:
                joyPad.buttonSelect = true;
                break;
            case KeyEvent.VK_W:
                joyPad.buttonUp = true;
                break;
            case KeyEvent.VK_S:
                joyPad.buttonDown = true;
                break;
            case KeyEvent.VK_A:
                joyPad.buttonLeft = true;
                break;
            case KeyEvent.VK_D:
                joyPad.buttonRight = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        switch ( keyEvent.getKeyCode() ){
            case KeyEvent.VK_J:
                joyPad.buttonB = false;
                break;
            case KeyEvent.VK_K:
                joyPad.buttonA = false;
                break;
            case KeyEvent.VK_ENTER:
                joyPad.buttonStart = false;
                break;
            case KeyEvent.VK_SPACE:
                joyPad.buttonSelect = false;
                break;
            case KeyEvent.VK_W:
                joyPad.buttonUp = false;
                break;
            case KeyEvent.VK_S:
                joyPad.buttonDown = false;
                break;
            case KeyEvent.VK_A:
                joyPad.buttonLeft = false;
                break;
            case KeyEvent.VK_D:
                joyPad.buttonRight = false;
                break;
        }
    }

    private static final class ScreenCanvas extends Canvas {
        BufferedImage screenBuffer;

        public ScreenCanvas(BufferedImage screenBuffer) {
            this.screenBuffer = screenBuffer;
        }

        public void paint(Graphics g) {
            try {
                Graphics2D g2d = (Graphics2D) g;
                g2d.drawImage(screenBuffer, null, 10, 10);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public MainWindow(String title, int windowWidth, int windowHeight, JoyPad joyPad){
        super(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(windowWidth, windowHeight);
        setLocationRelativeTo(null);

        this.joyPad = joyPad;

        BufferedImage screenBuffer = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);
        this.screenBuffer = screenBuffer;
        ScreenCanvas canvas = new ScreenCanvas(screenBuffer);
        this.canvas = canvas;

        JPanel pane = new JPanel();
        getContentPane().add(pane);

        canvas.setPreferredSize(new Dimension(windowWidth, windowHeight));
        pane.add(canvas);

        addKeyListener(this);

        setVisible(true);

    }

    public void refreshCanvas(){
        canvas.paint(canvas.getGraphics());
    }

}
