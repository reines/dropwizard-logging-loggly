package com.jamierf.dropwizard.logging.loggly;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.contrib.jackson.JacksonJsonFormatter;
import ch.qos.logback.contrib.json.classic.JsonLayout;
import ch.qos.logback.core.Appender;
import ch.qos.logback.ext.loggly.LogglyBatchAppender;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.base.Optional;
import com.google.common.net.HostAndPort;
import io.dropwizard.logging.AbstractAppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;

import javax.validation.constraints.NotNull;

/**
 * <p>An {@link io.dropwizard.logging.AppenderFactory} implementation which provides an appender that writes events to Loggly.</p>
 * <b>Configuration Parameters:</b>
 * <table summary="Configuration">
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
 *         <td>{@code tag}</td>
 *         <td>the application name</td>
 *         <td>The Loggly tag.</td>
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
public class LogglyAppenderFactory extends AbstractAppenderFactory<ILoggingEvent> {

    private static final String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String ENDPOINT_URL_TEMPLATE = "https://%s/bulk/%s/tag/%s";

    @NotNull
    private HostAndPort server = HostAndPort.fromString("logs-01.loggly.com");

    @NotEmpty
    private String token;

    @NotNull
    @UnwrapValidatedValue(false)
    private Optional<String> tag = Optional.absent();

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

    @JsonProperty
    public Optional<String> getTag() {
        return tag;
    }

    @JsonProperty
    public void setTag(final Optional<String> tag) {
        this.tag = tag;
    }

    protected JsonLayout buildJsonLayout(LoggerContext context, LayoutFactory<ILoggingEvent> layoutFactory) {
        JsonLayout formatter = new JsonLayout();
        formatter.setJsonFormatter(new JacksonJsonFormatter());
        formatter.setAppendLineSeparator(true);
        formatter.setContext(context);
        formatter.setTimestampFormat(ISO_8601_FORMAT);  //as per https://www.loggly.com/docs/automated-parsing/#json
        formatter.setTimestampFormatTimezoneId("UTC");
        formatter.start();
        return formatter;
    }

    @Override
    public Appender<ILoggingEvent> build(LoggerContext context, String applicationName, LayoutFactory<ILoggingEvent> layoutFactory,
                          LevelFilterFactory<ILoggingEvent> levelFilterFactory, AsyncAppenderFactory<ILoggingEvent> asyncAppenderFactory) {
        final LogglyBatchAppender<ILoggingEvent> appender = new LogglyBatchAppender<>();

        final String tagName = tag.or(applicationName);

        appender.setName("loggly-appender");
        appender.setContext(context);
        appender.setEndpointUrl(String.format(ENDPOINT_URL_TEMPLATE, server, token, tagName));
        appender.setLayout(buildJsonLayout(context, layoutFactory));
        appender.addFilter(levelFilterFactory.build(threshold));
        appender.start();

        return wrapAsync(appender, asyncAppenderFactory);
    }
}
