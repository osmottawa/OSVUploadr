/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.osmcanada.osvuploadr.Utils;

import ca.osmcanada.osvuploadr.JPMain;
import ca.osmcanada.osvuploadr.struct.ImageProperties;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import java.io.File;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jamie Nadeau
 */
public final class Helper {
    
    public static long getFileTime(File f){
        try{
            Metadata metadata = ImageMetadataReader.readMetadata(f);
            ExifSubIFDDirectory directory  = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            Date date  = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            return date.getTime();
        }
        catch(Exception ex)
        {}
        return 0;
    }
    
    public static ImageProperties getImageProperties(File f){
        try{
            Metadata metadata = ImageMetadataReader.readMetadata(f);
            GpsDirectory directory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        
            ImageProperties imp = new ImageProperties();
            imp.setLatitude(directory.getGeoLocation().getLatitude());
            imp.setLongitude(directory.getGeoLocation().getLongitude());
            imp.setCompass(-1.0);
            imp.setFilePath(f.getPath());
        
            if(directory.hasTagName(directory.TAG_IMG_DIRECTION))
            {
                try
                {
                    imp.setCompass(directory.getDouble(directory.TAG_IMG_DIRECTION));
                }
                catch(Exception ex)
                {}
            }
            if(directory.hasTagName(directory.TAG_TRACK) && imp.getCompass()==-1.0)
            {
                try
                {
                    imp.setCompass(directory.getDouble(directory.TAG_TRACK));
                }
                catch(Exception ex)
                {}
            }

            return imp;
        }catch(Exception ex)
        {
            Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
