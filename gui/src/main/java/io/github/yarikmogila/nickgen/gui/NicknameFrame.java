package io.github.yarikmogila.nickgen.gui;

import java.awt.BorderLayout;
import javax.swing.JFrame;

final class NicknameFrame extends JFrame {

    NicknameFrame(NicknameGenerationFacade facade) {
        super("Nickname Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(new NicknamePanel(facade), BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }
}
