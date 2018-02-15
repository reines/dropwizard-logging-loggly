package com.jamierf.dropwizard.logging.loggly;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Layout;
import ch.qos.logback.ext.loggly.LogglyBatchAppender;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.base.Optional;
import com.google.common.net.HostAndPort;
import io.dropwizard.logging.AbstractAppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;
import net.logstash.logback.composite.JsonProviders;
import net.logstash.logback.composite.accessevent.AccessEventFormattedTimestampJsonProvider;
import net.logstash.logback.composite.accessevent.AccessEventNestedJsonProvider;
import net.logstash.logback.composite.accessevent.AccessMessageJsonProvider;
import net.logstash.logback.composite.accessevent.ContentLengthJsonProvider;
import net.logstash.logback.composite.accessevent.ElapsedTimeJsonProvider;
import net.logstash.logback.composite.accessevent.MethodJsonProvider;
import net.logstash.logback.composite.accessevent.ProtocolJsonProvider;
import net.logstash.logback.composite.accessevent.RemoteHostJsonProvider;
import net.logstash.logback.composite.accessevent.RemoteUserJsonProvider;
import net.logstash.logback.composite.accessevent.RequestHeadersJsonProvider;
import net.logstash.logback.composite.accessevent.RequestedUrlJsonProvider;
import net.logstash.logback.composite.accessevent.ResponseHeadersJsonProvider;
import net.logstash.logback.composite.accessevent.StatusCodeJsonProvider;
import net.logstash.logback.layout.AccessEventCompositeJsonLayout;
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
 * @see AbstractAppenderFactory
 */
@JsonTypeName("loggly-request")
public class LogglyRequestAppenderFactory extends AbstractAppenderFactory<IAccessEvent> {

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

    protected Layout<IAccessEvent> buildJsonLayout(LoggerContext context, LayoutFactory<IAccessEvent> layoutFactory) {
        AccessEventCompositeJsonLayout formatter = new AccessEventCompositeJsonLayout();
        formatter.setContext(context);
        JsonProviders<IAccessEvent> rootProviders = new JsonProviders<>();
        JsonProviders<IAccessEvent> requestProviders = new JsonProviders<>();
        JsonProviders<IAccessEvent> responseProviders = new JsonProviders<>();

        AccessEventNestedJsonProvider requestProvider = new AccessEventNestedJsonProvider();
        requestProvider.setFieldName("request");
        requestProvider.setProviders(requestProviders);
        rootProviders.addProvider(requestProvider);

        AccessEventNestedJsonProvider responseProvider = new AccessEventNestedJsonProvider();
        responseProvider.setFieldName("response");
        responseProvider.setProviders(responseProviders);
        rootProviders.addProvider(responseProvider);

        AccessEventFormattedTimestampJsonProvider timestampJsonProvider = new AccessEventFormattedTimestampJsonProvider();
        timestampJsonProvider.setFieldName("timestamp");
        rootProviders.addProvider(timestampJsonProvider);

        AccessMessageJsonProvider accessMessageJsonProvider = new AccessMessageJsonProvider();
        accessMessageJsonProvider.setFieldName("message");
        rootProviders.addProvider(accessMessageJsonProvider);

        ProtocolJsonProvider protocolJsonProvider = new ProtocolJsonProvider();
        protocolJsonProvider.setFieldName("protocol");
        rootProviders.addProvider(protocolJsonProvider);

        MethodJsonProvider methodJsonProvider = new MethodJsonProvider();
        methodJsonProvider.setFieldName("method");
        requestProviders.addProvider(methodJsonProvider);

        RequestedUrlJsonProvider requestedUrlJsonProvider = new RequestedUrlJsonProvider();
        requestedUrlJsonProvider.setFieldName("url");
        requestProviders.addProvider(requestedUrlJsonProvider);

        RemoteHostJsonProvider remoteHostJsonProvider = new RemoteHostJsonProvider();
        remoteHostJsonProvider.setFieldName("remoteHost");
        requestProviders.addProvider(remoteHostJsonProvider);

        RemoteUserJsonProvider remoteUserJsonProvider = new RemoteUserJsonProvider();
        remoteUserJsonProvider.setFieldName("remoteUser");
        requestProviders.addProvider(remoteUserJsonProvider);

        RequestHeadersJsonProvider requestHeadersJsonProvider = new RequestHeadersJsonProvider();
        requestHeadersJsonProvider.setFieldName("headers");
        requestProviders.addProvider(requestHeadersJsonProvider);

        StatusCodeJsonProvider statusCodeJsonProvider = new StatusCodeJsonProvider();
        statusCodeJsonProvider.setFieldName("status");
        responseProviders.addProvider(statusCodeJsonProvider);

        ContentLengthJsonProvider contentLengthJsonProvider = new ContentLengthJsonProvider();
        contentLengthJsonProvider.setFieldName("contentLength");
        responseProviders.addProvider(contentLengthJsonProvider);

        ElapsedTimeJsonProvider elapsedTimeJsonProvider = new ElapsedTimeJsonProvider();
        elapsedTimeJsonProvider.setFieldName("responseTime");
        responseProviders.addProvider(elapsedTimeJsonProvider);

        ResponseHeadersJsonProvider responseHeadersJsonProvider = new ResponseHeadersJsonProvider();
        responseHeadersJsonProvider.setFieldName("headers");
        responseProviders.addProvider(responseHeadersJsonProvider);

        formatter.setProviders(rootProviders);
        formatter.start();
        return formatter;
    }

    @Override
    public Appender<IAccessEvent> build(LoggerContext context, String applicationName, LayoutFactory<IAccessEvent> layoutFactory,
                          LevelFilterFactory<IAccessEvent> levelFilterFactory, AsyncAppenderFactory<IAccessEvent> asyncAppenderFactory) {
        final LogglyBatchAppender<IAccessEvent> appender = new LogglyBatchAppender<>();

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
