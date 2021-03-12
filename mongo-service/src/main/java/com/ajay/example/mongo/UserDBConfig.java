package com.ajay.example.mongo;

import io.jaegertracing.Configuration;
import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.samplers.ConstSampler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class UserDBConfig {
    @Value("${jaeger.agent.host}")
    private String jaegerAgentHost;
    @Value("${jaeger.agent.port}")
    private String jaegerAgentPort;
    @Bean
    public JaegerTracer jaegerTracer(){
        Configuration config = new Configuration("mongo-service")
                .withSampler(new Configuration.SamplerConfiguration().withType(ConstSampler.TYPE).withParam(1))
                .withReporter(new Configuration.ReporterConfiguration().withSender(new Configuration.SenderConfiguration().withAgentHost(jaegerAgentHost).withAgentPort(Integer.valueOf(jaegerAgentPort))).withLogSpans(true));
        return config.getTracer();
    }
}
