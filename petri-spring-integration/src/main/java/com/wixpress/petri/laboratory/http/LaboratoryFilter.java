package com.wixpress.petri.laboratory.http;

import com.wixpress.petri.laboratory.UserInfo;
import com.wixpress.petri.laboratory.UserInfoStorage;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 10/6/14
 * Time: 3:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class LaboratoryFilter implements Filter {

    private final UserInfoStorage storage;

    public LaboratoryFilter(UserInfoStorage storage) {
        this.storage = storage;
    }

    public void destroy() {
    }

    private static class ByteArrayServletStream extends ServletOutputStream
    {

        ByteArrayOutputStream baos;

        ByteArrayServletStream(ByteArrayOutputStream baos)
        {
            this.baos = baos;
        }

        public void write(int param) throws IOException
        {
            baos.write(param);
        }
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintWriter pw = new PrintWriter(baos);

        final ServletOutputStream sos = new ByteArrayServletStream(baos);
        final HttpServletResponseWrapper response = new CachingHttpResponse(resp, sos, pw);
        chain.doFilter(req, response);
        final UserInfo ui = storage.read();

        // TODO: add specific unit tests that drive storing both anonymous and user experiments logs.
        // TODO: check that cookie age is 6 months and path is "/"
        Cookie cookie = new Cookie("_wixAB3", ui.anonymousExperimentsLog);

        response.addCookie(cookie);
        resp.getOutputStream().write(baos.toByteArray());
    }


    public void init(FilterConfig filterConfig) throws ServletException {
    }

    private static class CachingHttpResponse extends HttpServletResponseWrapper {
        private final ServletOutputStream sos;
        private final PrintWriter pw;

        public CachingHttpResponse(ServletResponse resp, ServletOutputStream sos, PrintWriter pw) {
            super((HttpServletResponse) resp);
            this.sos = sos;
            this.pw = pw;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return sos;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return pw;
        }
    }
}
