/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.osmcanada.osvuploadr;

import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.UIManager;

/**
 *
 * @author Nadeaj
 */
public class JPInfoBox extends javax.swing.JPanel {

    private Locale l;
    private ResourceBundle r;
    /**
     * Creates new form JPInfoBox
     */
    public JPInfoBox(Locale locale) {
        l=locale;
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch(Exception ex){}
        initComponents();
        r=ResourceBundle.getBundle("Bundle",l);
        SetUILang();
    }

    private void SetUILang(){
        try{
            jlCurrentlyProc.setText(new String(r.getString("currently_processing").getBytes(),"UTF-8"));
        }
        catch(Exception ex)
        {}
    }
    public void SetProcessingText(String str){
        jlProcessing.setText(str);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jlCurrentlyProc = new javax.swing.JLabel();
        jlProcessing = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(623, 150));

        jlCurrentlyProc.setText("Currently Processing:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jlCurrentlyProc)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jlProcessing, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jlCurrentlyProc)
                    .addComponent(jlProcessing, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(78, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jlCurrentlyProc;
    private javax.swing.JLabel jlProcessing;
    // End of variables declaration//GEN-END:variables
}
