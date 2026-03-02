package com.dining.fluxo.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Table {
    private Integer id;
    private Integer number;
    private Integer capacity;
}
