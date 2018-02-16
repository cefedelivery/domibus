package eu.domibus.controller;

import eu.domibus.plugin.webService.generated.BackendInterface;
import eu.domibus.plugin.webService.generated.BackendService11;
import org.springframework.beans.factory.annotation.Value;
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
    public BackendInterface backendInterface() throws MalformedURLException {
        BackendService11 backendService = new BackendService11(new URL(wsdlUrl),  new QName("http://org.ecodex.backend/1_1/", "BackendService_1_1"));
        BackendInterface backendPort = backendService.getBACKENDPORT();
        return backendPort;
    }
}
