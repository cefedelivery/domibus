/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.ebms3.common;

import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * This class is responsible for the generation of timestamps in an ebMS3 conformant way.
 *
 * @author Christian Koch, Stefan Mueller
 */
public class TimestampDateFormatter {

    @Autowired
    private SimpleDateFormat xmlDateTimeFormat;


    public String generateTimestamp() {
        this.xmlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return this.xmlDateTimeFormat.format(new Date());
    }

}
