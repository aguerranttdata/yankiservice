package com.group7.yanki.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageKafka {
    private String type;
    private String document;
    private Long number;
    private Boolean success;
    private Double amount;
    private String message;
}
