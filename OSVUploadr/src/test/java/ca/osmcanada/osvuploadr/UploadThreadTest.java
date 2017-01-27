/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.osmcanada.osvuploadr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Nadeaj
 */
public class UploadThreadTest {
    
    public UploadThreadTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        if(Files.exists(Paths.get("tests"+ File.separator + "test2"))){
            File f = new File("tests"+ File.separator + "test2" + File.separator + "count_file.txt");
            f.delete();
        }
    }
    
    @After
    public void tearDown() {
        if(Files.exists(Paths.get("tests"+ File.separator + "test2"))){
            File f = new File("tests"+ File.separator + "test2" + File.separator + "count_file.txt");
            f.delete();
        }
    }

    /**
     * Test of setDirectory method, of class UploadThread.
     */
    @org.junit.Test
    public void testSetDirectory() {
        System.out.println("setDirectory");
        String Directory = "tests" + File.separator + "test1";
        UploadThread instance = new UploadThread();
        instance.setDirectory(Directory);
        assertEquals(true,instance.getUploaded(1));
        assertEquals(false,instance.getUploaded(2));
        assertEquals(true,instance.getUploaded(3));
        assertEquals(false,instance.getUploaded(4));
        assertEquals(true,instance.getUploaded(5));
        assertEquals(false,instance.getUploaded(6));
        assertEquals(true,instance.getUploaded(7));
        assertEquals(true,instance.getUploaded(8));
        assertEquals(false,instance.getUploaded(9));
        assertEquals(true,instance.getUploaded(10));
        assertEquals(false,instance.getUploaded(11));
        assertEquals(false,instance.getUploaded(12));
        assertEquals(false,instance.getUploaded(13));
        assertEquals(false,instance.getUploaded(14));
        assertEquals(false,instance.getUploaded(15));
        assertEquals(false,instance.getUploaded(16));
        assertEquals(false,instance.getUploaded(17));
        assertEquals(false,instance.getUploaded(18));
    }

    /**
     * Test of setUploaded method, of class UploadThread.
     */
    @org.junit.Test
    public void testSetUploaded() {
        System.out.println("setUploaded");
        boolean Uploaded = true;
        int PhotoNumber = 1;
        UploadThread instance = new UploadThread();
        instance.initUpload(128);
        //set the 8 first as true
        for(int i =1;i<=16;i++){
            Uploaded=true;
            instance.setUploaded(Uploaded, i);
            assertEquals(true,instance.getUploaded(i));
        }
        //test if they are still true
        for(int i =1;i<=16;i++){
            assertEquals(true,instance.getUploaded(i));
        }
        Uploaded=false;
        //Set random order and check status of others
        instance.setUploaded(Uploaded,4);
        assertEquals(true,instance.getUploaded(1));
        assertEquals(true,instance.getUploaded(2));
        assertEquals(true,instance.getUploaded(3));
        assertEquals(false,instance.getUploaded(4));
        assertEquals(true,instance.getUploaded(5));
        assertEquals(true,instance.getUploaded(6));
        assertEquals(true,instance.getUploaded(7));
        assertEquals(true,instance.getUploaded(8));
        assertEquals(true,instance.getUploaded(9));
        assertEquals(true,instance.getUploaded(10));
        assertEquals(true,instance.getUploaded(11));
        assertEquals(true,instance.getUploaded(12));
        assertEquals(true,instance.getUploaded(13));
        assertEquals(true,instance.getUploaded(14));
        assertEquals(true,instance.getUploaded(15));
        assertEquals(true,instance.getUploaded(16));

    }

    /**
     * Test of getUploaded method, of class UploadThread.
     */
    @org.junit.Test
    public void testGetUploaded() {
        System.out.println("getUploaded");
        int PhotoNumber = 0;
        UploadThread instance = new UploadThread();
        instance.initUpload(128);
        boolean expResult = true;
        for(int i =1;i<=1024;i++){
            instance.setUploaded(true, i);
            assertEquals(expResult, instance.getUploaded(i));
        }
        ArrayList test = new ArrayList();
        //Generate 200 test samples to test
        for(int i=1;i<200;i++){
            int num = 1+(int)(Math.random()*((1024-1)+1));
            test.add(num);
            instance.setUploaded(false, num);
        }        
        for(int i=1;i<1024;i++){
            if(test.indexOf(i)>=0){
                assertEquals(false, instance.getUploaded(i));
            }
            else
            {
                assertEquals(true, instance.getUploaded(i));
            }               
        }
        test.clear();
        
    }

    /**
     * Test of initUpload method, of class UploadThread.
     */
    @Test
    public void testInitUpload() {
        System.out.println("initUpload");
        int size = 2;
        UploadThread instance = new UploadThread();
        instance.initUpload(size);

    }

    /**
     * Test of run method, of class UploadThread.
     */
    @Test
    public void testRun() {
        //Place holder test
    }

    /**
     * Test of incrementCount method, of class UploadThread.
     */
    @Test
    public void testIncrementCount() {
        System.out.println("incrementCount");
        UploadThread instance = new UploadThread();
        instance.setDirectory("tests" + File.separator + "test2");
        for(int i=1;i<=10;i++){
            instance.incrementCount(i);
        }
        if(!Files.exists(Paths.get("tests" + File.separator + "test2"+ File.separator + "count_file.txt"))){
            assertTrue("Count file was not created", true);
        }
        
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("tests" + File.separator + "test2"+ File.separator + "count_file.txt"),"UTF-8"));
            
            String line;
            for(int i=1;i<=10;i++){                 
                line = br.readLine();
                line=line.trim();
                
                if(Integer.parseInt(line)!=i){
                    assertTrue("Line number: " + String.valueOf(i) +" does not match count: " + line,true);
                }
                   
            }
            br.close();
            
        }
        catch(Exception e){
                assertTrue("Exception occured.", true);
        }

    }
    
}
