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
            description = "Required. File ID (Telegram ID/Server File UUID/MCP Root Filepath)",
            required = true
    )
    String fileId;

    @McpToolParam(
            description = """
                    Required. Represents file upload source, can be "TELEGRAM", "SERVER" and "LOCAL".
                    If TELEGRAM specified, file_id property should be Telegram File Id that already
                    is uploaded to Telegram Servers, it can be obtained from Telegram Media Metadata.
                    If SERVER source specified, file_id property should contain unique file UUID from
                    original file upload response (due POST /files/upload endpoint).
                    Notice that server files have TTL - 24 hours.
                    If MCP server deployed locally, "LOCAL" upload is also available, with this option
                    fileId should contain file absolute path on local machine, only specific directories
                    (MCP roots) is allowed.
                    """,
            required = true
    )
    String fileSource;

    @McpToolParam(
            description = "Optional. File name that will be assigned to file"
    )
    String fileName;
}
