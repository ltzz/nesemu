import javafx.application.Application;
import javafx.stage.Stage;
import system.NESSystem;

import javax.swing.*;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;


public class Main extends Application {

    static int w = 800, h = 600;

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
    }


    static class ScreenCanvas extends Canvas {

        public ScreenCanvas() {
            // キャンバスの背景を白に設定
            setBackground(Color.white);
        }

        public void paint(Graphics g) {
        }

    }
}
