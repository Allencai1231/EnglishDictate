import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

/**
 * 为单词学习应用提供图形用户界面。
 * 该GUI与原始的控制台逻辑(App.java)分离，仅调用其功能来驱动后端。
 * GUI.java
 */
public class Gui extends JFrame {

    private WordsCollection wordsCollection;
    private Words currentWords;
    private int currentWordIndex = 0;
    private ArrayList<Integer> randomList;

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel welcomePanel;
    private JPanel selectionPanel;
    private JPanel learningPanel;
    private JComboBox<String> fileComboBox;
    private JLabel definitionLabel;
    private JTextField wordInputField;
    private JButton submitButton;
    private JLabel feedbackLabel;
    private JLabel progressLabel;

    public Gui() {
        // --- 窗口基础设置 ---
        setTitle("单词记忆大师");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 350);
        setLocationRelativeTo(null); // 窗口居中
        try {
            // 尝试设置一个更现代的外观和感觉
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // --- 1. 欢迎面板 ---
        createWelcomePanel();

        // --- 2. 文件选择面板 ---
        createSelectionPanel();

        // --- 3. 学习面板 ---
        createLearningPanel();


        mainPanel.add(welcomePanel, "Welcome");
        mainPanel.add(selectionPanel, "Selection");
        mainPanel.add(learningPanel, "Learning");

        add(mainPanel);
    }

    private void createWelcomePanel() {
        welcomePanel = new JPanel(new BorderLayout(20, 20));
        welcomePanel.setBorder(new EmptyBorder(50, 50, 50, 50));

        JLabel titleLabel = new JLabel("欢迎使用单词记忆大师", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        welcomePanel.add(titleLabel, BorderLayout.CENTER);

        JButton startButton = new JButton("开始学习");
        startButton.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        startButton.addActionListener(e -> cardLayout.show(mainPanel, "Selection"));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(startButton);
        welcomePanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void createSelectionPanel() {
        selectionPanel = new JPanel(new GridBagLayout());
        selectionPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;


        JLabel selectLabel = new JLabel("请选择一个单词本开始学习:");
        selectLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        selectionPanel.add(selectLabel, gbc);
        
        gbc.gridy++;
        // 从逻辑代码中获取所有.txt文件名
        String[] txtFiles = Words.getAllTxtFileNames();
        // 移除.txt后缀以方便显示
        for (int i = 0; i < txtFiles.length; i++) {
            txtFiles[i] = txtFiles[i].substring(0, txtFiles[i].lastIndexOf('.'));
        }
        fileComboBox = new JComboBox<>(txtFiles);
        fileComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        selectionPanel.add(fileComboBox, gbc);

        gbc.gridy++;
        JButton confirmButton = new JButton("确定");
        confirmButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        confirmButton.addActionListener(new ActionListener() {
            @Override 
            public void actionPerformed(ActionEvent e) {
                String selectedFile = (String) fileComboBox.getSelectedItem();
                if (selectedFile != null) {
                    // 使用现有逻辑加载单词
                    wordsCollection = new WordsCollection();
                    ArrayList<String[]> wordsData = Words.readToFile(selectedFile+".txt");
                    randomList = Words.randomListGenerate(wordsData.size());
                    currentWords = new Words(wordsData, randomList);
                    
                    startLearningSession();
                }
            }
        });
        selectionPanel.add(confirmButton, gbc);
    }


    private void createLearningPanel() {
        learningPanel = new JPanel(new BorderLayout(20, 20));
        learningPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // --- 北部：进度和释义 ---
        JPanel topPanel = new JPanel(new BorderLayout(10,10));
        progressLabel = new JLabel("进度: 0/0", SwingConstants.LEFT);
        progressLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        definitionLabel = new JLabel("释义会显示在这里", SwingConstants.CENTER);
        definitionLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        topPanel.add(progressLabel, BorderLayout.NORTH);
        topPanel.add(definitionLabel, BorderLayout.CENTER);
        learningPanel.add(topPanel, BorderLayout.NORTH);


        // --- 中部：输入框和提交按钮 ---
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        wordInputField = new JTextField(20);
        wordInputField.setFont(new Font("Arial", Font.PLAIN, 16));
        submitButton = new JButton("确定");
        submitButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        centerPanel.add(wordInputField);
        centerPanel.add(submitButton);
        learningPanel.add(centerPanel, BorderLayout.CENTER);

        // --- 南部：反馈信息和退出按钮 ---
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        feedbackLabel = new JLabel(" ", SwingConstants.CENTER); // 初始为空白
        feedbackLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        
        JButton exitButton = new JButton("退出");
        exitButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        exitButton.addActionListener(e -> System.exit(0));

        bottomPanel.add(feedbackLabel, BorderLayout.CENTER);
        bottomPanel.add(exitButton, BorderLayout.EAST);
        learningPanel.add(bottomPanel, BorderLayout.SOUTH);

        // --- 事件监听 ---
        // 提交按钮的动作
        ActionListener submitAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkWord();
            }
        };
        submitButton.addActionListener(submitAction);
        // 在输入框中按回车键等同于点击提交按钮
        wordInputField.addActionListener(submitAction);
    }
    
    private void startLearningSession() {
        currentWordIndex = 0;
        cardLayout.show(mainPanel, "Learning");
        displayNextWord();
        feedbackLabel.setText(" "); // 清空上一轮的反馈
    }

    private void displayNextWord() {
        if (currentWordIndex < randomList.size()) {
            int wordListIndex = randomList.get(currentWordIndex);
            String[] wordPair = currentWords.words.get(wordListIndex);
            definitionLabel.setText("<html><div style='text-align: center;'>" + wordPair[1] + "</div></html>");
            progressLabel.setText("进度: " + (currentWordIndex + 1) + "/" + randomList.size());
            wordInputField.setText("");
            wordInputField.requestFocus(); // 光标自动聚焦
            feedbackLabel.setText(" ");
        } else {
            // 当前单词本学习完成
            showCompletionDialog();
        }
    }

    private void checkWord() {
        String userInput = wordInputField.getText().trim();
        if(userInput.isEmpty()) return;

        int wordListIndex = randomList.get(currentWordIndex);
        String[] wordPair = currentWords.words.get(wordListIndex);
        String correctAnswer = wordPair[0];

        if (userInput.equalsIgnoreCase(correctAnswer)) {
            feedbackLabel.setForeground(new Color(0, 128, 0)); // 绿色
            feedbackLabel.setText("恭喜你，回答正确！");
            currentWordIndex++;
            // 延迟一小段时间后显示下一个单词
            Timer timer = new Timer(1000, e -> displayNextWord());
            timer.setRepeats(false);
            timer.start();
        } else {
            feedbackLabel.setForeground(Color.RED);
            feedbackLabel.setText("很遗憾，回答错误，正确答案是: " + correctAnswer);
            // 答错不进入下一个，而是让用户重新输入
            // 为了避免用户困惑，可以清空输入框
            wordInputField.selectAll();
            wordInputField.requestFocus();
        }
    }
    
    private void showCompletionDialog() {
        Object[] options = {"学习下一个", "退出"};
        int choice = JOptionPane.showOptionDialog(this,
                "恭喜！您已完成本单词本的学习。",
                "学习完成",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == JOptionPane.YES_OPTION) {
             cardLayout.show(mainPanel, "Selection"); // 返回文件选择界面
        } else {
            System.exit(0);
        }
    }


    public static void main(String[] args) {
        // 确保GUI在事件调度线程上创建和显示
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Gui().setVisible(true);
            }
        });
    }
}
