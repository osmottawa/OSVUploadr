/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.osmcanada.osvuploadr;
import javax.swing.*;
import java.io.File;
import java.lang.*;
import ca.osmcanada.osvuploadr.API.OSMApi;
import ca.osmcanada.osvuploadr.UploadManager;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import ca.osmcanada.osvuploadr.struct.ImageProperties;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ca.osmcanada.osvuploadr.Utils.*;
import ca.osmcanada.osvuploadr.struct.PageContent;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;


/**
 *
 * @author Jamie Nadeau
 */
public class JPMain extends javax.swing.JPanel {

    private final String BASE_URL="https://www.openstreetmap.org/";
    private final String URL_SEQUENCE = "http://openstreetview.com/1.0/sequence/";
    private final String URL_PHOTO = "http://openstreetview.com/1.0/photo/";
    private final String URL_FINISH = "http://openstreetview.com/1.0/sequence/finished-uploading/";
    private final String URL_ACCESS = "http://openstreetview.com/auth/openstreetmap/client_auth";
    private final String API_KEY = "rBWV8Eaottv44tXfdLofdNvVemHOL62Lsutpb9tw";
    private final String API_SECRET = "rpmeZIp49sEjjcz91X9dsY0vD1PpEduixuPy8T6S";
    private String last_dir ="";
    private Locale l;
    private ResourceBundle r;
    
    UploadManager um;
    /**
     * Creates new form JPMain
     */   
    public JPMain(Locale locale) {
        l = locale;
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch(Exception ex)
        {
            JOptionPane.showMessageDialog(null, "Was unable to set the native look and feel", "Info", JOptionPane.WARNING_MESSAGE);
        }        
        initComponents();
        try{
        r=ResourceBundle.getBundle("Bundle",l);
        }
        catch(Exception ex){
            System.out.println(ex.toString());
        }
        
        setUILang();
        
    }
    
    public void setBearing(){
        if(listDir.getSelectedObjects().length==0){
            JOptionPane.showMessageDialog(null, new String(r.getString("select_item").getBytes(),StandardCharsets.UTF_8), new String(r.getString("attention").getBytes(),StandardCharsets.UTF_8), JOptionPane.INFORMATION_MESSAGE);
            return; //Need selected elements to work
        }
        Object[] bearings = new Object[361];
        for(int i=0;i<=360;i++){
            bearings[i]=i;
        }
        String[] dirs = listDir.getSelectedItems();
        for(String dir:dirs){
            Object bearing_offset = JOptionPane.showInputDialog(jbRemove, new String(r.getString("set_offset").getBytes(),StandardCharsets.UTF_8), new String(r.getString("attention").getBytes(),StandardCharsets.UTF_8), JOptionPane.QUESTION_MESSAGE, null, bearings, 0);
            if(bearing_offset==null){
                return;
            }
            Thread t = new Thread(){
                public void run(){
                    processBearing(dir,(int)bearing_offset);
                }
            };
            t.start();
            
        }
    }
    
    public void processBearing(String directory, int Offset){
        File dir_photos = new File(directory);
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
        System.out.println("Pictures found:"+ String.valueOf(file_list.length));
        
        System.out.println("Sorting files");
        //sort by modified time
        Arrays.sort(file_list, new Comparator<File>(){
        public int compare(File f1, File f2)
        {
            return Long.valueOf(Helper.getFileTime(f1)).compareTo(Helper.getFileTime(f2));
        }});
        System.out.println("End sorting");
        
        Double last_bearing=0.0;
        ImageProperties imTO = null;
        ImageProperties imFROM = null;
        for(int i=file_list.length-1;i>=0;i--){
            if(i==0){
                //TODO: set last bearing 
                try{
                    Helper.setBearing(file_list[i], last_bearing);
                }
                catch(IOException|ImageReadException|ImageWriteException ex){
                    Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, "Error writing exif data", ex);
                } 
                continue;
            }
            if(imTO==null){
                imTO = Helper.getImageProperties(file_list[i]);
                imFROM = Helper.getImageProperties(file_list[i-1]);
            }
            else
            {
                imTO=imFROM;
                imFROM = Helper.getImageProperties(file_list[i-1]);
            }
            last_bearing = (Helper.calc_bearing(imFROM.getLatitude(), imFROM.getLongitude(), imTO.getLatitude(), imTO.getLongitude())+ Offset) % 360.00;
            System.out.println("Calculated bearing (with offset) at: " + last_bearing);
            try{
                Helper.setBearing(file_list[i], last_bearing);
            }
            catch(IOException|ImageReadException|ImageWriteException ex){
                Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, "Error writing exif data", ex);
            } 
        }
    }
    
    private void setUILang(){
            jlDirectories.setText(new String(r.getString("Directories").getBytes(), StandardCharsets.UTF_8));
            jbAdd.setText(new String(r.getString("Add_Folder").getBytes(), StandardCharsets.UTF_8));
            jbRemove.setText(new String(r.getString("Remove_Folder").getBytes(), StandardCharsets.UTF_8));
            jbRemoveDup.setText(new String(r.getString("Remove_Duplicates").getBytes(), StandardCharsets.UTF_8));
            jbUpload.setText(new String(r.getString("Upload").getBytes(), StandardCharsets.UTF_8));
            jbExit.setText(new String(r.getString("Exit").getBytes(), StandardCharsets.UTF_8));        
    }
    
    public String getOSMUser(String usr, String psw)throws IOException{
        final OAuth10aService service = new ServiceBuilder()
                           .apiKey(API_KEY)
                           .apiSecret(API_SECRET)
                           .build(OSMApi.instance());
        final OAuth1RequestToken requestToken = service.getRequestToken();
        String url = service.getAuthorizationUrl(requestToken);
        
        //Automated grant and login***************************************
        //Get logon form
        CookieStore httpCookieStore = new BasicCookieStore();        
        HttpClient client = HttpClientBuilder.create().setDefaultCookieStore(httpCookieStore).build();
        //post.setHeader("User-Agent", USER_AGENT);
        try{
            PageContent pc = Helper.getPageContent(url,client);
            
            List<Cookie> cookies = httpCookieStore.getCookies();
            System.out.println("Getting OSM Login page");
            String page = pc.getPage();
            
            System.out.println("Sending username and password");
            
            int start_indx = page.indexOf("value=\"",page.indexOf("name=\"authenticity_token\""))+7;
            int end_indx = page.indexOf('\"',start_indx);                     
            
            String utf8 = "✓";            
            String authenticity_token = page.substring(start_indx,end_indx);
            
            start_indx = page.indexOf("value=\"",page.indexOf("name=\"referer\""))+7;
            end_indx = page.indexOf('\"',start_indx);
            String referer = page.substring(start_indx,end_indx);
            
            Map<String,String> arguments = new HashMap<>();
            arguments.put("utf8", utf8);
            arguments.put("authenticity_token", authenticity_token);
            arguments.put("referer", referer);
            arguments.put("username", usr);
            arguments.put("password", psw);
            arguments.put("commit", "Login");
            arguments.put("openid_url", "");
            
            System.out.println("Logging in");
            page = sendForm(BASE_URL+"login",arguments,"POST",cookies);
            
            
            
            //Proccessing grant access page
            System.out.println("Processing Granting Access page");
            start_indx = page.indexOf("<form");//Find form tag
            start_indx = page.indexOf("action=\"", start_indx)+8; //get action url
            end_indx = page.indexOf('\"',start_indx); //get closing tag
            String action_url = page.substring(start_indx, end_indx);
            if(action_url.startsWith("/")){
                action_url=BASE_URL+ action_url.substring(1, action_url.length());
            }
            else if(!action_url.toLowerCase(Locale.ENGLISH).startsWith("http")){
                //Need to post same level as current url
                end_indx=url.lastIndexOf('/')+1;
                action_url=url.substring(0, end_indx)+action_url;
            }
            
            start_indx = page.indexOf("name=\"authenticity_token\"");
            start_indx = findOpening(page,start_indx,"<");
            start_indx = page.indexOf("value=\"",start_indx)+7;
            end_indx = page.indexOf('\"',start_indx);
            authenticity_token = page.substring(start_indx,end_indx);
            
            start_indx = page.indexOf("name=\"oauth_token\"");
            start_indx = findOpening(page,start_indx,"<");
            start_indx = page.indexOf("value=\"",start_indx)+7;
            end_indx = page.indexOf('\"',start_indx);
            String oauth_token=page.substring(start_indx, end_indx);
                      
            arguments.clear();
            arguments.put("utf8", utf8);
            arguments.put("authenticity_token", authenticity_token);
            arguments.put("oauth_token", oauth_token);
            arguments.put("allow_read_prefs", "yes");
            arguments.put("commit", "Grant+Access");
            
            //Get PIN
            System.out.println("Submitting grant access");
            page = sendForm(action_url,arguments,"POST",cookies);
            
            //Find lovely PIN code in page that might be multilingual...(fun)
            start_indx = page.indexOf("class=\"content-body\"");
            end_indx = page.indexOf("</div>",start_indx);
            String tmp = page.substring(start_indx,end_indx); //Get aproximate location of pin code
            
            Pattern p = Pattern.compile("([A-Za-z0-9]){15,25}");
            Matcher m = p.matcher(tmp);
            String pin = "";
            if(m.find())
            {
                pin = m.group();
            }
            final OAuth1AccessToken accessToken = service.getAccessToken(requestToken, pin);
            return accessToken.getToken()+"|"+accessToken.getTokenSecret();
            
        }
        catch(Exception ex)
        {
            Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, null, ex);
        }      
        
        
        
        //End Automated grant and login **********************************
        return "";
    }
    
    public String getOSMUser() throws IOException{
        final OAuth10aService service = new ServiceBuilder()
                           .apiKey(API_KEY)
                           .apiSecret(API_SECRET)
                           .build(OSMApi.instance());
        
        final OAuth1RequestToken requestToken = service.getRequestToken();
        String url = service.getAuthorizationUrl(requestToken);
        Helper.openBrowser(java.net.URI.create(url));
        //java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        String pin_code = JOptionPane.showInputDialog(null, new String(r.getString("auth_window_opened").getBytes(),StandardCharsets.UTF_8), new String(r.getString("auth_required").getBytes(),StandardCharsets.UTF_8), JOptionPane.INFORMATION_MESSAGE);
        final OAuth1AccessToken accessToken = service.getAccessToken(requestToken, pin_code);
        //final OAuthRequest request = new OAuthRequest(Verb.GET, BASE_URL + "api/0.6/user/details", service);
        //service.signRequest(accessToken, request);
        //Response response = request.send();
        //String body = response.getBody();
        //int indxUserId= body.indexOf("user id=\"");
        //int indxEndUserId = body.indexOf("\"",indxUserId+9);
        //int indxUsername= body.indexOf("display_name=\"");
        //int indxEndUsername = body.indexOf("\"",indxUsername+14);
        //String user_id = body.substring(indxUserId+9,indxEndUserId);
        //String username = body.substring(indxUsername+14,indxEndUsername);
        
        //return user_id +";"+username;
        return accessToken.getToken()+"|"+accessToken.getTokenSecret();
    }
    
    private int findOpening(String str,int current_index,String OpeningChar){
        for(int i=current_index;i>=0;i--){
            if(str.substring(i,i+1).equals(OpeningChar)){
                    return i;
                }
        }
        return 0;
    }

    private String sendForm(String target_url,Map<String,String> arguments,String method,List<Cookie> cookielist){
        try{
            URL url = new URL(target_url);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            //HttpURLConnection http = (HttpURLConnection)con;
            con.setRequestMethod(method); // PUT is another valid option
            con.setDoOutput(true);
            con.setInstanceFollowRedirects(false);
            StringBuffer cookiestr = new StringBuffer();
            if(cookielist!=null){
                if(cookielist.size()>0){
                    for(Cookie cookie:cookielist){
                        if(cookiestr.length()!=0){
                            cookiestr.append(";" + cookie.getName() +"=" +cookie.getValue());
                        }
                        else{
                            cookiestr.append(cookie.getName() +"=" +cookie.getValue());
                        }
                    }
                    con.setRequestProperty("Cookie", cookiestr.toString());
                }
            }
            
            con.setReadTimeout(5000);
            
            StringJoiner sj = new StringJoiner("&");
            for(Map.Entry<String,String> entry : arguments.entrySet())
                sj.add(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()) + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;
            
            con.setFixedLengthStreamingMode(length);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            con.setRequestProperty("Accept-Language", "en-us;");
            con.connect();
            try(OutputStream os = con.getOutputStream()) {
                os.write(out);
                os.close();
            }
            
            boolean redirect = false;
            int status = con.getResponseCode();
            
            if(status != HttpURLConnection.HTTP_OK) {
		if (status == HttpURLConnection.HTTP_MOVED_TEMP
			|| status == HttpURLConnection.HTTP_MOVED_PERM
				|| status == HttpURLConnection.HTTP_SEE_OTHER)
		redirect = true;
            }
            
            if(redirect){
                String newURL = con.getHeaderField("Location");
                String cookies = con.getHeaderField("Set-Cookie");
                if(cookies == null){
                    cookies= cookiestr.toString();
                }
                con = (HttpURLConnection) new URL(newURL).openConnection();
                con.setRequestProperty("Cookie", cookies);                
            }
            
            InputStream is = con.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte buf[] = new byte[1024];
            int letti;

            while ((letti = is.read(buf)) > 0)
            baos.write(buf, 0, letti);

            String data;
            data = new String(baos.toByteArray(), StandardCharsets.UTF_8);
            con.disconnect();
            
            return data;
            
        }
        catch(Exception ex){
            Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    private void sendFile(OutputStream out, String name, InputStream in, String fileName) {
        try {
            String o = "Content-Disposition: form-data; name=\"" + URLEncoder.encode(name,"UTF-8") + "\"; filename=\"" + URLEncoder.encode(fileName,"UTF-8") + "\"\r\nContent-Type: image/jpeg\r\n\r\n";
            out.write(o.getBytes(StandardCharsets.UTF_8));
            byte[] buffer = new byte[2048];
            for (int n = 0; n >= 0; n = in.read(buffer))
                out.write(buffer, 0, n);
            out.write("\r\n".getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendField(OutputStream out, String name, String field) {
        try {
            String o = "Content-Disposition: form-data; name=\"" + URLEncoder.encode(name,"UTF-8") + "\"\r\n\r\n";
            out.write(o.getBytes(StandardCharsets.UTF_8));
            out.write(field.getBytes(StandardCharsets.UTF_8));
            out.write("\r\n".getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void sendAuthTokens(String accessToken, String accessSecret){
        try
        {
            URL url = new URL(URL_ACCESS);
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection)con;
            http.setRequestMethod("POST"); // PUT is another valid option
            http.setDoOutput(true);
            
            Map<String,String> arguments = new HashMap<>();
            arguments.put("request_token", accessToken);
            arguments.put("secret_token", accessSecret);
            System.out.println("accessToken:" + accessToken + "|secret token:"+accessSecret);
            
            StringJoiner sj = new StringJoiner("&");
            for(Map.Entry<String,String> entry : arguments.entrySet())
                sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
            byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;
            
            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            http.connect();
            try(OutputStream os = http.getOutputStream()) {
                os.write(out);
                os.close();
            }
            InputStream is = http.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte buf[] = new byte[1024];
            int letti;

            while ((letti = is.read(buf)) > 0)
            baos.write(buf, 0, letti);

            String data;
            data = new String(baos.toByteArray(), StandardCharsets.UTF_8);
            http.disconnect();
            
        }
        catch(Exception ex){
            Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void sendFinished(long Sequence_id, String accessToken)
    {
        try
        {
            URL url = new URL(URL_FINISH);
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection)con;
            http.setRequestMethod("POST"); // PUT is another valid option
            http.setDoOutput(true);
            
            Map<String,String> arguments = new HashMap<>();
            arguments.put("access_token", accessToken);
            arguments.put("sequenceId", Long.toString(Sequence_id));
            System.out.println("accessToken:" + accessToken + "|sequenceId:"+Long.toString(Sequence_id));
            
            StringJoiner sj = new StringJoiner("&");
            for(Map.Entry<String,String> entry : arguments.entrySet())
                sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
            byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;
            
            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            http.connect();
            try(OutputStream os = http.getOutputStream()) {
                os.write(out);
                os.close();
            }
            InputStream is = http.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte buf[] = new byte[1024];
            int letti;

            while ((letti = is.read(buf)) > 0)
            baos.write(buf, 0, letti);

            String data;
            data = new String(baos.toByteArray(), StandardCharsets.UTF_8);
            http.disconnect();
            
        }
        catch(Exception ex){
            Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private long getSequence(ImageProperties imp, String accessToken)
    {
        try {
            URL url = new URL(URL_SEQUENCE);
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection)con;
            http.setRequestMethod("POST"); // PUT is another valid option
            http.setDoOutput(true);

            DecimalFormat df = new DecimalFormat("#.##############");
            df.setRoundingMode(RoundingMode.CEILING);
            System.out.println("Getting Sequence ID..");
            Map<String,String> arguments = new HashMap<>();
            arguments.put("uploadSource", "OSVUploadr");
            arguments.put("access_token", accessToken);
            arguments.put("currentCoordinate", df.format(imp.getLatitude())+ "," + df.format(imp.getLongitude()));
            System.out.println("currentCoordinate:" + df.format(imp.getLatitude()) + "," + df.format(imp.getLongitude()) );
            StringJoiner sj = new StringJoiner("&");
            for(Map.Entry<String,String> entry : arguments.entrySet())
                sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
            byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;
            System.out.println("Sending request:" + sj.toString());
            
            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            http.connect();
            try(OutputStream os = http.getOutputStream()) {
                os.write(out);
                os.close();
            }
            System.out.println("Request Sent getting sequence response...");
            InputStream is = http.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte buf[] = new byte[1024];
            int letti;

            while ((letti = is.read(buf)) > 0)
            baos.write(buf, 0, letti);

            String data = new String(baos.toByteArray(), StandardCharsets.UTF_8);
            int start = data.indexOf("\"osv\":{\"sequence\":{\"id\":\"");
            int end = data.indexOf('\"',start+25);
            System.out.println("Received request:" + data);
            String sequence_id= data.substring(start+25,end);
            System.out.println("Obtained Sequence ID: sequence_id");
            http.disconnect();            
            return Long.parseLong(sequence_id);
            
        } catch (Exception ex) {
            System.out.println(ex.toString());
            Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
    
    
    private void upload_Image(ImageProperties imp, String accessToken, long Sequence_id)
    {
        try{
            URL url = new URL(URL_PHOTO);
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection)con;
            http.setRequestMethod("POST"); // PUT is another valid option
            http.setDoOutput(true);
            
            String boundary = UUID.randomUUID().toString();
            byte[] boundaryBytes = ("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8);
            byte[] finishBoundaryBytes = ("--" + boundary + "--").getBytes(StandardCharsets.UTF_8);
            http.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            http.setRequestProperty("Accept", "*/*");

            // Enable streaming mode with default settings
            http.setChunkedStreamingMode(0); 
            System.out.println("Uploading:"+ imp.getFilePath());
            // Send our fields:
            try(OutputStream out = http.getOutputStream()) {
                // Send our header
                out.write(boundaryBytes);

                //Send access token
                sendField(out,"access_token",accessToken);
                
                // Send a seperator
                out.write(boundaryBytes);
                
                if(imp.getCompass()>=0){
                    // Send our compass
                    sendField(out, "headers", Double.toString(imp.getCompass()));

                    // Send another seperator
                    out.write(boundaryBytes);
                }  
                
                // Send our coordinates
                sendField(out, "coordinate", imp.getCoordinates());
                
                // Send a seperator
                out.write(boundaryBytes);   
                
                // Send our sequence id
                sendField(out, "sequenceId", Long.toString(Sequence_id));
                
                // Send a seperator
                out.write(boundaryBytes);   
                
                // Send our sequence index
                sendField(out, "sequenceIndex", Integer.toString(imp.getSequenceNumber()));
                    
                // Send a seperator
                out.write(boundaryBytes);        
    

                String[] tokens = imp.getFilePath().split("[\\\\|/]");
                String filename = tokens[tokens.length - 1];
                // Send our file
                try(InputStream file = new FileInputStream(imp.getFilePath())) {
                  sendFile(out, "photo", file, filename);
                }

                // Finish the request
                out.write(finishBoundaryBytes);
                out.close();
            }
            InputStream is = http.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte buf[] = new byte[1024];
            int letti;

            while ((letti = is.read(buf)) > 0)
            baos.write(buf, 0, letti);

            String data;
            data = new String(baos.toByteArray(), StandardCharsets.UTF_8);
            //TODO:Process returned JSON
            System.out.println(data);
            http.disconnect();
        }
        catch(Exception ex)
        {
            Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void process(String dir, String accessToken)
    {
        File dir_photos = new File(dir);
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
        System.out.println("Pictures found:"+ String.valueOf(file_list.length));
        
        System.out.println("Sorting files");
        //sort by modified time
        Arrays.sort(file_list, new Comparator<File>(){
        public int compare(File f1, File f2)
        {
            return Long.valueOf(Helper.getFileTime(f1)).compareTo(Helper.getFileTime(f2));
        }});
        System.out.println("End sorting");
        File f_sequence = new File(dir+"/sequence_file.txt");
        Boolean need_seq = true;
        long sequence_id=-1;
        System.out.println("Checking " + f_sequence.getPath() +" for sequence_file");
        if(f_sequence.exists())
        {
            try
            {
                System.out.println("Found file, reading sequence id");
                List<String> id = Files.readAllLines(Paths.get(f_sequence.getPath()));
                if(id.size()>0)
                {
                    sequence_id = Long.parseLong(id.get(0));
                    need_seq=false;
                }
            }
            catch(IOException | NumberFormatException ex)
            {
                System.out.println("Failed to read sequence file, requesting new sequence id" + ex.getMessage());
                need_seq=true;
            }            
        }
        else
        {
            System.out.println("Sequence file not found, will need to request new id");
            need_seq=true;
        }
        //TODO: Load count from file
        System.out.println("Looking for count file");
        int cnt =0;
        File f_cnt = new File(dir+"/count_file.txt");
        if(f_cnt.exists())
        {
            System.out.println("Found count file:" + f_cnt.toString());
            try
            {
                List<String> id = Files.readAllLines(Paths.get(f_cnt.getPath()));
                if(id.size()>0)
                {
                    cnt= Integer.parseInt(id.get(0));
                }
            }
            catch(Exception ex)
            {
                cnt=0;
            }     
        }
        else
        {
            try{
                System.out.println("Creating new count file:" + f_cnt.getPath());
                boolean createNewFile = f_cnt.createNewFile();
                if(!createNewFile){
                    JOptionPane.showMessageDialog(null, "Could not create count file, please check write permissions: " + f_cnt.getAbsoluteFile(), "Error", JOptionPane.ERROR);
                }
            }
            catch(Exception ex)
            {
                Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Current count at:" + String.valueOf(cnt));
        if(cnt>0){
            if(file_list.length>cnt){
                File[] tmp = new File[file_list.length-cnt];
                int local_cnt=0;
                for(int i=cnt;i<file_list.length;i++)
                {
                    tmp[local_cnt]=file_list[i];
                    local_cnt++;
                }
                file_list=tmp;
            }
        }

        System.out.println("Processing photos...");
        //Read file info
        for(File f : file_list)
        {
            try{
                System.out.println("Processing:" + f.getPath());
                ImageProperties imp = Helper.getImageProperties(f);
                System.out.println("Image Properties:" );
                System.out.println("Lat:" + String.valueOf(imp.getLatitude()) + " Long:" + String.valueOf(imp.getLongitude()) + "Created:" + String.valueOf(Helper.getFileTime(f)));
                //TODO: Check that file has GPS coordinates
                //TODO: Remove invalid photos
                if(need_seq)
                {
                    System.out.println("Requesting sequence ID");
                    sequence_id=getSequence(imp,accessToken);
                    System.out.println("Obtained:" + sequence_id);
                    byte[] bytes = Long.toString(sequence_id).getBytes(StandardCharsets.UTF_8);
                    Files.write(Paths.get(f_sequence.getPath()), bytes, StandardOpenOption.CREATE); 
                    need_seq=false;
                }
                imp.setSequenceNumber(cnt);
                cnt++; //TODO: Write count to file
                System.out.println("Uploading image:"+ f.getPath());
                upload_Image(imp,accessToken,sequence_id);
                System.out.println("Uploaded");
                String out =String.valueOf(cnt);
                Files.write(Paths.get(f_cnt.getPath()),out.getBytes("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING);

            }
            catch(Exception ex)
            {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR);
            }
             
        }
        System.out.println("Sending finish for sequence:"+ sequence_id);
        sendFinished(sequence_id, accessToken);
    }
                
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jbAdd = new javax.swing.JButton();
        listDir = new java.awt.List();
        jlDirectories = new javax.swing.JLabel();
        jbRemoveDup = new javax.swing.JButton();
        jbUpload = new javax.swing.JButton();
        jbExit = new javax.swing.JButton();
        jbRemove = new javax.swing.JButton();

        jbAdd.setText("Add Folder");
        jbAdd.setMaximumSize(new java.awt.Dimension(123, 23));
        jbAdd.setMinimumSize(new java.awt.Dimension(123, 23));
        jbAdd.setPreferredSize(new java.awt.Dimension(123, 23));
        jbAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbAddActionPerformed(evt);
            }
        });

        listDir.setMultipleMode(true);
        listDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listDirActionPerformed(evt);
            }
        });

        jlDirectories.setText("Directories");

        jbRemoveDup.setText("Remove Duplicates");
        jbRemoveDup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbRemoveDupActionPerformed(evt);
            }
        });

        jbUpload.setText("Upload");
        jbUpload.setMaximumSize(new java.awt.Dimension(123, 23));
        jbUpload.setMinimumSize(new java.awt.Dimension(123, 23));
        jbUpload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbUploadActionPerformed(evt);
            }
        });

        jbExit.setText("Exit");
        jbExit.setMaximumSize(new java.awt.Dimension(123, 23));
        jbExit.setMinimumSize(new java.awt.Dimension(123, 23));
        jbExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbExitActionPerformed(evt);
            }
        });

        jbRemove.setText("Remove Folder");
        jbRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbRemoveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jlDirectories, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(listDir, javax.swing.GroupLayout.PREFERRED_SIZE, 456, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jbAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jbRemove, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jbRemoveDup, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jbUpload, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jbExit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jlDirectories)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(listDir, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jbAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jbRemove)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jbRemoveDup)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jbUpload, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jbExit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(41, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void listDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listDirActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_listDirActionPerformed

    private void jbExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbExitActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_jbExitActionPerformed

    private void jbAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbAddActionPerformed
        JFileChooser fc = new JFileChooser();
        if(!last_dir.isEmpty()){
            fc.setCurrentDirectory(new java.io.File(last_dir)); // start at application current directory
        }
        fc.setMultiSelectionEnabled(true);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showSaveDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            if(fc.getSelectedFiles().length==1){
                int response = JOptionPane.showConfirmDialog(null, new String(r.getString("immediate_sub_folders").getBytes(), StandardCharsets.UTF_8), new String(r.getString("add_subfolders").getBytes(), StandardCharsets.UTF_8), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if(response == JOptionPane.NO_OPTION){
                    File folder = fc.getSelectedFile();
                    listDir.add(folder.getPath());
                    last_dir=folder.getPath();
                }
                else if (response == JOptionPane.YES_OPTION)
                {
                    //Get a list of subdirectories
                    String[] subDirs = fc.getSelectedFile().list(new FilenameFilter(){
                        @Override
                        public boolean accept(File current, String name){
                            return new File(current,name).isDirectory();
                        }
                    });
                
                    for(String subDir: subDirs){
                        listDir.add(new File(fc.getSelectedFile() + File.separator + subDir).getPath());
                    }                
                }
            }
            else if(fc.getSelectedFiles().length > 1)
            {
                File[] folders = fc.getSelectedFiles();
                for(File folder:folders){
                    listDir.add(folder.getPath());
                    last_dir=folder.getPath();
                }
            }            
        }
    }//GEN-LAST:event_jbAddActionPerformed

    private void jbRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbRemoveActionPerformed
        int selectedCnt = listDir.getSelectedItems().length;
        if(selectedCnt > 0)
        {
            for(int i = selectedCnt-1;i>=0;i--)
            {
                listDir.remove(listDir.getSelectedIndexes()[i]);
            }
        }
    }//GEN-LAST:event_jbRemoveActionPerformed

    private void jbUploadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbUploadActionPerformed
        String path = ca.osmcanada.osvuploadr.JFMain.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath = "";
        try{
            decodedPath = new File(URLDecoder.decode(path, "UTF-8")).getParentFile().getPath();
        }
        catch(Exception ex)
        {
            Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, "decodePath", ex);
        }
        File id = new File(decodedPath + "/access_token.txt");
        String accessToken="";
        System.out.println("id_file exists:"+ id.exists());
        if(!id.exists())
        {
            try{
                String[] buttons={new String(r.getString("automatically").getBytes(), StandardCharsets.UTF_8),new String(r.getString("manually").getBytes(), StandardCharsets.UTF_8),new String(r.getString("cancel").getBytes(), StandardCharsets.UTF_8)};
                int rc = JOptionPane.showOptionDialog(null,new String(r.getString("login_to_osm").getBytes(), StandardCharsets.UTF_8),new String(r.getString("confirmation").getBytes(), StandardCharsets.UTF_8),JOptionPane.INFORMATION_MESSAGE,0,null,buttons,buttons[0]);
                String token="";
                System.out.println("GetOSMUser");
                switch(rc){
                    case 0:                        
                        String usr = "";
                        String psw = "";
                        JTextField tf = new JTextField();
                        JPasswordField pf = new JPasswordField();
                        rc = JOptionPane.showConfirmDialog(null,tf,new String(r.getString("email_osm_usr").getBytes(),"UTF-8"), JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
                        if(rc == JOptionPane.OK_OPTION){
                            usr = tf.getText();
                        }
                        else{
                            return;
                        }
                        
                        rc = JOptionPane.showConfirmDialog(null,pf,new String(r.getString("enter_password").getBytes(),"UTF-8"), JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
                        if(rc == JOptionPane.OK_OPTION){
                            psw = new String(pf.getPassword());
                        }
                        else{
                            return;
                        }
                        token = getOSMUser(usr,psw);                     
                        break;
                    case 1:
                        token = getOSMUser();
                        break;
                    case 2:
                        return;
                        
                }
                Path targetPath = Paths.get("./access_token.txt");
                byte[] bytes = token.split("\\|")[0].getBytes(StandardCharsets.UTF_8);
                Files.write(targetPath, bytes, StandardOpenOption.CREATE); 
                accessToken = token.split("\\|")[0];
                String accessSecret = token.split("\\|")[1];
                sendAuthTokens(accessToken,accessSecret);                
            }
            catch(Exception ex)
            {
                Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, "GetOSMUser", ex);
            }
        }
        else
        {
            try{
                List<String> token = Files.readAllLines(Paths.get(id.getPath()));
                if(token.size()>0){
                    accessToken=token.get(0); //read first line
                }                
            }
            catch(Exception ex)
            {
                Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, "readAllLines", ex);
            }
        }
        System.out.println("Access Token obtained from file or OSM:" + accessToken);
        
        //Start processing list
        for(String item:listDir.getItems()){
            System.out.println("Processing folder:"+item);
            process(item,accessToken);
        }
        //um = new UploadManager(listDir.getItems());
        //um.start();
                
    }//GEN-LAST:event_jbUploadActionPerformed

    private void jbRemoveDupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbRemoveDupActionPerformed
        JFMain topframe = (JFMain)SwingUtilities.getWindowAncestor(this);
        topframe.showInfoBox();
        topframe.setInfoBoxText("Sorting items, please wait");
        for(String item:listDir.getItems()){
            Thread t = new Thread(){
                public void run(){
                    FolderCleaner fc = new FolderCleaner(item);
                    fc.setLocale(l);
                    fc.setInfoBox(topframe);
                    fc.removeDuplicates();  
                }
            };
            t.start();
        }
    }//GEN-LAST:event_jbRemoveDupActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jbAdd;
    private javax.swing.JButton jbExit;
    private javax.swing.JButton jbRemove;
    private javax.swing.JButton jbRemoveDup;
    private javax.swing.JButton jbUpload;
    private javax.swing.JLabel jlDirectories;
    private java.awt.List listDir;
    // End of variables declaration//GEN-END:variables
}
