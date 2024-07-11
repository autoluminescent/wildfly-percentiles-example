package org.example;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
@ApplicationScoped
public class RootResource {
    static final String METER_PERFORMED_CHECKS = "prime_performedChecks";

    @Inject
    private MeterRegistry registry;

    @PostConstruct
    private void createMeters() {
        registry.config().meterFilter(new MeterFilter() {
            @Override
            public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                return DistributionStatisticConfig.builder()
                        .percentiles(0.50, 0.90, 0.95, 0.99)
                        .percentilesHistogram(true)
                        .build()
                        .merge(config);
            }

        });
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getRootResponse() {
        return "Micrometer quickstart deployed successfully. You can find the available operations in the included README file.";
    }

    @GET
    @Path("/prime/{number}")
    public String checkIfPrime(@PathParam("number") long number) throws Exception {
        registry.summary(METER_PERFORMED_CHECKS).record(number);

        if (number < 1) {
            return "Only natural numbers can be prime numbers.";
        }

        if (number == 1) {
            return "1 is not prime.";
        }

        if (number == 2) {
            return "2 is prime.";
        }

        if (number % 2 == 0) {
            return number + " is not prime, it is divisible by 2.";
        }

        for (int i = 3; i < Math.floor(Math.sqrt(number)) + 1; i = i + 2) {
            if (number % i == 0) {
                return number + " is not prime, is divisible by " + i + ".";
            }
        }

        return number + " is prime.";
    }

}
