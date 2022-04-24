package com.softwaremagico.kt.core.controller;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2022 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *  
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *  
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.core.converters.DuelConverter;
import com.softwaremagico.kt.core.converters.models.DuelConverterRequest;
import com.softwaremagico.kt.core.providers.DuelProvider;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.repositories.DuelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class DuelController extends BasicInsertableController<Duel, DuelDTO, DuelRepository,
        DuelProvider, DuelConverterRequest, DuelConverter> {


    @Autowired
    public DuelController(DuelProvider provider, DuelConverter converter) {
        super(provider, converter);
    }

    @Override
    protected DuelConverterRequest createConverterRequest(Duel entity) {
        return new DuelConverterRequest(entity);
    }

}
