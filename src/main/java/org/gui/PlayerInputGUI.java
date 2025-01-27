package org.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import lombok.extern.slf4j.Slf4j;
import org.dto.BuildQuarterDto;
import org.dto.PlayerDto;
import org.dto.Position;
import org.dto.QuarterDto;
import org.service.QuarterBuilder;

@Slf4j
public class PlayerInputGUI {
    private final QuarterBuilder quarterBuilder = new QuarterBuilder();

    public static void main(String[] args) {
        new PlayerInputGUI().createAndShowGUI();
    }

    public void createAndShowGUI() {
        JFrame frame = new JFrame("근본FC 간편 스쿼드메이커");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 800);
        frame.setResizable(false);

        // 전체 패널 색상 설정 (핑크 테마)
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.PINK);

        // 상단 제목 추가
        JLabel titleLabel = new JLabel("근본FC 간편 스쿼드메이커", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24)); // 한글 지원 글꼴
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(Color.DARK_GRAY);
        titleLabel.setPreferredSize(new Dimension(600, 50));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridLayout(20, 1, 5, 5));
        inputPanel.setBackground(Color.PINK); // 패널 색상 설정
        JScrollPane scrollPane = new JScrollPane(inputPanel);

        List<PlayerInputRow> playerInputRows = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            PlayerInputRow inputRow = new PlayerInputRow(i + 1);
            playerInputRows.add(inputRow);
            inputPanel.add(inputRow.getPanel());
        }

        JButton uploadButton = new JButton("txt 파일 업로드");
        JButton saveButton = new JButton("스쿼드 짜기");
        JButton helpButton = new JButton("도움말");

        // 도움말 버튼 이벤트 추가
        helpButton.addActionListener(e -> showHelpDialog());

        uploadButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                parseAndSetData(selectedFile, playerInputRows);
            }
        });

        saveButton.addActionListener(e -> {
            List<PlayerDto> players = new ArrayList<>();
            for (PlayerInputRow inputRow : playerInputRows) {
                String name = inputRow.getPlayerName();
                List<Position> positions = inputRow.getPreferredPositions();
                Integer nonappearance = inputRow.getNonappearance();

                if (!name.isEmpty() && !positions.isEmpty()) {
                    players.add(new PlayerDto(name, positions, nonappearance));
                }
            }

            BuildQuarterDto buildQuarterDto = quarterBuilder.buildQuarter(players);
            Map<Integer, QuarterDto> quarters = buildQuarterDto.getQuarters();
            showResults(quarters, buildQuarterDto.getDescription());
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.PINK); // 버튼 패널 색상 설정
        buttonPanel.add(uploadButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(helpButton);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private void showResults(Map<Integer, QuarterDto> quarters, String description) {
        StringBuilder resultBuilder = new StringBuilder();

        for (int i = 1; i <= 4; i++) {
            QuarterDto quarterDto = quarters.get(i);
            EnumMap<Position, PlayerDto> assignedPosition = quarterDto.getAssignedPosition();

            resultBuilder.append("쿼터 ").append(i).append("\n");
            resultBuilder.append(String.format("%-6s %-6s %-6s\n",
                    getName(assignedPosition, Position.LW),
                    getName(assignedPosition, Position.ST),
                    getName(assignedPosition, Position.RW)));
            resultBuilder.append(String.format("    %-6s %-6s\n",
                    getName(assignedPosition, Position.LCM),
                    getName(assignedPosition, Position.RCM)));
            resultBuilder.append(String.format("     %-6s\n",
                    getName(assignedPosition, Position.CDM)));
            resultBuilder.append(String.format("%-6s %-6s %-6s %-6s\n\n",
                    getName(assignedPosition, Position.LB),
                    getName(assignedPosition, Position.LCB),
                    getName(assignedPosition, Position.RCB),
                    getName(assignedPosition, Position.RB)));
        }

        resultBuilder.append("\n\n");
        resultBuilder.append(description);

        JOptionPane.showMessageDialog(null, resultBuilder.toString(), "쿼터별 결과", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showHelpDialog() {
        String helpMessage = " 1. TXT 파일로 선수들을 입력할 수 있습니다. 입력 예시는 아래와 같습니다.\n 장지담 2\n LW LB RW\n 최진원 2\n CDM LCM RCM\n 김태수 4\n ST\n\n여기서 이름 옆의 숫자는 불참 횟수입니다. 이름 아래는 포지션을 공백으로 구분해 넣어줍니다.\n\n 2. 포지션 종류:\n LW ST RW LCM RCM CDM LB LCB RCB RB ";

        JOptionPane.showMessageDialog(null, helpMessage, "도움말", JOptionPane.INFORMATION_MESSAGE);
    }

    private String getName(EnumMap<Position, PlayerDto> assignedPosition, Position position) {
        PlayerDto player = assignedPosition.get(position);
        return (player != null) ? player.getName() : "N/A";
    }

    private void parseAndSetData(File file, List<PlayerInputRow> playerInputRows) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int index = 0;

            while ((line = reader.readLine()) != null && index < playerInputRows.size()) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] nameAndCount = line.trim().split(" ");
                String name = nameAndCount[0];
                Integer nonappearance = Integer.parseInt(nameAndCount[1]);

                if ((line = reader.readLine()) != null) {
                    String[] positions = line.trim().split(" ");
                    PlayerInputRow inputRow = playerInputRows.get(index++);
                    inputRow.setPlayerData(name, positions, nonappearance);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error reading file: " + e.getMessage());
        }
    }
}

class PlayerInputRow {
    private final JPanel panel;
    private final JTextField playerNameField;
    private final JTextField nonappearanceField;
    private final List<JCheckBox> positionCheckBoxes;

    public PlayerInputRow(int playerNumber) {
        panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBackground(Color.PINK); // 패널 배경색 설정

        JLabel nameLabel = new JLabel("선수" + playerNumber + " :");
        nameLabel.setPreferredSize(new Dimension(50, 20));
        playerNameField = new JTextField(5);
        playerNameField.setPreferredSize(new Dimension(60, 20));

        JLabel nonappearanceLabel = new JLabel("불참횟수:");
        nonappearanceLabel.setPreferredSize(new Dimension(60, 20));
        nonappearanceField = new JTextField(2);
        nonappearanceField.setPreferredSize(new Dimension(40, 20));

        panel.add(nameLabel);
        panel.add(playerNameField);
        panel.add(nonappearanceLabel);
        panel.add(nonappearanceField);

        JPanel checkBoxPanel = new JPanel(new GridLayout(2, 5, 5, 5));
        checkBoxPanel.setBackground(Color.PINK);
        positionCheckBoxes = new ArrayList<>();
        for (Position position : Position.values()) {
            JCheckBox checkBox = new JCheckBox(position.name());
            checkBox.setFont(checkBox.getFont().deriveFont(10f));
            positionCheckBoxes.add(checkBox);
            checkBoxPanel.add(checkBox);
        }
        panel.add(checkBoxPanel);
    }

    public JPanel getPanel() {
        return panel;
    }

    public String getPlayerName() {
        return playerNameField.getText();
    }

    public Integer getNonappearance() {
        try {
            return Integer.parseInt(nonappearanceField.getText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public List<Position> getPreferredPositions() {
        List<Position> positions = new ArrayList<>();
        for (JCheckBox checkBox : positionCheckBoxes) {
            if (checkBox.isSelected()) {
                positions.add(Position.valueOf(checkBox.getText()));
            }
        }
        return positions;
    }

    public void setPlayerData(String name, String[] positions, Integer nonappearance) {
        playerNameField.setText(name);
        nonappearanceField.setText(nonappearance.toString());

        EnumSet<Position> positionSet = EnumSet.noneOf(Position.class);
        for (String pos : positions) {
            try {
                positionSet.add(Position.valueOf(pos));
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid position: " + pos);
            }
        }
        for (JCheckBox checkBox : positionCheckBoxes) {
            checkBox.setSelected(positionSet.contains(Position.valueOf(checkBox.getText())));
        }
    }
}
