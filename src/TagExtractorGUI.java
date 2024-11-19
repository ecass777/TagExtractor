// TagExtractorGUI.java
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.Map.Entry;

public class TagExtractorGUI extends JFrame implements ActionListener {

    private JButton selectTextFileButton;
    private JButton selectStopWordsFileButton;
    private JButton processButton;
    private JButton saveButton;
    private JLabel textFileLabel;
    private JLabel stopWordsFileLabel;
    private JTextArea outputTextArea;

    private File textFile;
    private File stopWordsFile;
    private Map<String, Integer> wordFrequencyMap;

    public TagExtractorGUI() {
        super("Tag/Keyword Extractor");

        // Initialize components
        selectTextFileButton = new JButton("Select Text File");
        selectStopWordsFileButton = new JButton("Select Stop Words File");
        processButton = new JButton("Process");
        saveButton = new JButton("Save Output");
        textFileLabel = new JLabel("No text file selected");
        stopWordsFileLabel = new JLabel("No stop words file selected");
        outputTextArea = new JTextArea(20, 50);
        outputTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputTextArea);

        // Set layout
        JPanel topPanel = new JPanel(new GridLayout(2, 2));
        topPanel.add(selectTextFileButton);
        topPanel.add(textFileLabel);
        topPanel.add(selectStopWordsFileButton);
        topPanel.add(stopWordsFileLabel);

        JPanel middlePanel = new JPanel();
        middlePanel.add(processButton);
        middlePanel.add(saveButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(middlePanel, BorderLayout.CENTER);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        // Add action listeners
        selectTextFileButton.addActionListener(this);
        selectStopWordsFileButton.addActionListener(this);
        processButton.addActionListener(this);
        saveButton.addActionListener(this);

        // Set up the frame
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(mainPanel);
        this.pack();
        this.setLocationRelativeTo(null); // Center the window
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == selectTextFileButton) {
            selectTextFile();
        } else if (e.getSource() == selectStopWordsFileButton) {
            selectStopWordsFile();
        } else if (e.getSource() == processButton) {
            processFiles();
        } else if (e.getSource() == saveButton) {
            saveOutput();
        }
    }

    private void selectTextFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Text File");
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            textFile = fileChooser.getSelectedFile();
            textFileLabel.setText("Text File: " + textFile.getName());
        }
    }

    private void selectStopWordsFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Stop Words File");
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            stopWordsFile = fileChooser.getSelectedFile();
            stopWordsFileLabel.setText("Stop Words File: " + stopWordsFile.getName());
        }
    }

    private void processFiles() {
        if (textFile == null || stopWordsFile == null) {
            JOptionPane.showMessageDialog(this, "Please select both text file and stop words file.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Set<String> stopWords = loadStopWords(stopWordsFile);
        wordFrequencyMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(textFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Normalize the line
                line = line.toLowerCase();
                line = line.replaceAll("[^a-z]", " "); // Replace non-letter characters with space

                String[] words = line.split("\\s+"); // Split on whitespace
                for (String word : words) {
                    if (word.isEmpty() || stopWords.contains(word)) {
                        continue;
                    }
                    // Update frequency map
                    wordFrequencyMap.put(word, wordFrequencyMap.getOrDefault(word, 0) + 1);
                }
            }

            // Display results
            displayResults();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading text file.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private Set<String> loadStopWords(File stopWordsFile) {
        Set<String> stopWordsSet = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(stopWordsFile))) {
            String word;
            while ((word = br.readLine()) != null) {
                word = word.trim().toLowerCase();
                if (!word.isEmpty()) {
                    stopWordsSet.add(word);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading stop words file.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return stopWordsSet;
    }

    private void displayResults() {
        outputTextArea.setText(""); // Clear previous results

        // Create a list from elements of the map
        List<Map.Entry<String, Integer>> list = new LinkedList<>(wordFrequencyMap.entrySet());

        // Sort the list in descending order of frequency
        Collections.sort(list, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        // Display the sorted list
        for (Map.Entry<String, Integer> entry : list) {
            outputTextArea.append(entry.getKey() + ": " + entry.getValue() + "\n");
        }
    }

    private void saveOutput() {
        if (wordFrequencyMap == null || wordFrequencyMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No data to save. Please process files first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Output File");
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File outputFile = fileChooser.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(outputFile)) {
                // Create a list from elements of the map
                List<Map.Entry<String, Integer>> list = new LinkedList<>(wordFrequencyMap.entrySet());

                // Sort the list in descending order of frequency
                Collections.sort(list, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));

                // Write the sorted list to the file
                for (Map.Entry<String, Integer> entry : list) {
                    pw.println(entry.getKey() + ": " + entry.getValue());
                }
                JOptionPane.showMessageDialog(this, "Output saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving output file.", "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}
