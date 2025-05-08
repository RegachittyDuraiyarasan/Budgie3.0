package com.hepl.budgie.entity.menu;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SubMenu extends CommonMenuFields {
    @Id
    private String id;
    private String subMenuId;
}
