import javafx.application.Application;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.stage.Stage;
import system.DrawTask;
import system.NESSystem;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;


public class Main extends Application {

    static int w = 400, h = 400;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        run();
    }

    public static void run() {
        NESSystem system = new NESSystem();

        JFrame frame = new JFrame("testNES");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(w, h);
        frame.setLocationRelativeTo(null);

        ScreenCanvas canvas = new ScreenCanvas(system);

        JPanel pane = new JPanel();
        frame.getContentPane().add(pane);

        canvas.setPreferredSize(new Dimension(w, h));
        pane.add(canvas);

        frame.setVisible(true);

        DrawTask drawTask = new DrawTask(w, h);

        final int[] count = {0};
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if( count[0] == 0 ) {
                    system.ppu.ppuReg[2] = (byte)(system.ppu.ppuReg[2] | 0x80); // TODO: 暫定処理
                    count[0] = (count[0] + 1) % 500;
                    system.systemExecute();
                    system.ppu.nextStep();
                    canvas.screenBuffer = drawTask.refreshFrameBuffer(system, w, h);
                    canvas.paint(canvas.getGraphics());
                }
                else {
                    system.ppu.ppuReg[2] = (byte)(system.ppu.ppuReg[2] & 0x7F); // TODO: 暫定処理
                    count[0] = (count[0] + 1) % 500;
                    system.systemExecute();
                }
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, 1000, 10);

    }


    static class ScreenCanvas extends Canvas {

        public BufferedImage screenBuffer = null;
        NESSystem system;

        public ScreenCanvas(NESSystem system) {
            this.system = system;
            // キャンバスの背景を白に設定
            // setBackground(Color.white);
        }

        public void paint(Graphics g) {
            try {
                Graphics2D g2d = (Graphics2D) g;
                g2d.drawImage(screenBuffer, null, 10, 10);

                // ImageIO.write(bufferedImage, "png", new File("debug_out.png"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
