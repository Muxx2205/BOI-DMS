/*
 * package com.boi.headless.rest.common.listeners;
 * 
 * import java.util.Collections;
 * 
 * import javax.ws.rs.NotFoundException; import javax.ws.rs.core.MediaType;
 * import javax.ws.rs.core.Response; import javax.ws.rs.ext.ExceptionMapper;
 * import javax.ws.rs.ext.Provider;
 * 
 * @Provider public class NotFoundExceptionMapper implements
 * ExceptionMapper<NotFoundException> {
 * 
 * static { System.out.println("Class Loaded for NotFoundExceptionMapper"); }
 * 
 * @Override public Response toResponse(NotFoundException exception) { return
 * Response.status(Response.Status.NOT_FOUND)
 * .entity(Collections.singletonMap("error", "Not Found 1"))
 * .type(MediaType.APPLICATION_JSON) .build(); } }
 */