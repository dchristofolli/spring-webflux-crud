package com.dchristofolli.webfluxessentials.exception;

import io.netty.util.internal.StringUtil;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@Order(-2)
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalExceptionHandler(ErrorAttributes errorAttributes, WebProperties.Resources resources,
                                  ApplicationContext applicationContext, ServerCodecConfigurer configurer) {
        super(errorAttributes, resources, applicationContext);
        this.setMessageWriters(configurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::formatErrorResponse);
    }

    private Mono<ServerResponse> formatErrorResponse(ServerRequest request) {
        var query = request.uri().getQuery();
        var errorAttributeOptions = isTraceEnabled(query) ?
            ErrorAttributeOptions.of(ErrorAttributeOptions.Include.STACK_TRACE) :
            ErrorAttributeOptions.defaults();
        var errorAttributes = getErrorAttributes(request, errorAttributeOptions);
        var status = (int) Optional.ofNullable(errorAttributes.get("status")).orElse(500);
        return ServerResponse
            .status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(errorAttributes));
    }

    private boolean isTraceEnabled(String query) {
        return !StringUtil.isNullOrEmpty(query) && query.contains("trace=truce");
    }
}
