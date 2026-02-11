package io.github.yarikmogila.nickgen.gui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class NicknameGuiApp {

    private NicknameGuiApp() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            setLookAndFeel();
            NicknameFrame frame = new NicknameFrame(new NicknameGenerationFacade());
            frame.setVisible(true);
        });
    }

    private static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Falling back to default look and feel is acceptable.
        }
    }
}
