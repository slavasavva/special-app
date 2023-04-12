package searchengine.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponse {
    @NotEmpty
    boolean result;
    private String message;
    private int count;
    private List<FoundPage> data;

    public SearchResponse(boolean result, String message) {
        this.result = result;
        this.message = message;
    }

    @Override
    public String toString() {
        return "SearchResponse{" +
                "result=" + result +
                ", message='" + message + '\'' +
                ", count=" + count +
                ", data=" + data +
                '}';
    }
}
