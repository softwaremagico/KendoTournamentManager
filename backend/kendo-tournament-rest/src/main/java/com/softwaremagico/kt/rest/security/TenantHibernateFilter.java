package com.softwaremagico.kt.rest.security;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.softwaremagico.kt.persistence.entities.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

/**
 * Enables the tenant predicate for every Spring Data repository invocation.
 */
@Aspect
@Component
public class TenantHibernateFilter {
    @PersistenceContext
    private EntityManager entityManager;

    @Around("execution(* com.softwaremagico.kt.persistence.repositories..*(..))")
    public Object scopeRepositoryAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        final Integer tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            entityManager.unwrap(Session.class).enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        }
        return joinPoint.proceed();
    }
}
