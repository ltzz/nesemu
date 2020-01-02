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

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                system.systemExecute();
                canvas.screenBuffer = DrawTask.refreshFrameBuffer(system, w, h);
                canvas.paint(canvas.getGraphics());
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, 1000, 50);

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
