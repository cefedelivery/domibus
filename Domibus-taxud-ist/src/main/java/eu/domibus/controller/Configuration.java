package eu.domibus.controller;

import com.codahale.metrics.servlets.AdminServlet;
import eu.domibus.example.ws.WebserviceExample;
import eu.domibus.plugin.webService.generated.BackendInterface;
import eu.domibus.plugin.webService.generated.BackendService11;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;

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
    public WebserviceExample backendInterface() throws MalformedURLException {
        return new WebserviceExample(wsdlUrl);
    }
  /*  @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }*/

}
