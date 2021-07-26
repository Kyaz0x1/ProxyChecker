package net.kyaz0x1.proxychecker.models;

public class Proxy {

    private String host;
    private int port;

    public Proxy(String host, int port){
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return String.format("%s:%d", getHost(), getPort());
    }

}