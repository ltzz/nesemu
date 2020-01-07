package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public final class DebugWindow {
    public BufferedImage screenBuffer;
    private ScreenCanvas canvas;

    private static final class ScreenCanvas extends Canvas {
        private BufferedImage screenBuffer;
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

    public DebugWindow(){
        final int windowWidth = 256;
        final int windowHeight = 100;
        JFrame frame = new JFrame("Debug Window");
        frame.setSize(windowWidth,windowHeight);
        frame.setLocationRelativeTo(null);

        BufferedImage screenBuffer = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);
        this.screenBuffer = screenBuffer;
        ScreenCanvas canvas = new ScreenCanvas(screenBuffer);
        this.canvas = canvas;

        JPanel pane = new JPanel();
        frame.getContentPane().add(pane);

        canvas.setPreferredSize(new Dimension(windowWidth, windowHeight));
        pane.add(canvas);

        frame.setVisible(true);
    }
}
