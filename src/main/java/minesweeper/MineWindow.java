package minesweeper;

import javax.swing.JFrame;

import java.awt.Color;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MineWindow implements ActionListener {

    JFrame frame;
    Minesweeper minesweeper;

    public MineWindow() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setResizable(false);
        frame.setBounds(200, 200, 300, 150);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        frame.getContentPane().setBackground(Color.darkGray);
        frame.setFocusable(true);

        JButton btnStart = new JButton("Start");
        btnStart.setForeground(new Color(0, 0, 0));
        btnStart.setFont(new Font("Verdana", Font.PLAIN, 30));
        btnStart.setToolTipText("Start!");
        btnStart.setBounds(30, 20, 240, 80);
        btnStart.setActionCommand("start");
        btnStart.addActionListener(this);
        frame.getContentPane().add(btnStart);

    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("start")) {
            minesweeper = new Minesweeper();
            minesweeper.start();
        }
    }
}

