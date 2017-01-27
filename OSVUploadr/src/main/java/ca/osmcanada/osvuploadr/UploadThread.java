/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.osmcanada.osvuploadr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jamie Nadeau
 */
public class UploadThread extends Thread{
    String _dir;
    byte[] UploadStatus;
    //Store upload status of dir
    final static byte first = (byte)-0x80; // 10000000
    final static byte second =(byte)0x40; // 01000000
    final static byte third = (byte)0x20; // 00100000
    final static byte fourth =(byte)0x10; // 00010000
    final static byte fifth = (byte)0x8; // 00001000
    final static byte sixth = (byte)0x4; // 00000100
    final static byte seventh=(byte)0x2; // 00000010
    final static byte eighth =(byte)0x1; // 00000001
    
    
    
    public void setDirectory(String Directory){
        _dir=Directory;
        File dir_photos = new File(_dir);
        //filter only .jpgs
        FilenameFilter fileNameFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
               if (name.lastIndexOf('.') > 0) {
                    int lastIndex = name.lastIndexOf('.');
                    String str = name.substring(lastIndex);
                    if (str.toLowerCase().equals(".jpg")) {
                        return true;
                    }
                }
                return false;
            }
        };
        int nbfiles = dir_photos.listFiles(fileNameFilter).length;
        int pages = nbfiles / 8;
        int rest = nbfiles % 8;
        if(nbfiles>0){pages++;} //increase if there are leftovers
        UploadStatus = new byte[pages];
        
        if(Files.exists(Paths.get(Directory + File.separator + "count_file.txt"))){
            UploadProgress(Directory);
        }
    }
    
    //for testing
    public void initUpload(int size){
        UploadStatus = new byte[size]; 
    }
    
    public void setUploaded(boolean Uploaded,int PhotoNumber){
        setValue(PhotoNumber,Uploaded);
    }
    
    public boolean getUploaded(int PhotoNumber){
        int idx = PhotoNumber / 8;
        int rest = PhotoNumber % 8;
        //Skip photo number 0 and put the 8th photo in previous byte array.
        if(PhotoNumber % 8 == 0){
            idx-=1;
        }  
        //Test if bit is set or not
        switch(rest){
            case 0:
                return ((UploadStatus[idx] & eighth) == eighth);
            case 1:
                return ((UploadStatus[idx] & first) == first);
            case 2:
                return ((UploadStatus[idx] & second) == second);
            case 3:
                return ((UploadStatus[idx] & third) == third);
            case 4:
                return ((UploadStatus[idx] & fourth) == fourth);
            case 5:
                return ((UploadStatus[idx] & fifth) == fifth);
            case 6:
                return ((UploadStatus[idx] & sixth) == sixth);
            case 7:
                return ((UploadStatus[idx] & seventh) == seventh);
        }
        return false;        
    }
    
    private void setValue(int pos,boolean Uploaded){
        int idx = pos / 8;
        int rest = pos % 8;
        //Skip photo number 0 and put the 8th photo in previous byte array.
        if(pos % 8 == 0){
            idx-=1;
        }        
        
        switch(rest){
            case 0:
                if(Uploaded)
                { 

                    UploadStatus[idx]=(byte)(UploadStatus[idx]|eighth);
                }
                else{
                    UploadStatus[idx]=(byte)(UploadStatus[idx] & ~eighth);
                }
                break;
            case 1:
                if(Uploaded)
                { 

                    UploadStatus[idx]=(byte)(UploadStatus[idx]|first);
                }
                else{
                    UploadStatus[idx]=(byte)(UploadStatus[idx] & ~first);
                }
                break;
            case 2:
                if(Uploaded)
                { 

                    UploadStatus[idx]=(byte)(UploadStatus[idx]|second);
                }
                else{
                    UploadStatus[idx]=(byte)(UploadStatus[idx] & ~second);
                }
                break;
            case 3:
                if(Uploaded)
                { 

                    UploadStatus[idx]=(byte)(UploadStatus[idx]|third);
                }
                else{
                    UploadStatus[idx]=(byte)(UploadStatus[idx] & ~third);
                }
                break;
            case 4:
                if(Uploaded)
                { 

                    UploadStatus[idx]=(byte)(UploadStatus[idx]|fourth);
                }
                else{
                    UploadStatus[idx]=(byte)(UploadStatus[idx] & ~fourth);
                }
                break;
            case 5:
                if(Uploaded)
                { 

                    UploadStatus[idx]=(byte)(UploadStatus[idx]|fifth);
                }
                else{
                    UploadStatus[idx]=(byte)(UploadStatus[idx] & ~fifth);
                }
                break;
            case 6:
                if(Uploaded)
                { 

                    UploadStatus[idx]=(byte)(UploadStatus[idx]|sixth);
                }
                else{
                    UploadStatus[idx]=(byte)(UploadStatus[idx] & ~sixth);
                }
                break;
            case 7:
                if(Uploaded)
                { 

                    UploadStatus[idx]=(byte)(UploadStatus[idx]|seventh);
                }
                else{
                    UploadStatus[idx]=(byte)(UploadStatus[idx] & ~seventh);
                }
                break;
        }
    }
    
    private void UploadProgress(String Dir){
        if(!Files.exists(Paths.get(Dir + File.separator + "count_file.txt"))){
            return;
        }
        
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(Dir + File.separator + "count_file.txt"),"UTF-8"));
            
            String line;
            while((line = br.readLine())!=null){
               line=line.trim();
               if(!line.isEmpty()){
                   if(isInteger(line,10)){
                       setUploaded(true,Integer.parseInt(line));                       
                   }
               } 
            }
            br.close();
            
        }
        catch(Exception e){
            Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, e.getMessage());
        }
        
    }
    private static boolean isInteger(String s, int radix) {
    if(s.isEmpty()) return false;
    for(int i = 0; i < s.length(); i++) {
        if(i == 0 && s.charAt(i) == '-') {
            if(s.length() == 1) return false;
            else continue;
        }
        if(Character.digit(s.charAt(i),radix) < 0) return false;
    }
    return true;
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
