/*
 * This product includes software developed at
 * The Apache Software Foundation (http://www.apache.org/).
 *
 * The following software was modified for this product:
 *     Apache Commons VFS (Sandbox)
 *     Copyright 2002-2016 The Apache Software Foundation
 */
package eu.domibus.plugin.fs.vfs.smb;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.GenericFileName;

/**
 * An SMB URI. Adds a share name to the generic URI.
 *
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class SmbFileName extends GenericFileName {

    private static final int DEFAULT_PORT = 139;

    private final String share;
    private final String domain;
    private String uriWithoutAuth;

    protected SmbFileName(
            final String scheme,
            final String hostName,
            final int port,
            final String userName,
            final String password,
            final String domain,
            final String share,
            final String path,
            final FileType type) {
        super(scheme, hostName, port, DEFAULT_PORT, userName, password, path, type);
        this.share = share;
        this.domain = domain;
    }

    /**
     * Returns the share name.
     *
     * @return share name
     */
    public String getShare() {
        return share;
    }

    /**
     * Builds the root URI for this file name.
     */
    @Override
    protected void appendRootUri(final StringBuilder buffer, final boolean addPassword) {
        super.appendRootUri(buffer, addPassword);
        buffer.append('/');
        buffer.append(share);
    }

    /**
     * Put {@code domain} before @{code username} if both are set.
     * <p>
     * Uses super method to add password or password placeholder.
     */
    @Override
    protected void appendCredentials(final StringBuilder buffer, final boolean addPassword) {
        if (getDomain() != null && getDomain().length() != 0
                && getUserName() != null && getUserName().length() != 0) {
            buffer.append(getDomain());
            buffer.append("\\");
        }
        super.appendCredentials(buffer, addPassword);
    }

    /**
     * Factory method for creating name instances.
     *
     * @param path path of file.
     * @param type file or directory
     * @return new SmbFileName object, never null.
     */
    @Override
    public FileName createName(final String path, final FileType type) {
        return new SmbFileName(
                getScheme(),
                getHostName(),
                getPort(),
                getUserName(),
                getPassword(),
                domain,
                share,
                path,
                type);
    }

    /**
     * Construct the path suitable for SmbFile when used with
     * NtlmPasswordAuthentication.
     *
     * @return caches and return URI with no username/password, never null
     * @throws FileSystemException if any of the invoked methods throw
     */
    public String getUriWithoutAuth() throws FileSystemException {
        if (uriWithoutAuth != null) {
            return uriWithoutAuth;
        }

        final StringBuilder sb = new StringBuilder(120);
        sb.append(getScheme());
        sb.append("://");
        sb.append(getHostName());
        if (getPort() != DEFAULT_PORT) {
            sb.append(":");
            sb.append(getPort());
        }
        sb.append("/");
        sb.append(getShare());
        sb.append(getPathDecoded());
        uriWithoutAuth = sb.toString();
        return uriWithoutAuth;
    }

    /**
     * Returns the domain name.
     *
     * @return domain name
     */
    public String getDomain() {
        return domain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SmbFileName smbFileName = (SmbFileName) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(share, smbFileName.share)
                .append(domain, smbFileName.domain)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(share)
                .append(domain)
                .toHashCode();
    }

}
