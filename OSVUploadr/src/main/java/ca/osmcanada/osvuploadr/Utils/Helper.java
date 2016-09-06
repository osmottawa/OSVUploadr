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
import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

/**
 *
 * @author Jamie Nadeau
 */
public final class Helper {
    public static enum EnumOS {
        linux, macos, solaris, unknown, windows;

        public boolean isLinux() {

            return this == linux || this == solaris;
        }


        public boolean isMac() {

            return this == macos;
        }


        public boolean isWindows() {

            return this == windows;
        }
    }
    
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
    
    public static Boolean OpenBrowser(URI uri){
        try{
            boolean supportsBrowse = true;
            if(!Desktop.isDesktopSupported()){
                supportsBrowse = false;
                if(!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)){
                    supportsBrowse = false;
                }
            }
            
            if(supportsBrowse){
                Desktop.getDesktop().browse(uri);
            }
            else{
                EnumOS os = getOs();
                if (os.isLinux()) {
                    if (runCommand("kde-open", "%s", uri.toString())) return true;
                    if (runCommand("gnome-open", "%s", uri.toString())) return true;
                    if (runCommand("xdg-open", "%s", uri.toString())) return true;
                }

                if (os.isMac()) {
                    if (runCommand("open", "%s", uri.toString())) return true;
                }

                if (os.isWindows()) {
                    if (runCommand("explorer.exe", "%s", uri.toString())) return true;
                }
            }
        }
        catch(Exception ex){
            Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return false;
    }
    
    private static boolean runCommand(String command, String args, String file) {

        logOut("Trying to exec:\n   cmd = " + command + "\n   args = " + args + "\n   %s = " + file);

        String[] parts = prepareCommand(command, args, file);

        try {
            Process p = Runtime.getRuntime().exec(parts);
            if (p == null) return false;

            try {
                int retval = p.exitValue();
                if (retval == 0) {
                    logOut("Process ended immediately.");
                    return false;
                } else {
                    logOut("Process crashed.");
                    return false;
                }
            } catch (IllegalThreadStateException itse) {
                logOut("Process is running.");
                return true;
            }
        } catch (IOException e) {
            Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }


    private static String[] prepareCommand(String command, String args, String file) {

        List<String> parts = new ArrayList<String>();
        parts.add(command);

        if (args != null) {
            for (String s : args.split(" ")) {
                s = String.format(s, file); // put in the filename thing

                parts.add(s.trim());
            }
        }

        return parts.toArray(new String[parts.size()]);
    }
    
    public static EnumOS getOs() {

        String s = System.getProperty("os.name").toLowerCase();

        if (s.contains("win")) {
            return EnumOS.windows;
        }

        if (s.contains("mac")) {
            return EnumOS.macos;
        }

        if (s.contains("solaris")) {
            return EnumOS.solaris;
        }

        if (s.contains("sunos")) {
            return EnumOS.solaris;
        }

        if (s.contains("linux")) {
            return EnumOS.linux;
        }

        if (s.contains("unix")) {
            return EnumOS.linux;
        } else {
            return EnumOS.unknown;
        }
    }
    private static void logOut(String msg) {
        System.out.println(msg);
    }    
}
