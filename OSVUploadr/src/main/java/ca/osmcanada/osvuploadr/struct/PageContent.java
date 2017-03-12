/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.osmcanada.osvuploadr.struct;

/**
 *
 * @author Jamie Nadeau
 */
public class PageContent {
    String _page;
    String _cookies;
    
    public void setPage(String Page){
        _page = Page;
    }
    public void setCookies(String Cookies){
        _cookies = Cookies;
    }
    public String getPage(){
        return _page;
    }
    public String getCookies(){
        return _cookies;
    }
}
