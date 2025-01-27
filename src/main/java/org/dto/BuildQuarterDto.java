package org.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BuildQuarterDto {
    private Map<Integer, QuarterDto> quarters;
    private String description;

    public static BuildQuarterDto of(Map<Integer, QuarterDto> quarters, String description) {
        return new BuildQuarterDto(quarters, description);
    }
}
