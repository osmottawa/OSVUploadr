/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.osmcanada.osvuploadr;

import ca.osmcanada.osvuploadr.UploadThread;

/**
 *
 * @author Jamie Nadeau
 */
public class UploadManager extends Thread{
    Boolean _haswork=false;
    int _iSpot=0;
    UploadThread _queue[];
    
    
    public synchronized Boolean hasWork() throws InterruptedException {
        if(_queue != null)
        {
            if(_iSpot == _queue.length-1 && !_queue[_iSpot].isAlive()){
                _haswork=false;
            }
        }
        return _haswork;
    }
    
    public UploadManager(String[] Directories){
        if(Directories.length>0){
            _queue= new UploadThread[Directories.length];
            
            //Initialize directories
            for(int i=Directories.length-1;i>=0;i--){
                _queue[i]=new UploadThread();
                _queue[i].setDirectory(Directories[i]);
            }
            
            _haswork=true;
        }
        
    }
    
    @Override
    public void run() {
        try {
            while (hasWork()) {
                _queue[_iSpot].start();
                _queue[_iSpot].join();
                _iSpot++;
                sleep(500);
            }
        } catch (InterruptedException e) {
        }
    }   
}
