package io.github.yarikmogila.nickgen.gui;

import io.github.yarikmogila.nickgen.common.NicknameLocale;
import io.github.yarikmogila.nickgen.common.NicknameResult;
import io.github.yarikmogila.nickgen.common.NicknameTemplate;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

final class NicknamePanel extends JPanel {

    private final NicknameGenerationFacade facade;

    private final JComboBox<NicknameLocale> localeComboBox;
    private final JComboBox<NicknameTemplate> templateComboBox;
    private final JSpinner countSpinner;
    private final JTextField seedTextField;
    private final DefaultListModel<String> nicknamesModel;
    private final JList<String> nicknamesList;

    NicknamePanel(NicknameGenerationFacade facade) {
        super(new BorderLayout(8, 8));
        this.facade = facade;

        localeComboBox = new JComboBox<>(NicknameLocale.values());
        templateComboBox = new JComboBox<>(NicknameTemplate.values());
        countSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        seedTextField = new JTextField(12);
        nicknamesModel = new DefaultListModel<>();
        nicknamesList = new JList<>(nicknamesModel);

        add(buildControlsPanel(), BorderLayout.NORTH);
        add(new JScrollPane(nicknamesList), BorderLayout.CENTER);
        add(buildActionsPanel(), BorderLayout.SOUTH);

        setOpaque(true);
    }

    private JPanel buildControlsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        panel.add(new JLabel("Locale:"));
        panel.add(localeComboBox);

        panel.add(new JLabel("Template:"));
        panel.add(templateComboBox);

        panel.add(new JLabel("Count:"));
        panel.add(countSpinner);

        panel.add(new JLabel("Seed:"));
        panel.add(seedTextField);

        return panel;
    }

    private JPanel buildActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton generateButton = new JButton("Generate");
        generateButton.addActionListener(event -> onGenerateClicked());

        JButton copyButton = new JButton("Copy selected");
        copyButton.addActionListener(event -> onCopyClicked());

        panel.add(generateButton);
        panel.add(copyButton);
        return panel;
    }

    private void onGenerateClicked() {
        try {
            int count = (Integer) countSpinner.getValue();
            NicknameLocale locale = (NicknameLocale) localeComboBox.getSelectedItem();
            NicknameTemplate template = (NicknameTemplate) templateComboBox.getSelectedItem();

            List<NicknameResult> nicknames = facade.generate(count, locale, template, seedTextField.getText());
            nicknamesModel.clear();
            nicknames.forEach(result -> nicknamesModel.addElement(result.value()));
        } catch (RuntimeException exception) {
            showError(exception.getMessage());
        }
    }

    private void onCopyClicked() {
        String selected = nicknamesList.getSelectedValue();
        if (selected == null || selected.isBlank()) {
            showError("Select a nickname to copy");
            return;
        }

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(selected), null);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Generation error", JOptionPane.ERROR_MESSAGE);
    }
}
