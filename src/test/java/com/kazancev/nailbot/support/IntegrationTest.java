package com.kazancev.nailbot.support;

import com.kazancev.nailbot.config.SalonProperties;
import com.kazancev.nailbot.service.AppointmentService;
import com.kazancev.nailbot.service.ClientService;
import com.kazancev.nailbot.service.SlotService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnableConfigurationProperties(SalonProperties.class)
@Import({PostgresContainerConfig.class, AppointmentService.class, ClientService.class, SlotService.class})
public abstract class IntegrationTest {
}
