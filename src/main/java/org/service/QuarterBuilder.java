package org.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import org.dto.BuildQuarterDto;
import org.dto.PlayerDto;
import org.dto.Position;
import org.dto.QuarterDto;

public class QuarterBuilder {

    private static final int PREFER_SCORE = 2;
    private static final int NOT_PREFER_SCORE = 1;

    public BuildQuarterDto buildQuarter(List<PlayerDto> playerList) {

        String validationError = validatePlayers(playerList);
        if (validationError != null) {
            JOptionPane.showMessageDialog(null, validationError, "데이터 오류", JOptionPane.ERROR_MESSAGE);
            return null; // 빈 결과 반환
        }

        playerList.sort(Comparator.comparingInt(PlayerDto::getNonappearance));
        setTwoQuarterPlayers(playerList);
        Map<Integer, QuarterDto> quarters = new HashMap<>();

        for (int i = 1; i <= 4; i++) {
            QuarterDto quarterDto = new QuarterDto();
            quarterDto.setQuarterNumber(i);
            assignPosition(playerList, quarterDto);
            quarters.put(i, quarterDto);
            playerList.sort(Comparator.comparingInt(PlayerDto::getScore));
        }

        return BuildQuarterDto.of(quarters, getDescription(playerList));
    }

    private static String getDescription(List<PlayerDto> playerList) {
        StringBuilder sb = new StringBuilder();
        sb.append("# 불참횟수별 쿼터 수 #\n");
        playerList.sort(Comparator.comparingInt(PlayerDto::getPlayCount).reversed());

        for (PlayerDto playerDto : playerList) {
            sb.append(String.format("[ %s 선수 ] 불참횟수 : %d , 쿼터수 : %d \n", playerDto.getName(),
                    playerDto.getNonappearance(), playerDto.getPlayCount()));
        }
        return sb.toString();
    }

    private String validatePlayers(List<PlayerDto> playerList) {
        StringBuilder errorBuilder = new StringBuilder();
        Set<String> validPositions = EnumSet.allOf(Position.class).stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        for (PlayerDto player : playerList) {
            // 불참 횟수 검사
            if (player.getNonappearance() < 0) {
                errorBuilder.append("선수 '").append(player.getName())
                        .append("'의 불참 횟수는 0 이상의 정수여야 합니다.\n");
            }
        }

        int size = playerList.size();
        if (size < 14) {
            errorBuilder.append("선수는 14명 이상이어야 합니다. 13명으로는 3쿼터씩 뛰어서 4쿼터를 채울 수 없습니다.");
        }

        return errorBuilder.length() > 0 ? errorBuilder.toString() : null;
    }

    private void setTwoQuarterPlayers(List<PlayerDto> playerList) {
        int size = playerList.size();
        int x = 0, y = 0;

        for (int i = 0; i <= size; i++) {
            int tempX = i;
            int tempY = size - tempX;

            if ((3 * tempX + 2 * tempY == 40) && (tempX > tempY)) {
                x = tempX;
                y = tempY;
                break;
            }
        }

        for (int i = size - y; i < size; i++) {
            playerList.get(i).setMustPlayTwoQuarter(true);
        }
    }

    private void assignPosition(List<PlayerDto> playerList, QuarterDto quarterDto) {
        List<PlayerDto> notIncluded = new ArrayList<>();
        EnumMap<Position, PlayerDto> assignedPosition = quarterDto.getAssignedPosition();

        assignPreferPosition(playerList, assignedPosition, notIncluded, quarterDto.getQuarterNumber());

        if (!notIncluded.isEmpty()) {
            assignNotPreferPosition(assignedPosition, notIncluded);
        }
    }

    private void assignPreferPosition(List<PlayerDto> playerList, EnumMap<Position, PlayerDto> assignedPosition,
                                      List<PlayerDto> notIncluded, int quarterNumber) {
        for (PlayerDto playerDto : playerList) {
            if (playerDto.getPlayCount() >= 3 || (playerDto.getPlayCount() >= 2 && playerDto.getMustPlayTwoQuarter())) {
                continue;
            }

            boolean included = false;

            for (Position position : Position.values()) {
                if (!assignedPosition.containsKey(position)) {
                    if (playerDto.getPreferPositions().contains(position)) {
                        assignedPosition.put(position, playerDto);
                        playerDto.addPlayer(PREFER_SCORE);
                        included = true;
                        break;
                    } else if (quarterNumber == 3 && !playerDto.getMustPlayTwoQuarter()
                            && playerDto.getPlayCount() < 2) {
                        assignedPosition.put(position, playerDto);
                        playerDto.addPlayer(NOT_PREFER_SCORE);
                        included = true;
                        break;
                    }
                }
            }

            if (!included) {
                notIncluded.add(playerDto);
            }
        }
    }

    private void assignNotPreferPosition(EnumMap<Position, PlayerDto> assignedPosition,
                                         List<PlayerDto> notIncluded) {
        notIncluded.sort(Comparator.comparingInt(PlayerDto::getScore));

        for (Position position : Position.values()) {
            if (notIncluded.isEmpty()) {
                break;
            }

            if (!assignedPosition.containsKey(position)) {
                PlayerDto playerDto = notIncluded.get(0);
                if (playerDto.getPlayCount() >= 3) {
                    break;
                }
                assignedPosition.put(position, playerDto);
                playerDto.addPlayer(NOT_PREFER_SCORE);
                notIncluded.remove(playerDto);
            }
        }
    }
}
