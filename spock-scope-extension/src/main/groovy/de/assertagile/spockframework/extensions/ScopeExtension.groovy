package de.assertagile.spockframework.extensions

import groovy.util.logging.Slf4j
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.SpecInfo

/**
 * <p>
 * Extension for the <a href="http://spockframework.org">Spock Framework</a> to be able to mark specifications or
 * single features to belong to a (set of) scopes.
 * </p>
 *
 * <p>
 * Just mark a singe feature method or a whole {@link spock.lang.Specification} with the annotation {@link Scope} and
 * a list of configured scopes.
 * </p>
 */
@Slf4j
public class ScopeExtension extends AbstractAnnotationDrivenExtension<Scope> {

    /** {@link ConfigObject} for the extension. */
    private ConfigObject config

    /** The {@link List} of currently inclided scopes. */
    private List<String> includedScopes

    /** The {@link List} of currently excluded scopes. */
    private List<String> excludedScopes

    /**
     * Standard constructor.
     */
    public ScopeExtension() {}

    /**
     * Called when a marked specification type is visited. Sets the whole {@link SpecInfo} to be ignored if not in one
     * of the included scopes.
     *
     * @param annotation
     *          the {@link Scope} annotation at the specification type. Contains the specification's scopes.
     * @param spec
     *          the {@link SpecInfo} for the visited specification class.
     */
    public void visitSpecAnnotation(Scope annotation, SpecInfo spec) {
        if (!isInIncludedScopes(annotation) || isInExcludedScopes(annotation)) {
            log.debug("Skipping ${spec.name} for it is not in included scope (${includedScopes})")
            spec.setSkipped(true)
        }
    }

    /**
     * Called when a marked feature method is visited. Sets the {@link FeatureInfo} to be ignored if not in one of the
     * included scopes.
     *
     * @param annotation
     *          the {@link Scope} annotation at the specification type. Contains the specification's scopes.
     * @param feature
     *          the {@link FeatureInfo} for the visited feature method.
     */
    public void visitFeatureAnnotation(Scope annotation, FeatureInfo feature) {
        if (!isInIncludedScopes(annotation) || isInExcludedScopes(annotation)) {
            log.debug("Skipping ${feature.name} for it is not in included scope (${includedScopes})")
            feature.setSkipped(true)
        }
    }

    private ConfigObject getConfig() {
        if (!config) {
            URL configUrl = getClass().getClassLoader().getResource("SpockScopeConfig.groovy")
            config = configUrl ? new ConfigSlurper().parse(configUrl) : new ConfigObject()
        }
        return config
    }

    private List<String> getIncludedScopes() {
        if (includedScopes == null) {
            if (System.getProperty("spock.scopes")) {
                includedScopes = System.getProperty("spock.scopes")?.split(/\s*,\s*/)
            } else {
                includedScopes = getConfig().get("includedScopes") ?: []
            }
        }
        return includedScopes
    }

    private List<String> getExcludedScopes() {
        if (excludedScopes == null) {
            if (System.getProperty("spock.excludedScopes")) {
                excludedScopes = System.getProperty("spock.excludedScopes")?.split(/\s*,\s*/)
            } else {
                excludedScopes = getConfig().get("excludedScopes") ?: []
            }
        }
        return excludedScopes
    }

    private isInIncludedScopes(Scope annotation) {
        if (!getIncludedScopes()) return true
        List<String> specOrFeatureScopes = annotation.value() ?: []
        return !getIncludedScopes().disjoint(specOrFeatureScopes)
    }

    private isInExcludedScopes(Scope annotation) {
        if (!getExcludedScopes()) return false
        List<String> specOrFeatureScopes = annotation.value() ?: []
        return !getExcludedScopes().disjoint(specOrFeatureScopes)
    }
}