package com.ajay.example.mongo;

import io.jaegertracing.Configuration;
import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.samplers.ConstSampler;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class UserDBConfig {
    @Bean
    public JaegerTracer jaegerTracer(){
        Configuration config = new Configuration("mongo-service")
                .withSampler(new Configuration.SamplerConfiguration().withType(ConstSampler.TYPE).withParam(1))
                .withReporter(new Configuration.ReporterConfiguration().withLogSpans(true));
        return config.getTracer();
    }
}
