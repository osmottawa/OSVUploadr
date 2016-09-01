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
    
    public void SetPage(String Page){
        _page = Page;
    }
    public void SetCookies(String Cookies){
        _cookies = Cookies;
    }
    public String GetPage(){
        return _page;
    }
    public String GetCookies(){
        return _cookies;
    }
}
