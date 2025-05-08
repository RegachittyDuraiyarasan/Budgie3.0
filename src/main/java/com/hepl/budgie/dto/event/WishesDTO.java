package com.hepl.budgie.dto.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WishesDTO {

    private int month;
    private int year;
    private int date;
    private String employee;

}
