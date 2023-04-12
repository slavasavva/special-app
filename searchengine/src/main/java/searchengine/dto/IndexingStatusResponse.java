package searchengine.dto;

import lombok.Data;

@Data
public class IndexingStatusResponse {
    private final boolean result;

    private final String error;
}
