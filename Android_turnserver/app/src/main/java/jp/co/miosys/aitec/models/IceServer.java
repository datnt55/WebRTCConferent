package jp.co.miosys.aitec.models;

import java.util.List;

/**
 * Created by Duc on 3/6/2018.
 */

public class IceServer {
    public List<String> urls;
    public String username;
    public String credential;

    public IceServer(List<String> urls, String username, String credential) {
        this.urls = urls;
        this.username = username;
        this.credential = credential;
    }

    public IceServer(List<String> urls) {
        this.urls = urls;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }
}
