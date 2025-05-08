package com.hepl.budgie.service.payroll;

import com.hepl.budgie.dto.payroll.PayrollComponentDTO;
import com.hepl.budgie.dto.payroll.PayrollPayTypeCompDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.payroll.PayrollComponent;

import java.util.List;
import java.util.Map;

public interface PayrollComponentService {
    List<PayrollComponent> fetch();
    List<PayrollPayTypeCompDTO> list(List<String> componentType, String payType);
    void upsert(String id, PayrollComponentDTO request);
    void upsertComponent(String id, PayrollComponentDTO request);

    void delete(String id);

    String status(String id);

    void update(PayrollComponentDTO request);
}
