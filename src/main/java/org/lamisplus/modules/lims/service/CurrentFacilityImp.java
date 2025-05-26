package org.lamisplus.modules.lims.service;

import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.base.domain.entities.User;
import org.lamisplus.modules.base.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CurrentFacilityImp implements CurrentFacility {

    private final UserService userService;

    @Override
    public Long getCurrentUserOrganization() {
        Optional<User> userWithRoles = userService.getUserWithRoles();
        return userWithRoles.map(User::getCurrentOrganisationUnitId).orElseGet(() -> null);
    }
}
