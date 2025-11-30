package com.msk.salaryflow.configuration;

import com.msk.salaryflow.entity.EventLog;
import com.msk.salaryflow.repository.EventLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final EventLogRepository eventLogRepository;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();

            EventLog log = new EventLog(
                    "LOGIN",
                    "Security",
                    username,
                    null,
                    username
            );

            eventLogRepository.save(log);
        }
    }
}