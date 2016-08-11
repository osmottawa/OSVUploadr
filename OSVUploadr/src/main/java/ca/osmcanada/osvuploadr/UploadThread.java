/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.osmcanada.osvuploadr;

/**
 *
 * @author Jamie Nadeau
 */
public class UploadThread extends Thread{
    String _dir;
    
    
    public void setDirectory(String Directory){
        _dir=Directory;
    }
    
    
    @Override
    public void run() {
        try {                            
            sleep(1000);
            return;
        } catch (InterruptedException e) {
        }
    }   
    
}
