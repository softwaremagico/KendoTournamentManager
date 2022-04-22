package com.softwaremagico.kt.core.controller;

import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.converters.RoleConverter;
import com.softwaremagico.kt.core.converters.models.RoleConverterRequest;
import com.softwaremagico.kt.core.providers.RoleProvider;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class RoleController extends BasicInsertableController<Role, RoleDTO, RoleRepository,
        RoleProvider, RoleConverterRequest, RoleConverter> {


    @Autowired
    public RoleController(RoleProvider provider, RoleConverter converter) {
        super(provider, converter);
    }

    @Override
    protected RoleConverterRequest createConverterRequest(Role entity) {
        return new RoleConverterRequest(entity);
    }

}
