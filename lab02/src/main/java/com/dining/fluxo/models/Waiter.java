package com.dining.fluxo.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Waiter {
    private Integer id;
    private String name;
    private String email;
}
