/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.osmcanada.osvuploadr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jamie Nadeau
 */
public class UploadThread extends Thread{
    String _dir;
    byte[] uploadStatus;
    //Store upload status of dir
    final static byte first = (byte)-0x80; // 10000000
    final static byte second =(byte)0x40; // 01000000
    final static byte third = (byte)0x20; // 00100000
    final static byte fourth =(byte)0x10; // 00010000
    final static byte fifth = (byte)0x8; // 00001000
    final static byte sixth = (byte)0x4; // 00000100
    final static byte seventh=(byte)0x2; // 00000010
    final static byte eighth =(byte)0x1; // 00000001
    
    
    /**
     * Sets directory that the thread needs to work from. This should be set before running the thread.
     * @param Directory Directory from which to work from
     */
    public void setDirectory(String Directory){
        _dir=Directory;
        File dir_photos = new File(_dir);
        //filter only .jpgs
        FilenameFilter fileNameFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
               if (name.lastIndexOf('.') > 0) {
                    int lastIndex = name.lastIndexOf('.');
                    String str = name.substring(lastIndex);
                    if (str.toLowerCase(Locale.ENGLISH).equals(".jpg")) {
                        return true;
                    }
                }
                return false;
            }
        };
        int nbfiles = dir_photos.listFiles(fileNameFilter).length;
        int pages = nbfiles / 8;
        int rest = nbfiles % 8;
        if(rest>0){pages++;} //increase if there are leftovers
        uploadStatus = new byte[pages];
        
        if(Files.exists(Paths.get(Directory + File.separator + "count_file.txt"))){
            UploadProgress(_dir);
        }
    }
    
    //for testing
    public void initUpload(int size){
        uploadStatus = new byte[size]; 
    }
    
    /**
     * Sets whether the photo has been uploaded or not
     * @param Uploaded Sets the status to uploaded or not. True: Uploaded, False: Not uploaded
     * @param PhotoNumber Non-zero based sequence number of the photo
     */
    public void setUploaded(boolean Uploaded,int PhotoNumber){
        setValue(PhotoNumber,Uploaded);
    }
    
    /**
     * Gets the upload status from the non-zero based sequence number
     * @param PhotoNumber Non-zero based sequence number
     * @return boolean stating if it was uploaded or not
     */
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
                return ((uploadStatus[idx] & eighth) == eighth);
            case 1:
                return ((uploadStatus[idx] & first) == first);
            case 2:
                return ((uploadStatus[idx] & second) == second);
            case 3:
                return ((uploadStatus[idx] & third) == third);
            case 4:
                return ((uploadStatus[idx] & fourth) == fourth);
            case 5:
                return ((uploadStatus[idx] & fifth) == fifth);
            case 6:
                return ((uploadStatus[idx] & sixth) == sixth);
            case 7:
                return ((uploadStatus[idx] & seventh) == seventh);
        }
        return false;        
    }
    
    /**
     * Helper method to switch bit on or off in the byte array
     * @param pos Non-zero sequence number of the bit we want to change
     * @param Uploaded boolean value that turns on or off the bit. True: 1 False: 0
     */
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

                    uploadStatus[idx]=(byte)(uploadStatus[idx]|eighth);
                }
                else{
                    uploadStatus[idx]=(byte)(uploadStatus[idx] & ~eighth);
                }
                break;
            case 1:
                if(Uploaded)
                { 

                    uploadStatus[idx]=(byte)(uploadStatus[idx]|first);
                }
                else{
                    uploadStatus[idx]=(byte)(uploadStatus[idx] & ~first);
                }
                break;
            case 2:
                if(Uploaded)
                { 

                    uploadStatus[idx]=(byte)(uploadStatus[idx]|second);
                }
                else{
                    uploadStatus[idx]=(byte)(uploadStatus[idx] & ~second);
                }
                break;
            case 3:
                if(Uploaded)
                { 

                    uploadStatus[idx]=(byte)(uploadStatus[idx]|third);
                }
                else{
                    uploadStatus[idx]=(byte)(uploadStatus[idx] & ~third);
                }
                break;
            case 4:
                if(Uploaded)
                { 

                    uploadStatus[idx]=(byte)(uploadStatus[idx]|fourth);
                }
                else{
                    uploadStatus[idx]=(byte)(uploadStatus[idx] & ~fourth);
                }
                break;
            case 5:
                if(Uploaded)
                { 

                    uploadStatus[idx]=(byte)(uploadStatus[idx]|fifth);
                }
                else{
                    uploadStatus[idx]=(byte)(uploadStatus[idx] & ~fifth);
                }
                break;
            case 6:
                if(Uploaded)
                { 

                    uploadStatus[idx]=(byte)(uploadStatus[idx]|sixth);
                }
                else{
                    uploadStatus[idx]=(byte)(uploadStatus[idx] & ~sixth);
                }
                break;
            case 7:
                if(Uploaded)
                { 

                    uploadStatus[idx]=(byte)(uploadStatus[idx]|seventh);
                }
                else{
                    uploadStatus[idx]=(byte)(uploadStatus[idx] & ~seventh);
                }
                break;
        }
    }
    
    /** 
     * Sets the upload progress for previously interrupted upload by consulting count file
     * @param Dir Directory from which it needs to read from
     */
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
    
    /** 
     * Writes to count file a photo number that has been uploaded.
     * 
     * @param PhotoNumber non-zero number of the photo in the sequence 
     */
    public synchronized void incrementCount(int PhotoNumber){
        File f_cnt = new File(_dir+ File.separator + "count_file.txt");
        try{
            if(!Files.exists(Paths.get(f_cnt.getAbsolutePath()))){
                f_cnt.createNewFile();
            }
            Writer w = Channels.newWriter(new FileOutputStream(f_cnt.getAbsoluteFile(), true).getChannel(), "UTF-8");
            w.append(String.valueOf(PhotoNumber)+"\n");
            w.flush();
            w.close();
        }
        catch(Exception e){
            Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, e.getMessage());
        }
    }
    
    /**
     * Checks if a string is an Integer or not
     * @param s String that you want to determine if it's an integer or not
     * @param radix what base the string is written in, for decimal: 10, for binary: 2, for hex: 16, etc
     * @return boolean whether it's an integer or not
     */
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
