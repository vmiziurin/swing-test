package com.gransoft.testing.miziurin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Random;

public class Main {

    private static final String INTRO_ERROR_LABEL_TEXT =
            "The entered value should be greater than 0 and less or equal to 30!";
    private static final String INTRO_FRAME_LABEL = "How many numbers to display?";
    private static final String ALERT_MESSAGE = "Please select a value smaller or equal to 30.";
    private static final String ENTER_BUTTON = "Enter";
    private static final String SORT_BUTTON = "Sort";
    private static final String RESET_BUTTON = "Reset";
    private static final String USER_INPUT_REGEX = "^\\s*([1-9]|[1-2][0-9]|[3][0])\\s*$";
    private static final int MANDATORY_NUMBER_LE_VALUE = 30;
    private static final int FRAME_WIDTH = 500;
    private static final int FRAME_HEIGHT = 400;
    private static final int FRAME_MIN_WIDTH = 300;
    private static final int FRAME_MIN_HEIGHT = 400;
    private static final int MAX_RANDOM_NUMBER = 1000;
    private static final int MAX_ROWS_QUANTITY = 10;
    private static final int TEXTFIELD_WIDTH = 10;
    private static final int DELAY_AFTER_SWAP_MS = 100;

    private JFrame introFrame;
    private JFrame sortFrame;
    private JTextField introTextField;
    private JLabel introErrorLabel;
    private JPanel numbersPanel;
    private JPanel managePanel;
    private JPanel introPanel;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private int[] randomNumbers;
    private JButton[] numberButtons;
    private JButton sortButton;
    private JButton resetButton;
    private boolean desc = true;

    private Main() {
        initComponents();
        introFrame.setLayout(new GridBagLayout());
        sortFrame.setLayout(new BorderLayout());
        prepareFrame(introFrame, true);
        prepareFrame(sortFrame, false);
        drawIntroFrame();
        makeSortFrameManagePanel();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }

    private void initComponents() {
        introFrame = new JFrame();
        sortFrame = new JFrame();
        introPanel = new JPanel(new GridLayout(4, 1));
        numbersPanel = new JPanel(new GridBagLayout());
        managePanel = new JPanel(new GridLayout(2, 1, 8, 8));
        leftPanel = new JPanel();
        rightPanel = new JPanel();
        sortButton = new JButton(SORT_BUTTON);
        resetButton = new JButton(RESET_BUTTON);
    }

    /**
     * Sets the standard frame properties for JFrame objects on startup.
     *
     * @param frame   the frame to set
     * @param visible true if frame should be visible on startup
     */
    private void prepareFrame(JFrame frame, boolean visible) {
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setMinimumSize(new Dimension(FRAME_MIN_WIDTH, FRAME_MIN_HEIGHT));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(visible);
    }

    /**
     * Draws intro frame. Intro frame contains
     * label, textField, enter button and
     * error label (appears when input string has not integer value).
     */
    private void drawIntroFrame() {
        makeIntroFrameLabel();
        makeIntroFrameTextField();
        makeIntroFrameEnterButton();
        makeIntroFrameErrorLabel();
        introFrame.add(introPanel);
    }

    private void makeIntroFrameLabel() {
        JLabel introLabel = new JLabel(INTRO_FRAME_LABEL);
        introPanel.add(introLabel);
    }

    private void makeIntroFrameTextField() {
        introTextField = new JTextField(TEXTFIELD_WIDTH);
        introTextField.addActionListener(e -> doEnter());
        introPanel.add(introTextField);
    }

    private void makeIntroFrameEnterButton() {
        JButton enterButton = new JButton(ENTER_BUTTON);
        enterButton.addActionListener(e -> doEnter());
        introPanel.add(enterButton);
    }

    private void makeIntroFrameErrorLabel() {
        introErrorLabel = new JLabel();
        introPanel.add(introErrorLabel);
    }

    /**
     * Makes right manage panel.
     * This panel contains sort and reset buttons.
     */
    private void makeSortFrameManagePanel() {
        sortButton.addActionListener(e -> sortNumbers());
        resetButton.addActionListener(e -> reset());
        managePanel.add(sortButton);
        managePanel.add(resetButton);
        rightPanel.add(managePanel);
        sortFrame.add(rightPanel, BorderLayout.EAST);
    }

    /**
     * Draws next screen with random numbers if user input is correct.
     * In other case the error message appears.
     */
    private void doEnter() {
        int numbersCount = getUserIntInput(introTextField.getText());
        if (numbersCount <= 0) {
            introErrorLabel.setText(INTRO_ERROR_LABEL_TEXT);
        } else {
            randomNumbers = generateRandomNumbers(numbersCount);
            makeSortFrameNumbersPanel();
            sortFrame.setVisible(true);
            introFrame.setVisible(false);
        }
    }


    /**
     * Sorts presented numbers in a descending order.
     * Recall will reverse the sort direction.
     */
    private void sortNumbers() {
        Thread thread = new Thread(() -> {
            switchButtonsState(false);
            quickSort(0, randomNumbers.length - 1);
            switchButtonsState(true);
            desc = !desc;
        });
        thread.start();
    }

    /**
     * Takes back to clear intro screen.
     */
    private void reset() {
        introTextField.setText(null);
        introFrame.setVisible(true);
        sortFrame.setVisible(false);
        desc = true;
    }

    /**
     * Enables (or disables) all the numberButtons on the sorting screen.
     *
     * @param enabled true to enable all the buttons, otherwise false
     */
    private void switchButtonsState(boolean enabled) {
        Arrays.stream(numberButtons).forEach(b -> b.setEnabled(enabled));
        sortButton.setEnabled(enabled);
        resetButton.setEnabled(enabled);
        numbersPanel.repaint();
    }

    /**
     * Makes left panel with buttons which indicate generated random numbers.
     * The values of numbers be updated after each iteration of quick-sort.
     */
    private void makeSortFrameNumbersPanel() {
        numbersPanel.removeAll();
        numberButtons = new JButton[randomNumbers.length];
        int row = 0;
        int col = 1;
        for (int i = 0; i < randomNumbers.length; i++) {
            numberButtons[i] = new JButton(Integer.toString(randomNumbers[i]));
            int newNumbersCount = randomNumbers[i];
            numberButtons[i].addActionListener(e -> updateSortFrameNumbersPanel(newNumbersCount));
            col = row >= MAX_ROWS_QUANTITY ? ++col : col;
            row = row >= MAX_ROWS_QUANTITY ? 1 : ++row;
            numbersPanel.add(numberButtons[i], getGridBagConstraints(row, col));
        }
        leftPanel.add(numbersPanel);
        sortFrame.add(leftPanel, BorderLayout.WEST);
        numbersPanel.revalidate();
        numbersPanel.repaint();
    }

    /**
     * Updates number buttons with generation of new values.
     * if newNumbersCount greater than @Link {@link Main#MANDATORY_NUMBER_LE_VALUE},
     * pop up a message should be appeared.
     *
     * @param newNumbersCount quantity of the numbers after updating.
     */
    private void updateSortFrameNumbersPanel(int newNumbersCount) {
        if (newNumbersCount > MANDATORY_NUMBER_LE_VALUE) {
            JOptionPane.showMessageDialog(sortFrame, ALERT_MESSAGE);
        } else {
            randomNumbers = generateRandomNumbers(newNumbersCount);
            makeSortFrameNumbersPanel();
        }
        desc = true;
    }

    private int[] generateRandomNumbers(int numbersCount) {
        int[] rns = new Random().ints(numbersCount, 1, MAX_RANDOM_NUMBER + 1).toArray();
        while (Arrays.stream(rns).noneMatch(n -> n <= MANDATORY_NUMBER_LE_VALUE)) {
            rns = new Random().ints(numbersCount, 1, MAX_RANDOM_NUMBER + 1).toArray();
        }
        return rns;
    }

    /**
     * Uses to check user integer input
     *
     * @param input input string
     * @return integer value of input string
     */
    private int getUserIntInput(String input) {
        return !input.matches(USER_INPUT_REGEX) ? -1 : Integer.parseInt(input.trim());
    }

    private GridBagConstraints getGridBagConstraints(int i, int j) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = j;
        gridBagConstraints.gridy = i;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(4, 4, 4, 4);
        return gridBagConstraints;
    }

    private void quickSort(int begin, int end) {
        if (begin < end) {
            int partitionIndex = partition(randomNumbers, begin, end);
            quickSort(begin, partitionIndex - 1);
            quickSort(partitionIndex + 1, end);
        }
    }

    private int partition(int[] arr, int begin, int end) {
        int pivot = arr[end];
        int i = (begin - 1);
        for (int j = begin; j < end; j++) {
            if (desc) {
                if (arr[j] > pivot) {
                    i++;
                    swap(i, j);
                }
            } else {
                if (arr[j] <= pivot) {
                    i++;
                    swap(i, j);
                }
            }
        }
        swap(i + 1, end);
        return i + 1;
    }

    private void swap(int a, int b) {
        if (a != b) {
            swapArrayNumbers(a, b);
            swapNumberButtonsText(a, b);
            swapButtonsListeners(a, b);
            numbersPanel.repaint();
            try {
                Thread.sleep(DELAY_AFTER_SWAP_MS);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

    private void swapButtonsListeners(int a, int b) {
        int temp = randomNumbers[a];
        randomNumbers[a] = randomNumbers[b];
        randomNumbers[b] = temp;
    }

    private void swapNumberButtonsText(int a, int b) {
        String numberTemp = numberButtons[a].getText();
        numberButtons[a].setText(numberButtons[b].getText());
        numberButtons[b].setText(numberTemp);
    }

    private void swapArrayNumbers(int a, int b) {
        ActionListener listenerTemp = numberButtons[a].getActionListeners()[0];
        numberButtons[a].removeActionListener(listenerTemp);
        numberButtons[a].addActionListener(numberButtons[b].getActionListeners()[0]);
        numberButtons[b].removeActionListener(numberButtons[b].getActionListeners()[0]);
        numberButtons[b].addActionListener(listenerTemp);
    }
}
