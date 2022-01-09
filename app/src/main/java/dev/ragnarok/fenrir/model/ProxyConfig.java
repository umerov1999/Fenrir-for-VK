package dev.ragnarok.fenrir.model;

import com.google.gson.annotations.SerializedName;

import dev.ragnarok.fenrir.api.model.Identificable;


public class ProxyConfig implements Identificable {

    @SerializedName("id")
    private int id;

    @SerializedName("address")
    private String address;

    @SerializedName("port")
    private int port;

    @SerializedName("authEnabled")
    private boolean authEnabled;

    @SerializedName("user")
    private String user;

    @SerializedName("pass")
    private String pass;

    public ProxyConfig set(int id, String address, int port) {
        this.id = id;
        this.address = address;
        this.port = port;
        return this;
    }

    public ProxyConfig setAuth(String user, String pass) {
        authEnabled = true;
        this.user = user;
        this.pass = pass;
        return this;
    }

    public boolean isAuthEnabled() {
        return authEnabled;
    }

    public String getPass() {
        return pass;
    }

    public String getUser() {
        return user;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProxyConfig config = (ProxyConfig) o;
        return id == config.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}