/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.osmcanada.osvuploadr.Utils;

import ca.osmcanada.osvuploadr.JPMain;
import ca.osmcanada.osvuploadr.struct.ImageProperties;
import ca.osmcanada.osvuploadr.struct.PageContent;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 *
 * @author Jamie Nadeau
 */
public final class Helper {
    private final static String USER_AGENT="Mozilla/5.0";
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
    
    public static PageContent GetPageContent(String url, HttpClient client) throws Exception{
        PageContent pc = new PageContent();
      
        HttpGet request = new HttpGet(url);
        request.setHeader("User-Agent", USER_AGENT);
	request.setHeader("Accept",
		"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
	request.setHeader("Accept-Language", "en-US,en;q=0.5");
     
        HttpResponse response = client.execute(request);
	int responseCode = response.getStatusLine().getStatusCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
	System.out.println("Response Code : " + responseCode);

	BufferedReader rd = new BufferedReader( new InputStreamReader(response.getEntity().getContent()));

	StringBuffer result = new StringBuffer();
	String line = "";
	while ((line = rd.readLine()) != null) {
		result.append(line);
	}
        //Setting cookies
        pc.SetCookies(response.getFirstHeader("Set-Cookie") == null ? "" :response.getFirstHeader("Set-Cookie").toString());
        pc.SetPage(result.toString());

        return pc;
    }
    
    public static List<NameValuePair> getFormParams(String html,List<String> FieldList)
    {
        List<NameValuePair> fields = new ArrayList<NameValuePair>();
        for(String field:FieldList){
            //TODO: Get fields from HTML
        }
        return null;
    }
}
