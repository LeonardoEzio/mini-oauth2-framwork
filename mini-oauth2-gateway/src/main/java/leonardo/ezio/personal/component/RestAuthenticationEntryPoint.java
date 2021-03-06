package leonardo.ezio.personal.component;

import leonardo.ezio.personal.api.CommonResult;
import cn.hutool.json.JSONUtil;
import leonardo.ezio.personal.api.ResultCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;

/**
 * 自定义返回结果：没有登录或token过期时
 * Created by macro on 2020/6/18.
 */
@Component
public class RestAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {
    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        String responseBody = "";
        String causeCode = e.getCause().getMessage();
        if (isResultCode(causeCode)){
            long resultCode = Long.valueOf(causeCode);
            ResultCode code = ResultCode.findByCode(resultCode);
            if (null != code){
                responseBody= JSONUtil.toJsonStr(CommonResult.failed(code));
            }
        } else {
            responseBody= JSONUtil.toJsonStr(CommonResult.failed(e.getMessage()));
        }

        DataBuffer buffer =  response.bufferFactory().wrap(responseBody.getBytes(Charset.forName("UTF-8")));
        return response.writeWith(Mono.just(buffer));
    }

    private boolean isResultCode(String message){
        if (StringUtils.isNotEmpty(message)){
            for (char c : message.toCharArray()) {
                if (!Character.isDigit(c)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
