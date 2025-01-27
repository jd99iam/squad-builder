package org.dto;

import java.util.EnumMap;
import lombok.Data;
import lombok.Setter;

@Data
public class QuarterDto {
    private EnumMap<Position, PlayerDto> assignedPosition = new EnumMap<>(Position.class);
    private int quarterNumber;

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        for (Position position : Position.values()) {
            sb.append(String.format("%s : %s \n", position.name(), assignedPosition.get(position)));
        }
        String info = sb.toString();

        return "[" + quarterNumber + "쿼터]\n" + info;
    }
}
