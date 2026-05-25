package com.codecraft.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Judge0Result {

    private String token;
    private String stdout;
    private String stderr;

    @JsonProperty("compile_output")
    private String compileOutput;

    private Judge0Status status;
    private Double time;
    private Integer memory;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Judge0Status {
        private int id;
        private String description;
    }
}
