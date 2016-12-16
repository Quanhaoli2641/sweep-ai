package minesweeper;

import java.util.Scanner;

public class MineStarter {

    public static int input;

    public static void main(String[] args) {

        System.out.println("Type 1 for Hill Climber AI.");
        System.out.println("Type 2 for Pattern Recognition AI.");

        Scanner object = new Scanner(System.in);
        input = object.nextInt();

        while(input != 1 && input != 2){
            System.out.println("Not valid. Try Again.");
            input = object.nextInt();
        }

        MineWindow window = new MineWindow();

        window.frame.setVisible(true);

        System.out.println("Starting...");
    }

}
