
[![License badge](https://img.shields.io/badge/license-EUPL-blue.svg)](https://ec.europa.eu/cefdigital/wiki/download/attachments/52601883/eupl_v1.2_en%20.pdf?version=1&modificationDate=1507206778126&api=v2)
[![Documentation badge](https://img.shields.io/badge/docs-latest-brightgreen.svg)](https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/Domibus)
[![Docker](https://img.shields.io/badge/docker-25-yellowgreen.svg)](https://hub.docker.com/r/fiware/domibus-tomcat/)
[![Support badge]( https://img.shields.io/badge/support-sof-yellowgreen.svg)](https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/Support)

# <a name="top"></a>Domibus 
### Sample implementation, open source project of the eDelivery AS4 Access Point.

* [Introduction](#introduction)
* [GEi overall description](#gei-overall-description)
* [Build](#build)
* [Install and run](#installandrun)
* [Default plugins](#defaultplugins)
* [Testing](#testing)
    * [Ent-to-end tests](#ent-to-end-tests)
    * [Unit Tests](#unit-tests)
* [License](#license)
* [Support](#support)
		  
## Introduction

This is the code repository for Domibus, the sample implementation, open source project of the European Commission AS4 Access Point.

This project is part of [FIWARE](http://www.fiware.org). Check also the [FIWARE Catalogue entry for Domibus](https://catalogue.fiware.org/enablers/electronic-data-exchange-domibus)

Any feedback on this documentation is highly welcome, including bugs, typos
or things you think should be included but aren't. You can use [JIRA](https://ec.europa.eu/cefdigital/tracker/projects/EDELIVERY/issues) to provide feedback.

Following documents are available on the [Domibus release page](https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/Domibus):
*   Quick Start Guide
*   Administration Guide 
*   Testing guide
*   Interface Control Documents of the default plugins
*   Plugin cookbook 
*   Software Architecture Document


[Top](#top)

## GEi overall description

The CEF eDelivery Access Point (AP) implements a standardised message exchange protocol that ensures interoperable, secure and reliable data exchange.
Domibus is the Open Source project of the AS4 Access Point maintained by the European Commission. 

If this is your first contact with the CEF eDelivery Access Point, it is highly recommended to check the [CEF eDelivery Access Point Component offering description](https://ec.europa.eu/cefdigital/wiki/download/attachments/46992278/%28CEFeDelivery%29.%28AccessPoint%29.%28COD%29.%28v1.04b%29.pdf?version=1&modificationDate=1493385571398&api=v2) available on the [Access Point Software](https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/Access+Point+software) page.

[Top](#top)

## Build

To build Domibus for Tomcat 8, Wildfly 9 and Weblogic 12c, including all release artifacts use the following profiles:

    mvn clean install -Ptomcat -Pweblogic -Pwildfly -Pdefault-plugins -Pdatabase -Psample-configuration -PUI -Pdistribution


[Top](#top)

## Install and run

How to install and run Domibus can be read in the Quick Start Guide and more advanced documentation is available in the Administration Guide, both available on the [Domibus Release Page](https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/Domibus).

[Top](#top)

## Testing

### Ent-to-end tests

The end-to-end tests are manually performed by the testing team using SoapUI PRO. 
For further information please check the Testing Guide available on the [Domibus Release Page](https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/Domibus). 

A restricted set of tests that do not require any interactivity may run with the soapui-pro-maven-plugin from com.smartbear.soapui. The maven command to run the tests is:

    mvn com.smartbear.soapui:soapui-pro-maven-plugin:5.1.2:test

### Unit Tests

To run unit test via Maven, issue this command : 

    mvn test

In Domibus there are two types of tests implemented using JUnit: unit tests (java classes ending in *Test.java) and integration tests (java classes ending in *IT.java)
To skip the unit tests from the build process:

    mvn clean install -DskipTests=true -DskipITs=true

[Top](#top)

## Default plugins

The purpose of Domibus is to facilitate B2B communication. To achieve this goal it provides a very flexible plugin model which allows the integration with nearly all back office applications. 
Domibus offers three default plugins, available with the Domibus distribution:

*   Web Service plugin
*   JMS plugin
*   File System plugin

The Interface Control Document (ICD) of the default JMS plugin outlines the JMS Data Format Exchange to be used as part of the default JMS backend plugin.
The Interface Control Document (ICD) of the default WS plugin describes the WSDL and the observable behaviour of the interface provided in the default WS plugin
Both documents are available on the [Domibus Release Page](https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/Domibus)

[Top](#top)

## License

Domibus is licensed under European Union Public Licence (EUPL) version 1.2.

[Top](#top)

## Support

Have questions? Consult our [Q&A section](https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/Domibus+FAQs). 
Ask your thorough programming questions using [stackoverflow](http://stackoverflow.com/questions/ask)
and your general questions on [FIWARE Q&A](https://ask.fiware.org). In both cases please use the tag `context.domibus`.

Still have questions? Contact [eDelivery support](https://ec.europa.eu/cefdigital/tracker/servicedesk/customer/portal/2/create/4).


[Top](#top)