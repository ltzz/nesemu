package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public final class DebugWindow extends JFrame {
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

    public DebugWindow(String title, int windowWidth, int windowHeight){
        super("Debug Window");
        setSize(windowWidth,windowHeight);
        setResizable(false);
        setLocationRelativeTo(null);


        BufferedImage screenBuffer = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);
        this.screenBuffer = screenBuffer;
        ScreenCanvas canvas = new ScreenCanvas(screenBuffer);
        this.canvas = canvas;

        JPanel pane = new JPanel();
        getContentPane().add(pane);

        canvas.setPreferredSize(new Dimension(windowWidth, windowHeight));
        pane.add(canvas);

        setVisible(true);
    }
}
