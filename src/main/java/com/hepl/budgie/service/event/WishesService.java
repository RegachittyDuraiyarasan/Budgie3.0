package com.hepl.budgie.service.event;

import java.util.List;

import com.hepl.budgie.dto.employee.EmployeeOrgChartDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.event.WishesType;

public interface WishesService {

    List<EmployeeOrgChartDTO> getBirthdays(int limit, String yearAndMonth, String employee, WishesType type);

    List<EmployeeOrgChartDTO> getAnniversary(int limit, String yearAndMonth, String employee, WishesType type);

    void sendMail(String from, Status type);

}
