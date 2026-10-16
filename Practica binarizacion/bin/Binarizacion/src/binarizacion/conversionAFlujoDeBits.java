/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package binarizacion;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Francisco Tapia
 */
public class conversionAFlujoDeBits {
    JFileChooser selector;
    File imagen;
    String rutaDeArchivo;
    BufferedImage imagenBuffereada;
    int [][] rgb;
    
    
    public int [][] imagenRGB(){
        selector = new JFileChooser();
        selector.setDialogTitle("Elige una imagen");
        FileNameExtensionFilter filtro = new FileNameExtensionFilter("Archivos de imagen", "jpg" , "png", "jpeg");
        selector.setFileFilter(filtro);
        int imagenARGB = selector.showOpenDialog(null);
        
        if (imagenARGB == JFileChooser.APPROVE_OPTION){
            imagen = selector.getSelectedFile();
            rutaDeArchivo = imagen.getAbsolutePath();
            
            try{
                imagenBuffereada = ImageIO.read(imagen);
                
                if (imagenBuffereada != null){
                    System.out.println("Imagen cargada " + imagenBuffereada.getHeight() + "x" + imagenBuffereada.getWidth());
                }
                
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        
        
        
        
        return rgb;
    } 
    
}
