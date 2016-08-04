/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package struct;

/**
 *
 * @author Jamie Nadeau
 */
public class ImageProperties {
    
    private Double _long;
    private Double _lat;
    private Double _compass;
    private String _filepath;
    private int _seq_num;
    
    // <editor-fold defaultstate="collapsed" desc="Get Set Methods">
    
    /**
     * Sets longitude
     * @param longitude 
     */
    public void setLongitude(Double longitude){
        _long=longitude;
    }
    
    /**
     * Sets latitude
     * @param latitude 
     */
    public void setLatitude(Double latitude){
        _lat=latitude;
    }
    
    /**
     * Sets compass
     * @param compass
     */
    public void setCompass(Double compass){
        _compass = compass;
    }
    
    /**
     * Sets file path of the image.
     * @param filepath 
     */
    public void setFilePath(String filepath){
        _filepath = filepath;
    }
    
    /**
     * Sets sequence number of the image.
     * @param seqnum 
     */
    public void setSequenceNumber(int seqnum){
        _seq_num = seqnum;
    }
    /**
     * Gets longitude
     * @return a Double that represents the longitude.
     */
    public Double getLongitude(){
        return _long;
    }
    /**
     * Gets the latitude
     * @return a Double that represents the latitude.
     */
    public Double getLatitude(){
        return _lat;
    }
    /**
     * Gets the compass
     * @return a Double that represents the direction the image was taken.
     */
    public Double getCompass(){
        return _compass;
    }
    /**
     * Gets the file path of the image
     * @return a String representing the full file URI
     */    
    public String getFilePath(){
        return _filepath;
    }
    /**
     * Gets the file sequence number
     * @return a int representing the index in the sequence assigned to it.
     */
    public int getSequenceNumber(){
        return _seq_num;
    }
    // </editor-fold>
    
}
