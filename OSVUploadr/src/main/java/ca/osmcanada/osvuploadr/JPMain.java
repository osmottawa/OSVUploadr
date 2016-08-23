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
import com.drew.metadata.exif.ExifSubIFDDirectory;
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
import ca.osmcanada.osvuploadr.Utils.*;

/**
 *
 * @author Jamie Nadeau
 */
public class JPMain extends javax.swing.JPanel {

    private final String BASE_URL="http://www.openstreetmap.org/";
    private final String URL_SEQUENCE = "http://openstreetview.com/1.0/sequence/";
    private final String URL_PHOTO = "http://openstreetview.com/1.0/photo/";
    private final String URL_FINISH = "http://openstreetview.com/1.0/sequence/finished-uploading/";
    private String last_dir ="";
    
    UploadManager um;
    /**
     * Creates new form JPMain
     */   
    public JPMain() {
        initComponents();
    }
    
    public String GetOSMUser() throws IOException{
        final OAuth10aService service = new ServiceBuilder()
                           .apiKey("[api key here]")
                           .apiSecret("[secret here]")
                           .build(OSMApi.instance());
        
        final OAuth1RequestToken requestToken = service.getRequestToken();
        String url = service.getAuthorizationUrl(requestToken);
        java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        String pin_code = JOptionPane.showInputDialog(null, "Authorization window has opened, please paste authorization code below once authorized.\nWith out the \".\" at the end", "Authorization Required", JOptionPane.INFORMATION_MESSAGE);
        final OAuth1AccessToken accessToken = service.getAccessToken(requestToken, pin_code);
        final OAuthRequest request = new OAuthRequest(Verb.GET, BASE_URL + "api/0.6/user/details", service);
        service.signRequest(accessToken, request);
        Response response = request.send();
        String body = response.getBody();
        int indxUserId= body.indexOf("user id=\"");
        int indxEndUserId = body.indexOf("\"",indxUserId+9);
        int indxUsername= body.indexOf("display_name=\"");
        int indxEndUsername = body.indexOf("\"",indxUsername+14);
        String user_id = body.substring(indxUserId+9,indxEndUserId);
        String username = body.substring(indxUsername+14,indxEndUsername);
        
        return user_id +";"+username;
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
    private void SendFinished(long Sequence_id, String user_id)
    {
        try
        {
            URL url = new URL(URL_FINISH);
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection)con;
            http.setRequestMethod("POST"); // PUT is another valid option
            http.setDoOutput(true);
            
            Map<String,String> arguments = new HashMap<>();
            arguments.put("externalUserId", user_id);
            arguments.put("userType", "osm");
            arguments.put("sequenceId", Long.toString(Sequence_id));
            
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

            String data = new String(baos.toByteArray());
            http.disconnect();
            
        }
        catch(Exception ex){
            Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private long getSequence(ImageProperties imp, String user_id, String user_name)
    {
        try {
            URL url = new URL(URL_SEQUENCE);
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection)con;
            http.setRequestMethod("POST"); // PUT is another valid option
            http.setDoOutput(true);

            DecimalFormat df = new DecimalFormat("#.##############");
            df.setRoundingMode(RoundingMode.CEILING);
            
            Map<String,String> arguments = new HashMap<>();
            arguments.put("externalUserId", user_id);
            arguments.put("userType", "osm");
            arguments.put("userName", user_name);
            arguments.put("clientToken", "2ed202ac08ea9cf8d5f290567037dcc42ed202ac08ea9cf8d5f290567037dcc4");
            arguments.put("currentCoordinate", df.format(imp.getLatitude())+ "," + df.format(imp.getLongitude()));
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

            String data = new String(baos.toByteArray());
            int start = data.indexOf("\"osv\":{\"sequence\":{\"id\":\"");
            int end = data.indexOf("\"",start+25);
            String sequence_id= data.substring(start+25,end);
            http.disconnect();
            return Long.parseLong(sequence_id);
            
        } catch (Exception ex) {
            Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
    
    
    private void Upload_Image(ImageProperties imp, String user_id, String user_name, long Sequence_id)
    {
        try{
            URL url = new URL(URL_PHOTO);
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection)con;
            http.setRequestMethod("POST"); // PUT is another valid option
            http.setDoOutput(true);
            
            String boundary = UUID.randomUUID().toString();
            byte[] boundaryBytes = boundaryBytes =  ("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8);
            byte[] finishBoundaryBytes = ("--" + boundary + "--").getBytes(StandardCharsets.UTF_8);
            http.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            http.setRequestProperty("Accept", "*/*");

            // Enable streaming mode with default settings
            http.setChunkedStreamingMode(0); 
            
            // Send our fields:
            try(OutputStream out = http.getOutputStream()) {
                // Send our header
                out.write(boundaryBytes);

                // Send our sequence id
                sendField(out, "sequenceId", Long.toString(Sequence_id));
    
                // Send a seperator
                out.write(boundaryBytes);
                if(imp.getCompass()>=0){
                    // Send our compass
                    sendField(out, "headers", Double.toString(imp.getCompass()));

                    // Send another seperator
                    out.write(boundaryBytes);
                }
                
                // Send our sequence index
                sendField(out, "sequenceIndex", Integer.toString(imp.getSequenceNumber()));
    
                // Send a seperator
                out.write(boundaryBytes);
                
                // Send our coordinates
                sendField(out, "coordinate", imp.getCoordinates());
    
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

            String data = new String(baos.toByteArray());
            //TODO:Process returned JSON
            http.disconnect();
        }
        catch(Exception ex)
        {
            Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void Process(String dir, String usr_id, String usr_name)
    {
        File dir_photos = new File(dir);
        //filter only .jpgs
        FilenameFilter fileNameFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
               if (name.lastIndexOf('.') > 0) {
                    int lastIndex = name.lastIndexOf('.');
                    String str = name.substring(lastIndex);
                    if (str.equals(".jpg")) {
                        return true;
                    }
                }
                return false;
            }
        };
        File[] file_list = dir_photos.listFiles(fileNameFilter);
        //sort by modified time
        Arrays.sort(file_list, new Comparator<File>(){
        public int compare(File f1, File f2)
        {
            return Long.valueOf(Helper.getFileTime(f1)).compareTo(Helper.getFileTime(f2));
        }});
        
        File f_sequence = new File(dir+"/sequence_file.txt");
        Boolean need_seq = true;
        long sequence_id=-1;
        if(f_sequence.exists())
        {
            try
            {
                List<String> id = Files.readAllLines(Paths.get(f_sequence.getPath()));
                if(id.size()>0)
                {
                    sequence_id = Long.parseLong(id.get(0));
                    need_seq=false;
                }
            }
            catch(Exception ex)
            {
                need_seq=true;
            }            
        }
        else
        {
            need_seq=true;
        }
        //TODO: Load count from file
        int cnt =0;
        File f_cnt = new File(dir+"/count_file.txt");
        if(f_cnt.exists())
        {
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
                f_cnt.createNewFile();
            }
            catch(Exception ex)
            {
                Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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

        //Read file info
        for(File f : file_list)
        {
            try{
                ImageProperties imp = Helper.getImageProperties(f);
                //TODO: Check that file has GPS coordinates
                //TODO: Remove invalid photos
                if(need_seq)
                {
                    sequence_id=getSequence(imp,usr_id,usr_name);
                    byte[] bytes = Long.toString(sequence_id).getBytes(StandardCharsets.UTF_8);
                    Files.write(Paths.get(f_sequence.getPath()), bytes, StandardOpenOption.CREATE); 
                    need_seq=false;
                }
                imp.setSequenceNumber(cnt);
                cnt++; //TODO: Write count to file                
                Upload_Image(imp,usr_id,usr_name,sequence_id);
                String out =String.valueOf(cnt);
                Files.write(Paths.get(f_cnt.getPath()),out.getBytes("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING);

            }
            catch(Exception ex)
            {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR);
            }
             
        }
        SendFinished(sequence_id, usr_id);
    }
                
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        listDir = new java.awt.List();
        jLabel1 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();

        jButton1.setText("Add Folder");
        jButton1.setMaximumSize(new java.awt.Dimension(123, 23));
        jButton1.setMinimumSize(new java.awt.Dimension(123, 23));
        jButton1.setPreferredSize(new java.awt.Dimension(123, 23));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        listDir.setMultipleMode(true);
        listDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listDirActionPerformed(evt);
            }
        });

        jLabel1.setText("Directories");

        jButton2.setText("Remove Duplicates");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Upload");
        jButton3.setMaximumSize(new java.awt.Dimension(123, 23));
        jButton3.setMinimumSize(new java.awt.Dimension(123, 23));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Exit");
        jButton4.setMaximumSize(new java.awt.Dimension(123, 23));
        jButton4.setMinimumSize(new java.awt.Dimension(123, 23));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setText("Remove Folder");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(listDir, javax.swing.GroupLayout.PREFERRED_SIZE, 456, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jButton3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(listDir, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(41, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void listDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listDirActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_listDirActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        JFileChooser fc = new JFileChooser();
        if(!last_dir.isEmpty()){
            fc.setCurrentDirectory(new java.io.File(last_dir)); // start at application current directory
        }
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showSaveDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            int response = JOptionPane.showConfirmDialog(null, "Do you wish to add all immediate subfolders of this folder?", "Add subfolders?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
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
                    listDir.add(new File(fc.getSelectedFile() + "/" +subDir).getPath());
                }                
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        int selectedCnt = listDir.getSelectedItems().length;
        if(selectedCnt > 0)
        {
            for(int i = selectedCnt-1;i>=0;i--)
            {
                listDir.remove(listDir.getSelectedIndexes()[i]);
            }
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        String path = ca.osmcanada.osvuploadr.JFMain.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath="";
        try{
            decodedPath=new File(URLDecoder.decode(path, "UTF-8")).getParentFile().getPath();
        }
        catch(Exception ex)
        {
            Logger.getLogger(JPMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        File id = new File(decodedPath+"/id_file.txt");
        String usr="";
        if(!id.exists())
        {
            try{
                String user_id=GetOSMUser();
                Path targetPath = Paths.get("./id_file.txt");
                byte[] bytes = user_id.getBytes(StandardCharsets.UTF_8);
                Files.write(targetPath, bytes, StandardOpenOption.CREATE); 
                usr = user_id;
            }
            catch(Exception ex)
            {
            }
        }
        else
        {
            try{
                List<String> user_id = Files.readAllLines(Paths.get(id.getPath()));
                if(user_id.size()>0){
                    usr=user_id.get(0); //read first line
                }                
            }
            catch(Exception ex)
            {}
        }
        
        //Start processing list
        for(String item:listDir.getItems()){
            Process(item,usr.split(";")[0],usr.split(";")[1]);
        }
        //um = new UploadManager(listDir.getItems());
        //um.start();
                
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        for(String item:listDir.getItems()){
            FolderCleaner fc = new FolderCleaner(item);
            fc.RemoveDuplicates();  
        }        
    }//GEN-LAST:event_jButton2ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private java.awt.List listDir;
    // End of variables declaration//GEN-END:variables
}
