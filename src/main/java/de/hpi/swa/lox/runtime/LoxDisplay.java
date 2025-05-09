package de.hpi.swa.lox.runtime;

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.graalvm.polyglot.Value;

public class LoxDisplay {
    public Value paint;
    public Value keypressed;

    public JFrame frame;
    public JPanel panel;

    public static LoxDisplay create() {
        // inspired by TruffleSqueak, UI has to happen on a special thread
        final LoxDisplay[] display = new LoxDisplay[1];
        try {
            EventQueue.invokeAndWait(() -> display[0] = new LoxDisplay());
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
        }
        return Objects.requireNonNull(display[0]);
    }

    private LoxDisplay() {

        paint = null;
        keypressed = null;

        frame = new JFrame("Lox Display");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);

        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (paint != null) {
                    try {
                        paint.execute((Graphics2D) g);
                    } catch (Exception e) {
                        System.out.println("Error calling paint function: " + e);
                    }
                } else {
                    System.out.println("no paint function");
                }
            }
        };
        frame.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (keypressed != null) {
                    try {
                        keypressed.execute(e.getKeyCode());
                    } catch (Exception ex) {
                        System.out.println("Error calling keypressed function: " + ex);
                    }
                } else {
                    System.out.println("no keypressed function");
                }
            }
        });

        frame.add(panel);
        frame.setVisible(true);
    }

}
