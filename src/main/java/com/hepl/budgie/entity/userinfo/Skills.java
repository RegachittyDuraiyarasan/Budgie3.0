package com.hepl.budgie.entity.userinfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Skills {
    private List<String> primary;
    private List<String> secondary;
}
