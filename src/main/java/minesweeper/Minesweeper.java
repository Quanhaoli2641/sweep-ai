package minesweeper;

import java.util.Arrays;

import javax.imageio.ImageIO;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Minesweeper extends Thread {

    public MineStarter mineStarter;
    boolean readyToRun = false;

    int topX, topY, botX, botY;

    final int offsetX = 15;
    final int offsetY = -101;

    final int botOffsetX = 15;
    final int botOffsetY = 15;

    MineReader mineInterface;
    HillClimbingMineAI hillClimbingMineAI;
    PatternMineAI patternMineAI;


    public void run() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e1) {
        }

        System.out.println("Starting AI...");
        locate();

        if (readyToRun) {
            try {
                updateFieldImage();
            } catch (AWTException e) {
                e.printStackTrace();
            }
            if(mineStarter.input == 1) {
                hillClimbingMineAI = new HillClimbingMineAI(this);
                hillClimbingMineAI.initialize();
                hillClimbingMineAI.mainLoop();
            }
            if(mineStarter.input == 2){
                patternMineAI = new PatternMineAI(this);
                patternMineAI.initialize();
                patternMineAI.mainLoop();
            }
        }
    }

    public void updateFieldImage() throws AWTException {
        int height = (botY - botOffsetY) - (topY - offsetY);
        int width = (botX - botOffsetX) - (topX + offsetX);

        Rectangle window = new Rectangle(topX + offsetX, topY - offsetY, width, height);
        Robot robot = new Robot();

        BufferedImage screenShot = robot.createScreenCapture(window);

        try {
            File image = new File("State.png");
            ImageIO.write(screenShot, "png", image);
        } catch (IOException e) {}
    }

    public void locate() {
        System.out.println("Locating Minesweeper...");
        String windowName = "Minesweeper X";
        int[] rect;
        readyToRun = true;
        try {
            rect = GetWindowRect.getRect(windowName);
            System.out.println("Corner of Minesweeper:\n" + windowName + Arrays.toString(rect));
            topX = rect[0];
            topY = rect[1];
            botX = rect[2];
            botY = rect[3];

        } catch (GetWindowRect.WindowNotFoundException e) {
            System.out.println("Open Minesweeper and try again");
            readyToRun = false;
        } catch (GetWindowRect.GetWindowRectException e) {
            readyToRun = false;
        }
    }
}
