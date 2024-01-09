public class MyApp {
    public static void main(String[] args) {
        System.setProperty("javax.net.ssl.trustStore", "server-apk/src/main/resources/truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "niklas");
        System.out.println("TrustStore properties set successfully");
    }
}