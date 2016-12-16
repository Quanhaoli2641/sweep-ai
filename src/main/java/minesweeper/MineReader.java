package minesweeper;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class MineReader {

    int[][] field;
    int[] data;

    int rows;
    int columns;

    public int numFresh;
    public int numEmpty;

    boolean dead = false;
    boolean won = false;

    BufferedImage fieldImage = null;
    Robot clicker;
    File screenshot;
    Minesweeper minesweeper;

    MineReader(Minesweeper minesweeper) {
        this.minesweeper = minesweeper;
    }

    public void init() {
        dead = false;
        won = false;
        numEmpty = 0;

        try {
            clicker = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        screenshot = new File("State.png");
        try {
            fieldImage = ImageIO.read(screenshot);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int width = fieldImage.getWidth();
        int height = fieldImage.getHeight();

        rows = height / 16;
        columns = width / 16;

        System.out.println("Detected field of " + columns + "x" + rows);

        field = new int[columns + 2][rows + 2];

        System.out.println("Creating Array of: " + (columns + 2) + "x" + (rows + 2));

        for (int n = 0; n < rows + 2; n++) {
            field[0][n] = -1;
            field[columns + 1][n] = -1;
        }

        for (int n = 0; n < columns + 2; n++) {
            field[n][0] = -1;
            field[n][rows + 1] = -1;
        }

    }

    public void updateField() {
        numFresh = 0;
        System.out.println("Updating Field:");
        try {
            minesweeper.updateFieldImage();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        try {
            fieldImage = ImageIO.read(screenshot);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int x = 1; x < columns + 1; x++)
            for (int y = 1; y < rows + 1; y++) {
                if (field[x][y] == 0 || field[x][y] == -3) {
                    if (field[x][y] == 0)
                        numFresh++;
                    field[x][y] = readTile(x, y);
                } else if (field[x][y] == -1)
                    numEmpty++;
            }
        if (numFresh == 0 && !dead) {
            won = true;
        }
    }

    public int readTile(int x, int y) {

        int tileType = 0;

        int pixelX = (x - 1) * 16;
        int pixelY = (rows - y) * 16;

        String color = "";
        int n = 1;

        do {
            color = getPixelColor(pixelX + 8, pixelY + n);
            switch (color) {
                case ("white"): {
                    tileType = 0;
                    n = 16;
                }
                break;
                case ("blue"): {
                    tileType = 1;
                    n = 16;
                }
                break;
                case ("green"): {
                    tileType = 2;
                    n = 16;
                }
                break;
                case ("red"): {
                    tileType = 3;
                    n = 16;
                }
                break;
                case ("purple"): {
                    tileType = 4;
                    n = 16;
                }
                break;
                case ("brown"): {
                    tileType = 5;
                    n = 16;
                }
                break;
                case ("teal"): {
                    tileType = 6;
                    n = 16;
                }
                break;
                case ("black"): {
                    tileType = 7;
                    n = 16;
                }
                break;
                case ("gray"): {
                    tileType = 8;
                    n = 16;
                }
                break;
                case ("bomb"): {
                    tileType = -2;
                    n = 16;
                }
                break;
                default: {
                    tileType = -1;
                }
                break;
            }
            n++;
        } while (n < 15);
        return tileType;
    }

    private String getPixelColor(int x, int y) {
        String color = "empty";

        final int clr = fieldImage.getRGB(x, y);
        final int red = (clr & 0x00ff0000) >> 16;
        final int green = (clr & 0x0000ff00) >> 8;
        final int blue = (clr & 0x000000ff);

        if (red == 255 && green == 255 && blue == 255) color = "white";
        else if (blue == 255 && red == 0 && green == 0) color = "blue";
        else if (green == 128 && red == 0 && blue == 0) color = "green";
        else if (red == 255 && green == 0 && blue == 0) {
            color = "red";
            int deadCheckColor = (fieldImage.getRGB(x - 4, y + 8) & 0x00ffffff);
            if (deadCheckColor == 0) {
                dead = true;
                color = "bomb";
            }
        }
        else if (blue == 128 && red == 0 && green == 0) color = "purple";
        else if (red == 128 && green == 0 && blue == 0) color = "brown";
        else if (red == 0 && green == 128 && blue == 128) color = "teal";
        else if (red == 0 && green == 0 && blue == 0) {
            color = "black";
            int deadCheckColor = (fieldImage.getRGB(x - 4, y + 8) & 0x00ffffff);
            if (deadCheckColor == 0) {
                dead = true;
                color = "bomb";
            }
        }
        else if (red == 128 && green == 128 && blue == 128) color = "gray";
        return color;
    }

    public void click(int x, int y, boolean flag) {

        int mask;

        if (!flag)
            mask = InputEvent.BUTTON1_DOWN_MASK;
        else
            mask = InputEvent.BUTTON3_DOWN_MASK;


        int pixelX = Math.abs((x - 1) * 16) + 8;
        int pixelY = Math.abs((rows - y) * 16) + 8;

        pixelX += minesweeper.topX + minesweeper.offsetX;
        pixelY += minesweeper.topY - minesweeper.offsetY;

        clicker.mouseMove(pixelX, pixelY);
        clicker.mousePress(mask);

        try {
            Thread.sleep(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        clicker.mouseRelease(mask);
    }
}