package io.github.yarikmogila.nickgen.gui;

import io.github.yarikmogila.nickgen.common.NicknameLocale;
import io.github.yarikmogila.nickgen.common.NicknameResult;
import io.github.yarikmogila.nickgen.common.NicknameTemplate;
import io.github.yarikmogila.nickgen.common.NicknameGeneratorDescriptor;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;

final class NicknamePanel extends JPanel {

    private final NicknameGenerationFacade facade;

    private final JComboBox<NicknameLocale> localeComboBox;
    private final JComboBox<NicknameTemplate> templateComboBox;
    private final JComboBox<NicknameGeneratorDescriptor> generatorComboBox;
    private final JSpinner countSpinner;
    private final JTextField seedTextField;
    private final DefaultListModel<String> nicknamesModel;
    private final JList<String> nicknamesList;

    NicknamePanel(NicknameGenerationFacade facade) {
        super(new BorderLayout(8, 8));
        this.facade = facade;

        localeComboBox = new JComboBox<>(NicknameLocale.values());
        templateComboBox = new JComboBox<>(NicknameTemplate.values());
        generatorComboBox = new JComboBox<>(facade.availableGenerators().toArray(NicknameGeneratorDescriptor[]::new));
        countSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        seedTextField = new JTextField(12);
        nicknamesModel = new DefaultListModel<>();
        nicknamesList = new JList<>(nicknamesModel);
        nicknamesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

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

        panel.add(new JLabel("Generator:"));
        panel.add(generatorComboBox);

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

        JButton exportButton = new JButton("Export TXT");
        exportButton.addActionListener(event -> onExportClicked());

        panel.add(generateButton);
        panel.add(copyButton);
        panel.add(exportButton);
        return panel;
    }

    private void onGenerateClicked() {
        try {
            int count = (Integer) countSpinner.getValue();
            NicknameLocale locale = (NicknameLocale) localeComboBox.getSelectedItem();
            NicknameTemplate template = (NicknameTemplate) templateComboBox.getSelectedItem();
            NicknameGeneratorDescriptor descriptor =
                    (NicknameGeneratorDescriptor) generatorComboBox.getSelectedItem();
            String generatorId = descriptor == null ? null : descriptor.id();

            List<NicknameResult> nicknames = facade.generate(
                    count,
                    locale,
                    template,
                    generatorId,
                    seedTextField.getText()
            );
            nicknamesModel.clear();
            nicknames.forEach(result -> nicknamesModel.addElement(result.value()));
        } catch (RuntimeException exception) {
            showError(exception.getMessage());
        }
    }

    private void onCopyClicked() {
        List<String> selected = nicknamesList.getSelectedValuesList();
        if (selected.isEmpty()) {
            showError("Select one or more nicknames to copy");
            return;
        }

        String text = joinLines(selected);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
    }

    private void onExportClicked() {
        if (nicknamesModel.isEmpty()) {
            showError("Generate nicknames before exporting");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export nicknames to TXT");
        fileChooser.setSelectedFile(new File("nicknames.txt"));

        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = ensureTxtExtension(fileChooser.getSelectedFile());
        String output = joinLines(modelValues());
        try {
            Files.writeString(selectedFile.toPath(), output, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            showError("Failed to export file: " + exception.getMessage());
        }
    }

    private List<String> modelValues() {
        return Collections.list(nicknamesModel.elements());
    }

    static String joinLines(List<String> values) {
        return String.join("\n", values);
    }

    private File ensureTxtExtension(File file) {
        String name = file.getName();
        if (name.toLowerCase().endsWith(".txt")) {
            return file;
        }
        File parent = file.getParentFile();
        if (parent == null) {
            return new File(name + ".txt");
        }
        return new File(parent, name + ".txt");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Generation error", JOptionPane.ERROR_MESSAGE);
    }
}
