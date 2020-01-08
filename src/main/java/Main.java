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

        final int[] count = {0};
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (count[0] == 0) {
                    system.ppu.ppuReg[2] = (byte) (system.ppu.ppuReg[2] | 0x80); // TODO: 暫定処理
                    count[0] = (count[0] + 1) % 3;
                    system.systemExecute();
                    system.ppu.nextStep();
                    drawTask.refreshFrameBuffer(system, w, h, mainWindow.screenBuffer);
                    mainWindow.refreshCanvas();
                } else {
                    system.ppu.ppuReg[2] = (byte) (system.ppu.ppuReg[2] & 0x7F); // TODO: 暫定処理
                    count[0] = (count[0] + 1) % 3;
                    for (int i = 0; i < 5000; ++i) {
                        system.systemExecute();
                    }
                }
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, 1000, 10);

    }
}