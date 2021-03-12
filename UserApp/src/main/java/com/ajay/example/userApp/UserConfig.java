package com.ajay.example.userApp;

import io.jaegertracing.Configuration;
import io.jaegertracing.Configuration.ReporterConfiguration;
import io.jaegertracing.Configuration.SamplerConfiguration;
import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.jaegertracing.internal.samplers.ProbabilisticSampler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@org.springframework.context.annotation.Configuration
@ComponentScan("com.ajay.example.userApp")
public class UserConfig {
    @Value("${jaeger.agent.host}")
    private String jaegerAgentHost;
    @Value("${jaeger.agent.port}")
    private String jaegerAgentPort;
    @Bean
    public JaegerTracer jaegerTracer(){
        Configuration config = new Configuration("user-service")
                                                .withSampler(new SamplerConfiguration().withType(ConstSampler.TYPE).withParam(1))
                                                .withReporter(new ReporterConfiguration().withSender(new Configuration.SenderConfiguration().withAgentHost(jaegerAgentHost).withAgentPort(Integer.valueOf(jaegerAgentPort))).withLogSpans(true));
        return config.getTracer();
    }

}
