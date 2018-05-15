/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.osmcanada.osvuploadr;

import ca.osmcanada.osvuploadr.UploadThread.UploadResult;

/**
 *
 * @author Jamie Nadeau
 */
public class UploadPhoto extends Thread{
    UploadResult result;
    String _token;
    int _photonum;
    String _photopath;

    public void setUploadResult(UploadResult callback)
    {
        this.result = callback;
    }
    
    public UploadPhoto(String token,int Photonumber,String PhotoPath)
    {
        _token = token;
        _photonum = Photonumber;
        _photopath = PhotoPath;
    }
    
    @Override
    public void run() {    
        //TODO: Get image information
        //TODO: Upload photo
        
        //Update photo upload status
        result.setResult(_photonum);
    }
}
