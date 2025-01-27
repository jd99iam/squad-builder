package org.dto;

import java.util.List;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class PlayerDto {
    private String name;
    private List<Position> preferPositions;
    private Integer score = 0;
    private Integer playCount = 0;
    private Integer nonappearance = 0;
    private Boolean mustPlayTwoQuarter = false;

    public PlayerDto(String name, List<Position> preferPositions, Integer nonappearance) {
        this.name = name;
        this.preferPositions = preferPositions;
        this.nonappearance = nonappearance;
    }

    public void addScore(int add) {
        this.score += add;
    }

    public void addPlayCount() {
        this.playCount += 1;
    }

    public void addPlayer(int add) {
        addScore(add);
        addPlayCount();
    }
}
