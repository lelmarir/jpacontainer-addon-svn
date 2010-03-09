/*
 * JPAContainer
 * Copyright (C) 2010 Oy IT Mill Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vaadin.addon.jpacontainer.demo.servlet;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.AbstractApplicationServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Custom servlet that fetches the application instance from the Spring
 * application context. It takes one servlet configuration parameter, <code>applicationBean</code>,
 * which should contain the name of the Vaadin application prototype bean defined in the
 * web application context.
 * <p>
 * This class is based on the <a href="http://dev.vaadin.com/browser/incubator/SpringApplication">SpringApplication example</a>
 * by Petri Hakala.
 *
 * @see WebApplicationContext
 * @author Petter Holmström (IT Mill)
 * @since 1.0
 */
public class SpringApplicationServlet extends AbstractApplicationServlet {

    protected final Log logger = LogFactory.getLog(getClass());
    private WebApplicationContext applicationContext;
    private Class<? extends Application> applicationClass;
    private String applicationBean;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        /*
         * Look up the name of the Vaadin application prototype bean.
         */
        applicationBean = servletConfig.getInitParameter("applicationBean");
        if (applicationBean == null) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "ApplicationBean not specified in servlet parameters");
            }
            throw new ServletException(
                    "ApplicationBean not specified in servlet parameters");
        }

        if (logger.isInfoEnabled()) {
            logger.info("Found Vaadin ApplicationBean [" + applicationBean + "]");
        }
        /*
         * Fetch the Spring web application context
         */
        applicationContext = WebApplicationContextUtils.getWebApplicationContext(
                servletConfig.getServletContext());

        if (!applicationContext.isPrototype(applicationBean)) {
            if (logger.isWarnEnabled()) {
                logger.warn(
                        "ApplicationBean not configured as a prototype");
            }
        }

        applicationClass = (Class<? extends Application>) applicationContext.
                getType(applicationBean);
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Vaadin application class is [" + applicationClass + "]");
        }
    }

    @Override
    protected Class<? extends Application> getApplicationClass() throws
            ClassNotFoundException {
        return applicationClass;
    }

    @Override
    protected Application getNewApplication(HttpServletRequest request) {
        /*
         * As the application bean should be defined as a prototype,
         * this call should always return a new application instance.
         */
        return (Application) applicationContext.getBean(applicationBean);
    }
}