package eu.domibus.controller;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@org.springframework.context.annotation.Configuration
@ComponentScan({"eu.domibus.controller","eu.domibus.taxud"})

public class Configuration {


    @Bean
    public HealthCheckRegistry healthCheckRegistry(){
        return new HealthCheckRegistry();
    }

    @Bean
    public MetricRegistry metricRegistry(){
        return new MetricRegistry();
    }
    @Bean
    public MetricsServletContextListener metricsServletContextListener(MetricRegistry metricRegistry, HealthCheckRegistry healthCheckRegistry) {
        return new MetricsServletContextListener(metricRegistry, healthCheckRegistry);
    }

    @Bean
    public ServletRegistrationBean servletRegistrationBean(){
        return new ServletRegistrationBean(new AdminServlet(),"/metrics/*");
    }



 /*   @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http) {
        return http.authorizeExchange()
                .pathMatchers("/actuator/**").permitAll()
                .anyExchange().authenticated()
                .and().build();
    }*/

    /*@Bean
    public ServletRegistrationBean exampleServletBean(HealthCheckRegistry healthCheckRegistry,MetricRegistry metricRegistry) {
        final AdminServlet servlet = new AdminServlet();
        servlet.getServletContext().setAttribute(MetricsServlet.METRICS_REGISTRY, metricRegistry);
        servlet.getServletContext().setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY, healthCheckRegistry);
        ServletRegistrationBean bean = new ServletRegistrationBean(
                servlet, "/metrics/*");
        bean.setLoadOnStartup(1);
        return bean;
    }*/

    /*@Override
    public void configureReporters(MetricRegistry metricRegistry) {
        registerReporter(ConsoleReporter
                .forRegistry(metricRegistry)
                .build())
                .start(1, TimeUnit.MINUTES);
        servletContext.setAttribute(MetricsServlet.METRICS_REGISTRY, metricRegistry);
        servletContext.setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY, healthCheckRegistry);
    }*/



}
