import javafx.application.Application;
import javafx.stage.Stage;
import rom.ROM;

import javax.swing.JFrame;
import java.io.File;

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
        File file = new File("./sample1.nes");
        try {
            byte[] rom = ROM.loadFile(file);

        } catch (Exception e) {

        }
        // JFrameのインスタンスを生成
        JFrame frame = new JFrame("お絵かきアプリ");
        // ウィンドウを閉じたらプログラムを終了する
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // ウィンドウのサイズ・初期位置
        frame.setSize(w, h);
        frame.setLocationRelativeTo(null);
        // setBounds(x, y, w, h);

        // ウィンドウを表示
        frame.setVisible(true);
    }
}
