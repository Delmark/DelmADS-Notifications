package ru.delmark.dads.notifications.mcp.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.ai.mcp.annotation.McpToolParam;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AttachedFile {
    @McpToolParam(
            description = "Required. File ID (Telegram ID/Server File UUID/Local Absolute Filepath)",
            required = true
    )
    String fileId;

    @McpToolParam(
            description = "Required. File source, if file was uploaded to server:" +
                          "'SERVER', if MCP deployed locally - 'LOCAL'," +
                          " if file from TG/already has Telegram ID - 'TELEGRAM'",
            required = true
    )
    String fileSource;

    @McpToolParam(
            description = "Optional. File name that will be assigned to file"
    )
    String fileName;
}
