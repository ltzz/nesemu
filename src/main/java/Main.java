import javafx.application.Application;
import javafx.stage.Stage;
import system.DrawTask;
import system.NESSystem;
import ui.DebugWindow;
import ui.MainWindow;

import java.util.Timer;
import java.util.TimerTask;


public class Main extends Application {

    static int w = 400, h = 500;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        run();
    }

    public static void run() {
        NESSystem system = new NESSystem();

        MainWindow mainWindow = new MainWindow("testNES", 400, 500, system.joyPad);
        DebugWindow debugWindow = new DebugWindow("Debug Window", 256, 100);

        DrawTask drawTask = new DrawTask(w, h);

        // debug
        ROMNestestTest.run();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                for (int i = 0; i < 1; ++i) { // TODO: サイクル数計算する
                    system.systemExecute();
                }
                drawTask.refreshFrameBuffer(system, w, h, mainWindow.screenBuffer);
                mainWindow.refreshCanvas();
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, 500, 16); // 60Hz

    }
}