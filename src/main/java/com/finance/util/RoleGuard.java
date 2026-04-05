package com.finance.util;

import com.finance.Entity.user.Role;
import com.finance.exception.AccessDeniedException;
import com.finance.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class RoleGuard
{

    public CustomUserDetails currentUser()
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails))
        {
            throw new AccessDeniedException("Not authenticated");
        }
        return (CustomUserDetails) auth.getPrincipal();
    }

    public void requireAdmin()
    {
        if (currentUser().getRole() != Role.ADMIN)
        {
            throw new AccessDeniedException("Admin access required");
        }
    }

    public void requireAtLeastAnalyst()
    {
        Role role = currentUser().getRole();
        if (role == Role.VIEWER) {
            throw new AccessDeniedException("Analyst or Admin access required");
        }
    }

    public boolean isAdmin()
    {
        return currentUser().getRole() == Role.ADMIN;
    }

    public boolean isAtLeastAnalyst()
    {
        return currentUser().getRole() != Role.VIEWER;
    }
}
