package com.labndbnb.landbnb.service.scheduler;

import com.labndbnb.landbnb.service.definition.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingStatusScheduler {

    private final BookingService bookingService;

    // Se ejecuta todos los días a las 3:00 AM
    @Scheduled(cron = "0 0 3 * * ?")
    public void autoCompletePastBookings() {
        log.info("Starting automatic booking completion process...");
        try {
            bookingService.autoCompletePastBookings();
            log.info("Automatic booking completion process finished successfully");
        } catch (Exception e) {
            log.error("Error during automatic booking completion: {}", e.getMessage());
        }
    }

    // Opcional: También se puede ejecutar cada hora para mayor precisión
    @Scheduled(cron = "0 0 * * * ?") // Cada hora en el minuto 0
    public void autoCompletePastBookingsHourly() {
        log.debug("Running hourly booking completion check...");
        try {
            bookingService.autoCompletePastBookings();
        } catch (Exception e) {
            log.error("Error during hourly booking completion: {}", e.getMessage());
        }
    }
}