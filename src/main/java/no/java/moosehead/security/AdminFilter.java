package no.java.moosehead.security;

import no.java.moosehead.web.Configuration;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class AdminFilter implements Filter {

    private static final String APP_AUTH = "applicationcredential=<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> \n" +
            " <applicationcredential>\n" +
            "    <params>\n" +
            "        <applicationID>99</applicationID>\n" +
            "        <applicationSecret>33879936R6Jr47D4Hj5R6p9qT</applicationSecret>\n" +
            "    </params> \n" +
            "</applicationcredential>\n";

    private SecurityHandler securityHandler;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.securityHandler = new SecurityHandler();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!Configuration.secureAdmin()) {
            chain.doFilter(request,response);
            return;
        }
        String userticket = request.getParameter("userticket");
        if (userticket == null || userticket.length() < 3) {
            ((HttpServletResponse) response).sendRedirect("http://localhost:9997/sso/login?redirectURI=http://localhost:8088/admin/");
            return;
        }
        String usertoken = readUserToken(userticket);
        response.getWriter().append(usertoken);



        System.out.println("fhf");
    }

    private String readUserToken(String userticket) throws IOException {
        String apptokenXml = readAppToken();
        String apptokenid = readAppTokenFromXml(apptokenXml);
        String path = "http://localhost:9998/tokenservice/user/" + apptokenid + "/get_usertoken_by_userticket";
        StringBuilder payload = new StringBuilder();
        payload.append("userticket=");
        payload.append(userticket);
        payload.append("&apptoken=");
        payload.append(apptokenXml);

        URLConnection conn = new URL(path).openConnection();
        conn.setDoOutput(true);
        try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(),"utf-8"))) {
            printWriter.append(payload.toString());
        }
        return toString(conn.getInputStream());
    }

    private static String readAppTokenFromXml(String apptokenXml) {
        int ind = apptokenXml.indexOf("<applicationtokenID>");
        int endind = apptokenXml.indexOf("</applicationtokenID>");
        String token = apptokenXml.substring(ind + "<applicationtokenID>".length(),endind);
        return token;
    }

    private String readAppToken() throws IOException {
        URLConnection conn = new URL("http://localhost:9998/tokenservice/logon").openConnection();
        conn.setDoOutput(true);
        try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(),"utf-8"))) {
            printWriter.append(APP_AUTH);
        }
        return toString(conn.getInputStream());
    }


    private static String toString(InputStream inputStream) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"))) {
            StringBuilder result = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                result.append((char)c);
            }
            return result.toString();
        }
    }

    @Override
    public void destroy() {
    }

}
