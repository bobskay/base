package wang.wangy.utils.http;

import lombok.SneakyThrows;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class SSLContextFactory {

    public static SSLContext create(String keyStorePath, String keyStorepass){
        if (keyStorePath!=null){
            return custom(keyStorePath,keyStorepass);
        }else {
            return createIgnoreVerifySSL();
        }
    }

    /**
     * 设置信任自签名证书
     *
     * @param keyStorePath 密钥库路径
     * @param keyStorepass 密钥库密码
     * @return
     */
    public static SSLContext custom(String keyStorePath, String keyStorepass) {
        try {
            return  SSLContexts.custom()
                    .loadTrustMaterial(new File(keyStorePath), keyStorepass.toCharArray(),
                            new TrustSelfSignedStrategy())
                    .build();
        } catch (Exception e) {
            throw  new RuntimeException("获取证书出错");
        }
    }

    public static  SSLContext custome(InputStream inputStream,String password){
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(inputStream, password.toCharArray());

            // 相信自己的CA和所有自签名的证书
            return SSLContexts.custom().loadTrustMaterial(trustStore, new TrustSelfSignedStrategy()).build();
        } catch (Exception ex) {
            throw new RuntimeException("获取ssl证书出错", ex);
        }finally {
            if(inputStream!=null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 绕过验证
     *
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    @SneakyThrows
    public static SSLContext createIgnoreVerifySSL() {
        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }
            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        SSLContext sc = SSLContext.getInstance("SSLv3");
        sc.init(null, new TrustManager[]{trustManager}, null);
        return sc;
    }
}
