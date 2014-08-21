package com.jamierf.dropwizard.logging.loggly;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Layout;
import ch.qos.logback.ext.loggly.LogglyAppender;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.net.HostAndPort;
import io.dropwizard.logging.AbstractAppenderFactory;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * An {@link io.dropwizard.logging.AppenderFactory} implementation which provides an appender that writes events to Loggly.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code type}</td>
 *         <td><b>REQUIRED</b></td>
 *         <td>The appender type. Must be {@code loggly}.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code threshold}</td>
 *         <td>{@code ALL}</td>
 *         <td>The lowest level of events to write to the server.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code server}</td>
 *         <td>{@code logs-01.loggly.com}</td>
 *         <td>The Loggly server.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code token}</td>
 *         <td><b>REQUIRED</b></td>
 *         <td>Your Loggly customer token.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code logFormat}</td>
 *         <td>the default format</td>
 *         <td>
 *             The Logback pattern with which events will be formatted. See
 *             <a href="http://logback.qos.ch/manual/layouts.html#conversionWord">the Logback documentation</a>
 *             for details.
 *         </td>
 *     </tr>
 * </table>
 *
 * @see io.dropwizard.logging.AbstractAppenderFactory
 */
@JsonTypeName("loggly")
public class LogglyAppenderFactory extends AbstractAppenderFactory {

    private static final String ENDPOINT_URL_TEMPLATE = "https://%s/inputs/%s/tag/%s";

    @NotNull
    private HostAndPort server = HostAndPort.fromString("logs-01.loggly.com");

    @NotEmpty
    private String token;

    @JsonProperty
    public HostAndPort getServer() {
        return server;
    }

    @JsonProperty
    public void setServer(final HostAndPort server) {
        this.server = server;
    }

    @JsonProperty
    public String getToken() {
        return token;
    }

    @JsonProperty
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public Appender<ILoggingEvent> build(LoggerContext context, String applicationName, Layout<ILoggingEvent> layout) {
        final LogglyAppender<ILoggingEvent> appender = new LogglyAppender<>();

        appender.setName("loggly-appender");
        appender.setContext(context);
        appender.setEndpointUrl(String.format(ENDPOINT_URL_TEMPLATE, server, token, applicationName));

        addThresholdFilter(appender, threshold);
        appender.start();

        return wrapAsync(appender);
    }
}
