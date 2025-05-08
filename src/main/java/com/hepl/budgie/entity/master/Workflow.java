package com.hepl.budgie.entity.master;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Workflow {

    private List<String> role;
    private String roleSpecific;
    private Integer sequence;
    private List<Actions> actions;

}
