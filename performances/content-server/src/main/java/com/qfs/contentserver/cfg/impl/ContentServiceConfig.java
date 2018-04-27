package com.qfs.contentserver.cfg.impl;

import com.qfs.content.cfg.IContentServiceConfig;
import com.qfs.content.service.IContentService;
import com.qfs.content.service.audit.impl.AuditableHibernateContentService;
import com.qfs.sandbox.cfg.impl.ContentServerUtil;
import org.hibernate.cfg.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ContentServiceConfig implements IContentServiceConfig {

    /**
     * The content service is a bean which can be used by ActivePivot server to store:
     * <ul>
     * <li>calculated members and share them between users</li>
     * <li>the cube descriptions</li>
     * <li>entitlements</li>
     * </ul>
     * @return the content service
     */
    @Override
    @Bean
    public IContentService contentService() {
        Configuration conf  = ContentServerUtil.loadConfiguration("hibernate.properties");
        return new AuditableHibernateContentService(conf);
    }

}
