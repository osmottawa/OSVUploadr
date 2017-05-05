/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.osmcanada.osvuploadr.Utils;

import ca.osmcanada.osvuploadr.struct.ImageProperties;
import ca.osmcanada.osvuploadr.struct.PageContent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import org.apache.http.client.HttpClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Nadeaj
 */
public class HelperTest {
    
    public HelperTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getFileTime method, of class Helper.
     */
    @Test
    public void testGetFileTime() {
        System.out.println("getFileTime");
        File f = new File("tests" + File.separator+ "test1" + File.separator + "2017_01_05_12_09_36_994.jpg");
        long expResult = 1483618177200L;
        long result = Helper.getFileTime(f);
        assertEquals(expResult, result);
    }

    /**
     * Test of getImageProperties method, of class Helper.
     */
    @Test
    public void testGetImageProperties() throws IOException {
        System.out.println("getImageProperties");
        File f = new File("tests" + File.separator+ "test1" + File.separator + "2017_01_05_12_09_36_994.jpg");
        ImageProperties result = Helper.getImageProperties(f);
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        Double expResult=-75.63175833333332;
        assertEquals(expResult,result.getLongitude());
        expResult=45.422266666666665;
        assertEquals(expResult,result.getLatitude());
        expResult=66.41303551812523;
        assertEquals(expResult,result.getCompass());
    }

    /**
     * Test of openBrowser method, of class Helper.
     */
    @Ignore
    @Test
    public void testOpenBrowser() {
        System.out.println("openBrowser");
        URI uri = null;
        Boolean expResult = null;
        Boolean result = Helper.openBrowser(uri);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getOs method, of class Helper.
     */
    @Test
    public void testGetOs() {
        System.out.println("getOs");
        Helper.EnumOS result = Helper.getOs();
        if(result!=Helper.EnumOS.linux && result!=Helper.EnumOS.windows && result!=Helper.EnumOS.macos && result!=Helper.EnumOS.solaris){
            assertTrue("Failed to get OS type or OS type is unknown",false);
        }
    }

    /**
     * Test of getPageContent method, of class Helper.
     */
    @Ignore
    @Test
    public void testGetPageContent() throws Exception {
        System.out.println("getPageContent");
        String url = "";
        HttpClient client = null;
        PageContent expResult = null;
        PageContent result = Helper.getPageContent(url, client);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of calc_bearing method, of class Helper.
     */
    @Test
    public void testCalc_bearing() {
        System.out.println("calc_bearing");
        File dir_photos = new File("tests"+File.separator+"test1");
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
        //sort by modified time
        Arrays.sort(file_list, new Comparator<File>(){
        public int compare(File f1, File f2)
        {
            return Long.valueOf(Helper.getFileTime(f1)).compareTo(Helper.getFileTime(f2));
        }});
        
        Double expResult = null;
        Double result =null;
        
        ImageProperties imTO = Helper.getImageProperties(file_list[17]);
        ImageProperties imFROM = Helper.getImageProperties(file_list[16]);
        expResult=30.48363181326248;
        result = Helper.calc_bearing(imFROM.getLatitude(), imFROM.getLongitude(), imTO.getLatitude(), imTO.getLongitude());
        assertEquals(expResult, result);
        
        imTO = imFROM;
        imFROM = Helper.getImageProperties(file_list[15]);
        expResult=31.867219634095843;
        result = Helper.calc_bearing(imFROM.getLatitude(), imFROM.getLongitude(), imTO.getLatitude(), imTO.getLongitude());
        assertEquals(expResult, result);
        
        imTO = imFROM;
        imFROM = Helper.getImageProperties(file_list[14]);
        expResult=30.733979538144894;
        result = Helper.calc_bearing(imFROM.getLatitude(), imFROM.getLongitude(), imTO.getLatitude(), imTO.getLongitude());
        assertEquals(expResult, result);
        
        imTO = imFROM;
        imFROM = Helper.getImageProperties(file_list[13]);
        expResult=33.3751062711555;
        result = Helper.calc_bearing(imFROM.getLatitude(), imFROM.getLongitude(), imTO.getLatitude(), imTO.getLongitude());
        assertEquals(expResult, result);
        
        imTO = imFROM;
        imFROM = Helper.getImageProperties(file_list[12]);
        expResult=36.28818642366175;
        result = Helper.calc_bearing(imFROM.getLatitude(), imFROM.getLongitude(), imTO.getLatitude(), imTO.getLongitude());
        assertEquals(expResult, result);
        
        imTO = imFROM;
        imFROM = Helper.getImageProperties(file_list[11]);
        expResult=39.268278508845434;
        result = Helper.calc_bearing(imFROM.getLatitude(), imFROM.getLongitude(), imTO.getLatitude(), imTO.getLongitude());
        assertEquals(expResult, result);
        
        imTO = imFROM;
        imFROM = Helper.getImageProperties(file_list[10]);
        expResult=41.40698538457085;
        result = Helper.calc_bearing(imFROM.getLatitude(), imFROM.getLongitude(), imTO.getLatitude(), imTO.getLongitude());
        assertEquals(expResult, result);
        
        imTO = imFROM;
        imFROM = Helper.getImageProperties(file_list[9]);
        expResult=42.14643440834042;
        result = Helper.calc_bearing(imFROM.getLatitude(), imFROM.getLongitude(), imTO.getLatitude(), imTO.getLongitude());
        assertEquals(expResult, result);
        
        imTO = imFROM;
        imFROM = Helper.getImageProperties(file_list[8]);
        expResult=44.26949065687802;
        result = Helper.calc_bearing(imFROM.getLatitude(), imFROM.getLongitude(), imTO.getLatitude(), imTO.getLongitude());
        assertEquals(expResult, result);
        
        imTO = imFROM;
        imFROM = Helper.getImageProperties(file_list[7]);
        expResult=47.32687307806327;
        result = Helper.calc_bearing(imFROM.getLatitude(), imFROM.getLongitude(), imTO.getLatitude(), imTO.getLongitude());
        assertEquals(expResult, result);
        
        imTO = imFROM;
        imFROM = Helper.getImageProperties(file_list[6]);
        expResult=47.572751048647056;
        result = Helper.calc_bearing(imFROM.getLatitude(), imFROM.getLongitude(), imTO.getLatitude(), imTO.getLongitude());
        assertEquals(expResult, result);
        
        imTO = imFROM;
        imFROM = Helper.getImageProperties(file_list[5]);
        expResult=50.849285262852376;
        result = Helper.calc_bearing(imFROM.getLatitude(), imFROM.getLongitude(), imTO.getLatitude(), imTO.getLongitude());
        assertEquals(expResult, result);
        
        imTO = imFROM;
        imFROM = Helper.getImageProperties(file_list[4]);
        expResult=54.062491470608656;
        result = Helper.calc_bearing(imFROM.getLatitude(), imFROM.getLongitude(), imTO.getLatitude(), imTO.getLongitude());
        assertEquals(expResult, result);
        
        imTO = imFROM;
        imFROM = Helper.getImageProperties(file_list[3]);
        expResult=55.888487107080785;
        result = Helper.calc_bearing(imFROM.getLatitude(), imFROM.getLongitude(), imTO.getLatitude(), imTO.getLongitude());
        assertEquals(expResult, result);
        
        imTO = imFROM;
        imFROM = Helper.getImageProperties(file_list[2]);
        expResult=61.122434204936674;
        result = Helper.calc_bearing(imFROM.getLatitude(), imFROM.getLongitude(), imTO.getLatitude(), imTO.getLongitude());
        assertEquals(expResult, result);
        
        imTO = imFROM;
        imFROM = Helper.getImageProperties(file_list[1]);
        expResult=61.27800044531455;
        result = Helper.calc_bearing(imFROM.getLatitude(), imFROM.getLongitude(), imTO.getLatitude(), imTO.getLongitude());
        assertEquals(expResult, result);
        
        imTO = imFROM;
        imFROM = Helper.getImageProperties(file_list[0]);
        expResult=63.906251590678536;
        result = Helper.calc_bearing(imFROM.getLatitude(), imFROM.getLongitude(), imTO.getLatitude(), imTO.getLongitude());
        assertEquals(expResult, result);
    }
    
}
