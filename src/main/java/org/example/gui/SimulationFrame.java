package org.example.gui;

import org.example.businesslogic.SelectionPolicy;
import org.example.businesslogic.Server;
import org.example.businesslogic.SimulationManager;
import org.example.model.Task;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SimulationFrame extends JFrame {

    private final JTextField clientsField;
    private final JTextField queuesField;
    private final JTextField simulationTimeField;
    private final JTextField minArrivalField;
    private final JTextField maxArrivalField;
    private final JTextField minServiceField;
    private final JTextField maxServiceField;

    private final JComboBox<String> strategyBox;
    private final JButton startButton;

    private final JLabel currentTimeLabel;
    private final JPanel simulationPane;
    private final JTextArea resultsArea;

    private List<List<Task>> currentQueues = new ArrayList<>();
    private List<Task> currentWaitingClients = new ArrayList<>();

    private Thread simulationThread;

    public SimulationFrame() {
        setTitle("Queues Management Simulation");
        setSize(1450, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(8, 8));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel inputPanel = new JPanel(new GridLayout(4, 4, 8, 8));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Simulation Setup"));

        clientsField = new JTextField();
        queuesField = new JTextField();
        simulationTimeField = new JTextField();
        minArrivalField = new JTextField();
        maxArrivalField = new JTextField();
        minServiceField = new JTextField();
        maxServiceField = new JTextField();

        strategyBox = new JComboBox<>(new String[]{"SHORTEST_QUEUE", "SHORTEST_TIME"});
        startButton = new JButton("Start");

        inputPanel.add(new JLabel("Clients:"));
        inputPanel.add(clientsField);

        inputPanel.add(new JLabel("Queues:"));
        inputPanel.add(queuesField);

        inputPanel.add(new JLabel("Simulation time:"));
        inputPanel.add(simulationTimeField);

        inputPanel.add(new JLabel("Strategy:"));
        inputPanel.add(strategyBox);

        inputPanel.add(new JLabel("Min arrival:"));
        inputPanel.add(minArrivalField);

        inputPanel.add(new JLabel("Max arrival:"));
        inputPanel.add(maxArrivalField);

        inputPanel.add(new JLabel("Min service:"));
        inputPanel.add(minServiceField);

        inputPanel.add(new JLabel("Max service:"));
        inputPanel.add(maxServiceField);

        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(startButton, BorderLayout.EAST);

        currentTimeLabel = new JLabel("Current time: 0");
        currentTimeLabel.setFont(new Font("Arial", Font.BOLD, 18));

        simulationPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawSimulation((Graphics2D) g);
            }
        };
        simulationPane.setBackground(Color.WHITE);
        simulationPane.setPreferredSize(new Dimension(1800, 650));

        JScrollPane simulationScrollPane = new JScrollPane(simulationPane);
        simulationScrollPane.setBorder(BorderFactory.createTitledBorder("Simulation"));
        simulationScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        simulationScrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        resultsArea = new JTextArea(2, 20);
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane resultsScroll = new JScrollPane(resultsArea);
        resultsScroll.setBorder(BorderFactory.createTitledBorder("Results"));
        resultsScroll.setPreferredSize(new Dimension(100, 85));

        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.add(currentTimeLabel, BorderLayout.NORTH);
        centerPanel.add(simulationScrollPane, BorderLayout.CENTER);
        centerPanel.add(resultsScroll, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);

        startButton.addActionListener(e -> startSimulation());

        setVisible(true);
    }

    private void startSimulation() {
        if (simulationThread != null && simulationThread.isAlive()) {
            JOptionPane.showMessageDialog(
                    this,
                    "A simulation is already running.",
                    "Simulation Running",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (clientsField.getText().trim().isEmpty()
                || queuesField.getText().trim().isEmpty()
                || simulationTimeField.getText().trim().isEmpty()
                || minArrivalField.getText().trim().isEmpty()
                || maxArrivalField.getText().trim().isEmpty()
                || minServiceField.getText().trim().isEmpty()
                || maxServiceField.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "All fields must be completed.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            int numberOfClients = Integer.parseInt(clientsField.getText().trim());
            int numberOfQueues = Integer.parseInt(queuesField.getText().trim());
            int simulationTime = Integer.parseInt(simulationTimeField.getText().trim());
            int minArrival = Integer.parseInt(minArrivalField.getText().trim());
            int maxArrival = Integer.parseInt(maxArrivalField.getText().trim());
            int minService = Integer.parseInt(minServiceField.getText().trim());
            int maxService = Integer.parseInt(maxServiceField.getText().trim());

            SelectionPolicy policy;
            if ("SHORTEST_QUEUE".equals(strategyBox.getSelectedItem())) {
                policy = SelectionPolicy.SHORTEST_QUEUE;
            } else {
                policy = SelectionPolicy.SHORTEST_TIME;
            }

            resultsArea.setText("");
            setCurrentTime(0);
            currentQueues = new ArrayList<>();
            currentWaitingClients = new ArrayList<>();
            simulationPane.repaint();

            SimulationManager simulationManager = new SimulationManager(
                    simulationTime,
                    minService,
                    maxService,
                    numberOfQueues,
                    numberOfClients,
                    minArrival,
                    maxArrival,
                    policy,
                    this
            );

            startButton.setEnabled(false);

            simulationThread = new Thread(() -> {
                try {
                    simulationManager.run();
                } finally {
                    simulationThread = null;
                    SwingUtilities.invokeLater(() -> startButton.setEnabled(true));
                }
            });

            simulationThread.start();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "All fields must contain valid numeric values.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void drawSimulation(Graphics2D g2) {
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int startX = 165;
        int startY = 22;
        int rowHeight = 95;
        int rectWidth = 105;
        int rectHeight = 68;
        int gap = 12;

        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("Waiting list", 20, startY + 28);

        if (currentWaitingClients.isEmpty()) {
            g2.setColor(Color.GRAY);
            g2.drawString("empty", startX, startY + 28);
        } else {
            for (int i = 0; i < currentWaitingClients.size(); i++) {
                Task task = currentWaitingClients.get(i);
                int x = startX + i * (rectWidth + gap);
                drawTaskRect(g2, task, x, startY, rectWidth, rectHeight, new Color(255, 228, 181), -1);
            }
        }

        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(15, startY + 82, Math.max(simulationPane.getWidth() - 20, 1200), startY + 82);

        for (int i = 0; i < currentQueues.size(); i++) {
            int y = startY + 88 + i * rowHeight;

            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString("Queue " + (i + 1), 20, y + 28);

            List<Task> tasks = currentQueues.get(i);

            if (tasks.isEmpty()) {
                g2.setColor(Color.GRAY);
                g2.drawString("closed", startX, y + 28);
                continue;
            }

            int waitingTime = 0;

            for (int j = 0; j < tasks.size(); j++) {
                Task task = tasks.get(j);
                int x = startX + j * (rectWidth + gap);

                Color color = (j == 0)
                        ? new Color(144, 238, 144)
                        : new Color(173, 216, 230);

                drawTaskRect(g2, task, x, y, rectWidth, rectHeight, color, waitingTime);

                if (j == 0) {
                    g2.setColor(new Color(180, 0, 0));
                    g2.setFont(new Font("Arial", Font.BOLD, 11));
                    g2.drawString("serving", x + 24, y - 4);
                }

                waitingTime += task.getServiceTime();
            }
        }
    }

    private void drawTaskRect(Graphics2D g2, Task task, int x, int y, int w, int h, Color fillColor, int waitingTime) {
        g2.setColor(fillColor);
        g2.fillRoundRect(x, y, w, h, 16, 16);

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1));
        g2.drawRoundRect(x, y, w, h, 16, 16);

        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.drawString("C" + task.getId(), x + 38, y + 14);

        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        g2.drawString("A:" + task.getArrivalTime(), x + 8, y + 28);
        g2.drawString("S:" + task.getServiceTime(), x + 8, y + 42);

        String waitText = waitingTime >= 0 ? "W:" + waitingTime : "W:-";
        g2.drawString(waitText, x + 8, y + 56);
    }

    public void setCurrentTime(int time) {
        currentTimeLabel.setText("Current time: " + time);
    }

    public void updateSimulationPane(List<List<Task>> queuesSnapshot, List<Task> waitingClients) {
        currentQueues = new ArrayList<>();
        currentWaitingClients = new ArrayList<>();

        for (Task task : waitingClients) {
            currentWaitingClients.add(new Task(task));
        }

        for (List<Task> queue : queuesSnapshot) {
            List<Task> queueCopy = new ArrayList<>();
            for (Task task : queue) {
                queueCopy.add(new Task(task));
            }
            currentQueues.add(queueCopy);
        }

        int maxElements = currentWaitingClients.size();
        for (List<Task> queue : currentQueues) {
            maxElements = Math.max(maxElements, queue.size());
        }

        int width = Math.max(1700, 250 + maxElements * 125);
        int height = Math.max(650, 150 + currentQueues.size() * 95);

        simulationPane.setPreferredSize(new Dimension(width, height));
        simulationPane.revalidate();
        simulationPane.repaint();
    }

    public void setResultsText(String text) {
        resultsArea.setText(text);
    }
}