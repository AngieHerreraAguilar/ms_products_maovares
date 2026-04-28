package com.maovares.ms_products.product.infraestructure.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

@Component
public class CertificateFilter implements Filter {

    @Value("${CLIENT_CERT_THUMBPRINT:}")
    private String expectedThumbprint;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientCertHeader = httpRequest.getHeader("X-ARR-ClientCert");

        if (clientCertHeader == null || clientCertHeader.isEmpty()) {
            httpResponse.setStatus(403);
            httpResponse.getWriter().write("Client Certificate Required");
            return;
        }

        try {
            byte[] certBytes = Base64.getDecoder().decode(clientCertHeader);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes));

            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(cert.getEncoded());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
            }
            String thumbprint = sb.toString();

            if (!thumbprint.equalsIgnoreCase(expectedThumbprint)) {
                httpResponse.setStatus(403);
                httpResponse.getWriter().write("Invalid Certificate");
                return;
            }

            chain.doFilter(request, response);

        } catch (Exception e) {
            httpResponse.setStatus(403);
            httpResponse.getWriter().write("Certificate validation failed");
        }
    }
}