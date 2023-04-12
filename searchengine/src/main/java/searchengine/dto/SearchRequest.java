package searchengine.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class SearchRequest {
    @NotEmpty
    private final String query;

    private final String site;

    private final int offset;

    private final int limit;
}
