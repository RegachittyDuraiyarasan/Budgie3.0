package com.hepl.budgie.service.impl.userinfo;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.userinfo.FamilyDTO;
import com.hepl.budgie.entity.userinfo.EmergencyContacts;
import com.hepl.budgie.entity.userinfo.Family;
import com.hepl.budgie.entity.userinfo.Sections;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.mapper.userinfo.FamilyMapper;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.userinfo.FamilyService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.IdGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FamilyServiceImplementation implements FamilyService {

    private final FamilyMapper familyMapper;

    private final UserInfoRepository userInfoRepository;

    private final JWTHelper jwtHelper;

    public FamilyServiceImplementation(FamilyMapper familyMapper, UserInfoRepository userInfoRepository, JWTHelper jwtHelper) {
        this.familyMapper = familyMapper;
        this.userInfoRepository = userInfoRepository;
        this.jwtHelper = jwtHelper;
    }

    @Override
    public UserInfo insertFamily(FormRequest formRequest, String empId) {
        // Fetch user info by employee ID
        Optional<UserInfo> userInfoOptional = userInfoRepository.findByEmpId(empId);

        if (userInfoOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }

        UserInfo userInfo = userInfoOptional.get();

        // Get or initialize Family object
        Family family = userInfo.getSections() != null && userInfo.getSections().getFamily() != null
                ? userInfo.getSections().getFamily()
                : new Family();

        // Ensure emergencyContacts is initialized
        if (family.getEmergencyContacts() == null) {
            family.setEmergencyContacts(new ArrayList<>());
        }

        // Map formRequest to Family entity
        Family newFamily = familyMapper.toEntity(formRequest);

        // Generate unique familyId
        List<EmergencyContacts> emergencyContacts = family.getEmergencyContacts();

        // Check if any existing emergency contact already has 'true'
        boolean existingEmergencyContactExists = emergencyContacts.stream()
//                .anyMatch(EmergencyContacts::isEmergencyContact);
                .anyMatch(contact -> contact.isEmergencyContact() && !"Deleted".equalsIgnoreCase(contact.getStatus()));

        // Ensure only one contact has emergencyContact: true
        for (EmergencyContacts contact : newFamily.getEmergencyContacts()) {
            if (contact.isEmergencyContact()) {
                if (existingEmergencyContactExists) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, AppMessages.EMERGENCY_CONTACT_EXISTS);
                }
                existingEmergencyContactExists = true; // Allow one true value
            }
        }

        String familyId = IdGenerator.generateFamilyId(emergencyContacts.size() + 1);

        // Assign familyId to each emergency contact
        if (newFamily.getEmergencyContacts() != null) {
            newFamily.getEmergencyContacts().forEach(contact -> contact.setFamilyId(familyId));
            emergencyContacts.addAll(newFamily.getEmergencyContacts());
        }

        // Update the Family and Sections objects
        family.setEmergencyContacts(emergencyContacts);

        Sections sections = userInfo.getSections() != null ? userInfo.getSections() : new Sections();

        sections.setFamily(family);
        userInfo.setSections(sections);

        // Save updated userInfo
        userInfoRepository.save(userInfo);
        return userInfo;
    }

    @Override
    public UserInfo updateFamily(FormRequest formRequest, String empId) {
        Optional<UserInfo> userInfoOptional = userInfoRepository.findByEmpId(empId);

        if (userInfoOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }

        UserInfo userInfo = userInfoOptional.get();

        Sections sections = userInfo.getSections();
        if (sections == null || sections.getFamily() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.FAMILY_DETAILS_NOT_FOUND);
        }

        Family existingFamily = sections.getFamily();
        String familyIdToUpdate = formRequest.getFormFields().get("familyId").toString();

        if (familyIdToUpdate == null || familyIdToUpdate.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.FAMILY_ID_IS_REQUIRED);
        }

        Family inputFamily = familyMapper.toEntity(formRequest);
        EmergencyContacts inputContact = inputFamily.getEmergencyContacts().get(0);
        boolean inputEmergencyContactValue = inputContact.isEmergencyContact();

        // Check if any contact in existingFamily has emergencyContact set to true
        List<EmergencyContacts> emergencyContacts = existingFamily.getEmergencyContacts();
        if (emergencyContacts == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.EMERGENCY_CONTACT_NOT_FOUND);
        }

        boolean updated = false;
        boolean hasTrueEmergencyContact = emergencyContacts.stream()
//                .anyMatch(EmergencyContacts::isEmergencyContact);
        .anyMatch(contact -> contact.isEmergencyContact() && !"Deleted".equalsIgnoreCase(contact.getStatus()));

        for (EmergencyContacts contact : emergencyContacts) {
            if (familyIdToUpdate.equals(contact.getFamilyId())) {
                // Condition 1: If existing and input. familyId are the same and current value
                // is true, allow update, (contact.isEmergencyContact())
                // Condition 2: If no one has true, allow setting true for the current input
                // familyId (!hasTrueEmergencyContact !inputEmergencyContactValue)
                if (contact.isEmergencyContact() || (!hasTrueEmergencyContact || !inputEmergencyContactValue)) {
                    contact.setContactName(inputContact.getContactName());
                    contact.setContactNumber(inputContact.getContactNumber());
                    contact.setGender(inputContact.getGender());
                    contact.setRelationship(inputContact.getRelationship());
                    contact.setMaritalStatus(inputContact.getMaritalStatus());
                    contact.setBloodGroup(inputContact.getBloodGroup());
                    contact.setEmergencyContact(inputEmergencyContactValue);
                    updated = true;
                    break;
                }
            }
        }

        if (!updated) {
            // Condition 3: If any other familyId has true in emergencyContact, block
            // setting true for this familyId
            if (hasTrueEmergencyContact && inputEmergencyContactValue) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.EMERGENCY_CONTACT_EXISTS);
            }

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.FAMILY_ID_NOT_FOUND);
        }

        userInfo.setSections(sections);
        userInfoRepository.save(userInfo);
        return userInfo;
    }

    @Override
    public FamilyDTO getFamily(String empId) {
        UserInfo userInfo = userInfoRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));

        Sections sections = userInfo.getSections();

        if (sections == null || sections.getFamily() == null) {
            throw new ResponseStatusException(HttpStatus.ACCEPTED, "");
        }

        Family family = sections.getFamily();

        List<EmergencyContacts> activeContacts = family.getEmergencyContacts()
                .stream()
                .filter(contact -> "Active".equalsIgnoreCase(contact.getStatus())).toList();

        family.setEmergencyContacts(activeContacts);

        return familyMapper.mapToDTO(family);
    }

    @Override
    public UserInfo deleteFamily(EmergencyContacts emergencyContacts, String empId) {
        Optional<UserInfo> userInfoOptional = userInfoRepository.findByEmpId(empId);
        if (userInfoOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }

        UserInfo userInfo = userInfoOptional.get();
        Sections sections = userInfo.getSections();
        if (sections == null || sections.getFamily() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.FAMILY_DETAILS_NOT_FOUND);
        }

        Family family = sections.getFamily();
        List<EmergencyContacts> emergencyContactsList = family.getEmergencyContacts();
        if (emergencyContactsList == null || emergencyContactsList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.EMERGENCY_CONTACT_NOT_FOUND);
        }

        String familyIdToDelete = Optional.ofNullable(emergencyContacts.getFamilyId())
                .filter(familId -> !familId.isEmpty())
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.FAMILY_ID_IS_REQUIRED));

        EmergencyContacts contactToDelete = emergencyContactsList.stream()
                .filter(contact -> familyIdToDelete.equals(contact.getFamilyId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.FAMILY_ID_NOT_FOUND));

        contactToDelete.setStatus("Deleted");
        userInfoRepository.save(userInfo);
        return userInfo;
    }
}
