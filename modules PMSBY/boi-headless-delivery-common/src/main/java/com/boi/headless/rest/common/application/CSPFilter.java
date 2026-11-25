/*
 * package com.boi.headless.rest.common.application; import
 * org.osgi.service.component.annotations.Component;
 * 
 * import javax.servlet.*; import javax.servlet.http.HttpServletRequest; import
 * javax.servlet.http.HttpServletResponse; import java.io.IOException; import
 * java.util.UUID;
 * 
 * @Component( immediate = true, property = { "dispatcher=REQUEST",
 * "dispatcher=FORWARD", "dispatcher=INCLUDE", "dispatcher=ERROR",
 * "url-pattern=/", "servlet-context-name=", "service.ranking:Integer=1000" },
 * service = Filter.class ) public class CSPFilter implements Filter {
 * 
 * @Override public void init(FilterConfig filterConfig) { // No init needed }
 * 
 * @Override public void doFilter( ServletRequest servletRequest,
 * ServletResponse servletResponse, FilterChain chain) throws IOException,
 * ServletException {
 * 
 * HttpServletRequest request = (HttpServletRequest) servletRequest;
 * HttpServletResponse response = (HttpServletResponse) servletResponse;
 * 
 * String existingCsp = response.getHeader("Content-Security-Policy");
 * 
 * // Generate a secure random nonce String nonce =
 * UUID.randomUUID().toString().replaceAll("-", "");
 * request.setAttribute("cspNonce", nonce);
 * 
 * String updatedCsp;
 * 
 * if (existingCsp != null && !existingCsp.isEmpty()) { // Remove unsafe-inline
 * updatedCsp = existingCsp.replace("'unsafe-inline'", "").trim();
 * 
 * // Inject nonce into script-src if (updatedCsp.contains("script-src")) {
 * updatedCsp = updatedCsp.replaceFirst("script-src([^;]*)",
 * "script-src$1 'nonce-" + nonce + "'"); } else { updatedCsp +=
 * "; script-src 'self' 'nonce-" + nonce + "'"; } } else { // Default CSP if
 * none exists updatedCsp = "script-src 'self' 'nonce-" + nonce +
 * "'; object-src 'none'; style-src 'self'"; }
 * 
 * // Overwrite the CSP header response.setHeader("Content-Security-Policy",
 * updatedCsp);
 * 
 * chain.doFilter(request, response); }
 * 
 * @Override public void destroy() { // No cleanup needed } }
 */