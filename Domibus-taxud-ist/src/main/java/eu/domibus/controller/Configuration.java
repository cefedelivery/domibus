package eu.domibus.controller;


import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.AdminServlet;
import eu.domibus.plugin.webService.generated.BackendInterface;
import eu.domibus.plugin.webService.generated.BackendService11;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@org.springframework.context.annotation.Configuration
@ComponentScan({"eu.domibus.controller","eu.domibus.taxud"})
public class Configuration {

    @Value("${domibus.wsdl}")
    private String wsdlUrl;



    @Bean
    public BackendInterface backendInterface() throws MalformedURLException {
        BackendService11 backendService = new BackendService11(new URL(wsdlUrl),  new QName("http://org.ecodex.backend/1_1/", "BackendService_1_1"));
        BackendInterface backendPort = backendService.getBACKENDPORT();

        //enable chunking
        BindingProvider bindingProvider = (BindingProvider) backendPort;
        //comment the following lines if sending large files
        List<Handler> handlers = bindingProvider.getBinding().getHandlerChain();
        bindingProvider.getBinding().setHandlerChain(handlers);

        return backendPort;

    }

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


}
