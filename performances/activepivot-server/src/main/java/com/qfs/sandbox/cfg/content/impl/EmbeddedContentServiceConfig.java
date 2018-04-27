package com.qfs.sandbox.cfg.content.impl;

import com.qfs.contentserver.cfg.impl.ContentServiceConfig;
import com.qfs.pivot.content.IActivePivotContentService;
import com.qfs.pivot.content.impl.ActivePivotContentServiceBuilder;
import com.qfs.sandbox.cfg.impl.ActiveUIResourceServerConfig;
import com.qfs.server.cfg.content.IActivePivotContentServiceConfig;
import com.qfs.server.cfg.i18n.impl.LocalI18nConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

/**
 * Spring configuration of the <b>Content Service</b> backed by a local <b>Content Server</b>.
 *
 * <p> Since the content service is embedded in the current ActivePivot server when using
 * this configuration file, it also exposes the ActiveUI web application which is otherwise
 * exposed by the remote content server.
 *
 * @author Quartet FS
 */
@Import(value={
        LocalI18nConfig.class, // (I18n) Cube translation is set up from the file system
        ActiveUIResourceServerConfig.class, // (ActiveUI) Expose the ActiveUI web application

        // Reporting and scheduling configs.
        /*ReportingPluginsConfig.class,
        ReportingRestServicesConfig.class,
        SchedulingPluginsConfig.class,
        ContentServiceSchedulingConfig.class,*/
})
@Configuration
@Profile({EmbeddedContentServiceConfig.SPRING_PROFILE})
public class EmbeddedContentServiceConfig extends ContentServiceConfig implements IActivePivotContentServiceConfig {

    /** The name of the Spring profile that enables this configuration file */
    public static final String SPRING_PROFILE = "embedded-content";

    @Bean
    public IActivePivotContentServiceConfig apCSConfig() {
        return new EmbeddedContentServiceConfig();
    }

    @Bean
    public IActivePivotContentService activePivotContentService() {
        return new ActivePivotContentServiceBuilder()
                .with(contentService())
                .withCacheForEntitlements(-1)

                // WARNING: In production, you should not keep the next lines, which will erase
                // parts of your remote configuration. Prefer pushing them manually using the
                // PushToContentServer utility class before starting the ActivePivot server.

                // Setup directories and permissions
                /*.needInitialization(
                        env.getRequiredProperty(CALCULATED_MEMBER_ROLE_PROPERTY),
                        env.getRequiredProperty(KPI_ROLE_PROPERTY))*/
                .build();
    }

}
