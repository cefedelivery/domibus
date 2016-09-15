package eu.domibus.property;

/**
 * Created by Cosmin Baciu on 01-Jul-16.
 */
public class PropertyResolverBuilder {

    private PropertyResolver propertyResolver;

    public static PropertyResolverBuilder create() {
        return new PropertyResolverBuilder();
    }

    private PropertyResolverBuilder() {
        this.propertyResolver = new PropertyResolver();
    }

    public PropertyResolverBuilder startDelimiter(String startDelimiter) {
        propertyResolver.setStartDelimiter(startDelimiter);
        return this;
    }

    public PropertyResolverBuilder endDelimiter(String endDelimiter) {
        propertyResolver.setEndDelimiter(endDelimiter);
        return this;
    }

    public PropertyResolverBuilder resolveLevel(Integer resolveLevel) {
        propertyResolver.setResolveLevel(resolveLevel);
        return this;
    }

    public PropertyResolver build() {
        return propertyResolver;
    }
}
