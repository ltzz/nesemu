import javafx.application.Application;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.stage.Stage;
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


public class Main extends Application {

    static int w = 400, h = 300;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        run();
    }

    public static void run() {
        NESSystem system = new NESSystem();
        system.systemExecute();

        JFrame frame = new JFrame("testNES");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(w, h);
        frame.setLocationRelativeTo(null);

        ScreenCanvas canvas = new ScreenCanvas();

        JPanel pane = new JPanel();
        frame.getContentPane().add(pane);

        canvas.setPreferredSize(new Dimension(w, h));
        pane.add(canvas);

        frame.setVisible(true);

        try {
            BufferedImage bufferedImage = new BufferedImage(320, 300, BufferedImage.TYPE_INT_RGB);
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

            canvas.screenBuffer = bufferedImage;
            // ImageIO.write(bufferedImage, "png", new File("debug_out.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static class ScreenCanvas extends Canvas {

        public BufferedImage screenBuffer = null;

        public ScreenCanvas() {
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
