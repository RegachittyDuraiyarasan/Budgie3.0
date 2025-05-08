package com.hepl.budgie.service.impl.payroll;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.payroll.PayrollPfDTO;
import com.hepl.budgie.dto.payroll.PayrollPfListDTO;
import com.hepl.budgie.entity.payroll.PayrollPf;
import com.hepl.budgie.facade.DataInitFacade;
import com.hepl.budgie.mapper.payroll.PayrollPfMapper;
import com.hepl.budgie.repository.payroll.PayrollPfRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.payroll.PayrollPfService;
import com.hepl.budgie.utils.AppUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
public class PayrollPfServiceImpl implements PayrollPfService {

    private final PayrollPfRepository payrollPfRepository;
    private final PayrollPfMapper payrollPfMapper;
    private final UserInfoRepository userInfoRepository;
    private final JWTHelper jwtHelper;
    private final MongoTemplate mongoTemplate;
    private static final String PF_SEQUENCE = "PF000";

    public PayrollPfServiceImpl(PayrollPfRepository payrollPfRepository, PayrollPfMapper payrollPfMapper,
                                MongoTemplate mongoTemplate, DataInitFacade dataInitFacade, UserInfoRepository userInfoRepository, JWTHelper jwtHelper) {
        this.payrollPfMapper = payrollPfMapper;
        this.payrollPfRepository = payrollPfRepository;
        this.mongoTemplate = mongoTemplate;

        this.userInfoRepository = userInfoRepository;
        this.jwtHelper = jwtHelper;
    }

    @Override
    public void addorupdate(PayrollPfDTO request , String operation,String id) {
        boolean resultpf = false;
        if(operation.equalsIgnoreCase("save")) {
            request.setPfId(PayrollPfRepository.findTopByOrderByIdDescpf("IN", mongoTemplate)
                    .map(e -> AppUtils.generateUniqueId(e.getPfId()))
                    .orElse(AppUtils.generateUniqueId(PF_SEQUENCE)));
            resultpf  = PayrollPfRepository.upsertPf(request, mongoTemplate, "IN",id);

        }else if (operation.equalsIgnoreCase("update")) {
            PayrollPfRepository.findBypfId(mongoTemplate,"IN",id)
                    .orElseThrow(()->new NoSuchElementException("PF Id is not found"));
             resultpf = PayrollPfRepository.upsertPf(request, mongoTemplate, "IN",id);

        }
        //      PayrollPf payrollpf = PayrollPfRepository.save(payrollPfMapper.toEntity(request));
        if(!resultpf)
                throw new RuntimeException("PF ID doesn't match : ");

    }


    public List<PayrollPf> list(){
        List<String> orgList=List.of("ORG001");
        return PayrollPfRepository.findByOrgId(mongoTemplate,"IN",orgList);

    }

    public List<PayrollPf> listpf(String orgid){
//        List<String> orgList=List.of("ORG001");
        return PayrollPfRepository.findBypflist(mongoTemplate,orgid);

    }

    public void delete(String id){
        PayrollPfRepository.findBypfId(mongoTemplate,"IN",id)
                .orElseThrow(()->new NoSuchElementException("PF Id is not found"));
        PayrollPfRepository.deletePF(id,mongoTemplate,"IN");
    }



    public void addorupdate(PayrollPfDTO request, String id) {
        request.setPfId(PayrollPfRepository.findTopByOrderByIdDescpf("IN", mongoTemplate)
                .map(e -> AppUtils.generateUniqueId(e.getPfId()))
                .orElse(AppUtils.generateUniqueId(PF_SEQUENCE)));
        // PayrollPf payrollpf =
        // PayrollPfRepository.save(payrollPfMapper.toEntity(request));
        boolean resultpf = PayrollPfRepository.upsertPf(request, mongoTemplate, "IN", id);
        if (!resultpf)
            throw new RuntimeException("PF ID doesn't match : ");

    }

    @Override
    public List<PayrollPfListDTO> listIndex() {
        return userInfoRepository.findByPfEmployeeDetails(mongoTemplate, jwtHelper.getOrganizationCode());
    }

}
