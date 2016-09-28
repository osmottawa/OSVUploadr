/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.osmcanada.osvuploadr.API;
import com.github.scribejava.core.builder.api.DefaultApi10a;
import com.github.scribejava.core.model.OAuth1RequestToken;
/**
 *
 * @author Jamie Nadeau
 */
public class OSMApi extends DefaultApi10a {
    private static final String AUTHORIZE_URL = "https://www.openstreetmap.org/oauth/authorize?oauth_token=%s";
    private static final String REQUEST_TOKEN_RESOURCE = "www.openstreetmap.org/oauth/request_token";
    private static final String ACCESS_TOKEN_RESOURCE = "www.openstreetmap.org/oauth/access_token";
    
    protected OSMApi() {
    }
    
    private static class InstanceHolder {
        private static final OSMApi INSTANCE = new OSMApi();
    }
    
    public static OSMApi instance() {
        return InstanceHolder.INSTANCE;
    }
    
    @Override
    public String getAccessTokenEndpoint() {
        return "https://" + ACCESS_TOKEN_RESOURCE;
    }
        @Override
    public String getRequestTokenEndpoint() {
        return "https://" + REQUEST_TOKEN_RESOURCE;
    }

    @Override
    public String getAuthorizationUrl(OAuth1RequestToken requestToken) {
        return String.format(AUTHORIZE_URL, requestToken.getToken());
    }
    
}
