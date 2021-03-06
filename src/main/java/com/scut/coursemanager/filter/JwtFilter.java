package com.scut.coursemanager.filter;/*

 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scut.coursemanager.Exception.ErrorResponse;
import com.scut.coursemanager.Service.Impl.UserDetailsServiceImpl;
import com.scut.coursemanager.utility.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    @Resource
    private UserDetailsServiceImpl userDetailsService;

    @Resource
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        try {
            String authHeader = httpServletRequest.getHeader("Authorization");
            String username=null;
            String jwt=null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
                username=jwtUtil.extractSubject(jwt);
            }
            if(username!=null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                // 验证身份成功，则把用户上下文保存在当前请求（这里没看懂？？这里validateToken没起作用）
                if (jwtUtil.validateToken(jwt, username)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }
            filterChain.doFilter(httpServletRequest,httpServletResponse);
        }
        catch (ExpiredJwtException e){
            // token过期
            log.info("JWT过期 {}",e.getMessage());
            dealWithError(httpServletResponse, "JWT过期，请重新认证");
        }catch (SignatureException e){
            // token无效
            log.info("JWT无效 {}",e.getMessage());
            dealWithError(httpServletResponse, "JWT错误，请输入正确的token");
        }

    }

    /**
     * 返回filter错误
     * @param httpServletResponse response
     * @param message 信息
     */
    private static void dealWithError(HttpServletResponse httpServletResponse, String message) throws IOException {
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON.toString());
        ServletOutputStream outputStream = httpServletResponse.getOutputStream();

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message(message)
                .build();

        ObjectMapper build = Jackson2ObjectMapperBuilder.json().build();
        build.writeValue(outputStream, response);
    }
}
