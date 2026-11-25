/*
 * package com.boi.headless.rest.common.listeners;
 * 
 * import com.liferay.portal.kernel.servlet.ServletResponseUtil;
 * 
 * import java.io.IOException;
 * 
 * import javax.servlet.Filter; import javax.servlet.FilterChain; import
 * javax.servlet.FilterConfig; import javax.servlet.ServletException; import
 * javax.servlet.ServletRequest; import javax.servlet.ServletResponse; import
 * javax.servlet.http.HttpServletRequest; import
 * javax.servlet.http.HttpServletResponse;
 * 
 * import org.osgi.service.component.annotations.Component;
 * 
 * @Component( immediate = true, property = { "servlet-context-name=",
 * "url-pattern=/boi-headless-delivery-spa/v1.0/*" }, service = Filter.class )
 * public class NotFoundJsonFilter implements Filter { static {
 * System.out.println("Class Loaded for NotFoundJsonFilter"); }
 * 
 * @Override public void doFilter( ServletRequest request, ServletResponse
 * response, FilterChain chain) throws IOException, ServletException {
 * System.out.println("NotFoundJsonFilter doFilter"); HttpServletRequest
 * httpRequest = (HttpServletRequest) request; HttpServletResponse httpResponse
 * = (HttpServletResponse) response;
 * 
 * try { chain.doFilter(request, response);
 * 
 * // If response is 404 after processing, replace it if
 * (httpResponse.getStatus() == HttpServletResponse.SC_NOT_FOUND) {
 * httpResponse.reset();
 * httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
 * httpResponse.setContentType("application/json");
 * httpResponse.getWriter().write("{\"error\":\"Not Found\"}"); } } catch
 * (Exception e) { httpResponse.reset();
 * httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 * httpResponse.setContentType("application/json");
 * httpResponse.getWriter().write("{\"error\":\"Internal Server Error\"}"); } }
 * 
 * @Override public void init(FilterConfig filterConfig) throws ServletException
 * {
 * 
 * System.out.println("NotFoundJsonFilter init"); }
 * 
 * @Override public void destroy() {
 * System.out.println("NotFoundJsonFilter destroy");
 * 
 * } }
 */