package searchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SearchResponse {
    private boolean result;

    private String error;

    private int count = 0;

    private List<FoundPage> data = null;

    public SearchResponse(boolean result, String error) {
        this.result = result;
        this.error = error;
    }
}
