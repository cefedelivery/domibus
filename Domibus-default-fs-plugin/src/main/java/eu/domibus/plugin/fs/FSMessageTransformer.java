/*
 * Copyright 2017 Domibus FS Plugin Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *https://joinup.ec.europa.eu/sites/default/files/eupl1.1.-licence-en_0.pdf
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */
package eu.domibus.plugin.fs;

import eu.domibus.plugin.Submission;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * This class is responsible for transformations from {@link FSMessage} to
 * {@link eu.domibus.plugin.Submission} and vice versa
 *
 * @author @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSMessageTransformer
        implements MessageRetrievalTransformer<FSMessage>, MessageSubmissionTransformer<FSMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSMessageTransformer.class);

    /**
     * The default properties to be used
     */
    private final Properties properties;
    
    /**
     * Creates a new <code>FSMessageTransformer</code>.
     */
    public FSMessageTransformer() {
        properties = new Properties();
    }
    
    /**
     * Creates a new <code>FSMessageTransformer</code> with the given properties.
     * 
     * @param defaultProperties Default properties
     * @throws java.io.IOException
     */
    public FSMessageTransformer(String defaultProperties) throws IOException {
        properties = new Properties();
        properties.load(new FileReader(defaultProperties));
    }

    /**
     * Transforms {@link eu.domibus.plugin.Submission} to {@link FSMessage}
     *
     * @param submission the message to be transformed
     * @param messageOut output target
     *
     * @return result of the transformation as {@link FSMessage}
     */
    @Override
    public FSMessage transformFromSubmission(final Submission submission, final FSMessage messageOut) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Transforms {@link FSMessage} to {@link eu.domibus.plugin.Submission}
     *
     * @param messageIn the message ({@link FSMessage}) to be tranformed
     * @return the result of the transformation as
     * {@link eu.domibus.plugin.Submission}
     */
    @Override
    public Submission transformToSubmission(final FSMessage messageIn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
