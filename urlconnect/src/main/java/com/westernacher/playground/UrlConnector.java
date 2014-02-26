package com.westernacher.playground;

import org.apache.commons.io.IOUtils;

import javax.net.ssl.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static java.lang.String.format;
import static java.lang.System.err;
import static java.lang.System.out;

public class UrlConnector {
    private TrustManager[] getTrustManagers() {
        return new TrustManager[] {
                new X509TrustManager() {
                    private int certCount = 0;
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }
                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                        out.println(format("SSL message: %s", s));
                        for (final X509Certificate certificate: x509Certificates) {
                            final String filename = format("/tmp/cert%02d.cer", certCount ++);
                            try {
                                IOUtils.write(certificate.getEncoded(), new FileOutputStream(filename));
                                out.println(format("written cert of %s to file %s",
                                        certificate.getSubjectDN().toString(), filename));
                            } catch (IOException e) {
                                throw new RuntimeException(format("failed to write file %s", filename), e);
                            }
                        }
                    }
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }
        };
    }

    private HostnameVerifier getHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                out.println(format("SSL hostname: %s", s));
                return true;
            }
        };
    }

    private URLConnection getUrlConnection(String arg) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getInstance("SSL");
        context.init(null, getTrustManagers(), new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(getHostnameVerifier());
        return new URL(arg).openConnection();
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        if (args.length != 1) {
            err.println("need 1 argument");
            System.exit(1);
        }
        final URLConnection connection = new UrlConnector().getUrlConnection(args[0]);
        out.println("connected");
        IOUtils.copy(connection.getInputStream(), out);
        out.println("done");
    }
}
