/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.osmcanada.osvuploadr;

import java.util.Date;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.time.Duration;
import ca.osmcanada.osvuploadr.Utils.*;
import ca.osmcanada.osvuploadr.struct.ImageProperties;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 *
 * @author Jamie Nadeau
 */
public class FolderCleaner {
    String _folder;
    int max_speed=300; //Improbable speed to reach (km/h)
    int min_duplicates = 3; // Minimum of photos in "same location" to be considered duplicates
    int dist_threshold = 4; // Minimum distance a photo should move to not be considered a duplicate (in meters)
    int radius_threshold = 20; // Minimum turn radius a photo should move to not be considered a duplicate (degrees)
    String duplicate_folder="duplicates";
    JFMain info=null;
    Locale l;
    ResourceBundle r;
    
    private double calc_distance(double lon1, double lat1, double lon2, double lat2){
        //haversine formula
        lon1=Math.toRadians(lon1);
        lon2=Math.toRadians(lon2);
        lat1=Math.toRadians(lat1);
        lat2=Math.toRadians(lat2);
        double dlon = lon2-lon1;
        double dlat = lat2-lat1;
        double a = Math.pow(Math.sin(dlat/2),2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon/2),2);
        double c = 2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
        double r = 6371000; // Average Earth Radius in meters
        return r*c;
    }
    
    private void do_science(){
        double prev_lat=-512; //Something impossible to get to
        double prev_long=-512;//Something impossible to get to
        Date last_Date = null;
        double prev_compass=-512;//Something impossible to get to
        int duplicate_cnt =0;
        boolean is_first=true;
        double dist_diff;
        double compass_diff;
        long ts_diff;
        double km_h = 0;
        File dir_photos = new File(_folder);
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
        File[] file_list = dir_photos.listFiles(fileNameFilter);
        HashMap<File, Long> file_timecache = new HashMap<File,Long>();
        for(File f:file_list){
            file_timecache.put(f, Helper.getFileTime(f));
        }
        //sort by modified time
        Arrays.sort(file_list, new Comparator<File>(){
        public int compare(File f1, File f2)
        {
            return Long.valueOf(file_timecache.get(f1)).compareTo(file_timecache.get(f2));
        }});
        
        for(File f:file_list){
            if(info!=null){
                info.setInfoBoxText(f.getPath());
            }
            ImageProperties imp = Helper.getImageProperties(f);
            if(is_first){
                is_first=false;
                prev_lat=imp.getLatitude();
                prev_long=imp.getLongitude();
                last_Date=new Date(file_timecache.get(f));
                prev_compass = imp.getCompass();
                continue;
            }
            
            dist_diff = calc_distance(prev_long,prev_lat, imp.getLongitude(),imp.getLatitude());
            compass_diff = Math.abs(prev_compass - imp.getCompass());
            Logger.getLogger(JPMain.class.getName()).log(Level.INFO, "Moved:" + String.valueOf(dist_diff));
            Logger.getLogger(JPMain.class.getName()).log(Level.INFO, "Rotated:" + String.valueOf(compass_diff));
            ts_diff=Duration.between(last_Date.toInstant(), Instant.ofEpochMilli(file_timecache.get(f))).getSeconds();
            if(ts_diff == 0)
            {
                //Is a duplicate or a copy
                continue;
            }
            km_h = (dist_diff/ts_diff) * 3.6; // Calculcate m/s and convert to km/h
            Logger.getLogger(JPMain.class.getName()).log(Level.INFO, "Moved:" + String.valueOf(km_h) + " km/h");
            if(km_h > max_speed){
                //TODO: move files to a new folder and split the sequence
            }
            if(dist_diff < dist_threshold && compass_diff < radius_threshold)
            {
                duplicate_cnt +=1;
            }
            else{
                duplicate_cnt=0;
                prev_lat=imp.getLatitude();
                prev_long=imp.getLongitude();
                last_Date=new Date(file_timecache.get(f));
                prev_compass= imp.getCompass();
            }
            
            if(duplicate_cnt >= min_duplicates)
            {
                //File is a duplicate, move to duplicate folder
                Logger.getLogger(JPMain.class.getName()).log(Level.INFO, "Have to move:" + imp.getFilePath());
                File newLoc = new File(_folder + File.separator +duplicate_folder+ File.separator +f.getName());
                try{
                //Files.move(f.toPath(), newLoc.toPath(),StandardCopyOption.REPLACE_EXISTING);
                move(f.toPath(),newLoc.toPath());
                }
                catch(Exception ex){
                    Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, ex.toString(),ex);
                }
            }         
        }
        
    }
    private void move(Path from, Path to)
    {
        try{

    	   File afile =new File(from.toUri());

    	   if(afile.renameTo(new File(to.toUri()))){
    		System.out.println("File is moved successful!");
    	   }else{
    		System.out.println("File is failed to move!");
    	   }

    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    public void removeDuplicates(){
        File dup = new File(_folder + File.separator + duplicate_folder);
        if(!dup.exists())
        {
            try{
                Files.createDirectory(dup.toPath());
            }
            catch(Exception ex)
            {
                Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, ex.toString());
            }
        }
        do_science();
        if(info!=null){
            info.setInfoBoxText(new String(r.getString("done").getBytes(), StandardCharsets.UTF_8));           
        }
    }
    
    public void setInfoBox(JFMain frame){
        info=frame;
    }
    
    public void setLocale(Locale locale){
        l=locale;
        r=ResourceBundle.getBundle("Bundle",l);
    }
    
    public FolderCleaner(String Folder){
        _folder=Folder;
        l=Locale.getDefault();
        r=ResourceBundle.getBundle("Bundle",l);
    }
    
    
}
