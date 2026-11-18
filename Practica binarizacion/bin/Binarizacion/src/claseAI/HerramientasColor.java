/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package claseAI;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 *
 * @author panch
 */
public class HerramientasColor {
    public enum CanalColor {
		ROJO(2), VERDE(1), AZUL(0);

		private int index;

		private CanalColor(int index) {
			this.index = index;
		}

		public int getColorIndex() {
			return index;
		}
	}

	public static int obtenerARGBdeCanal(int color, CanalColor channel) {

                int aux =0;
                switch(channel){
                    case ROJO:
                        aux = new Color(color,0,0).getRGB();
                        break;
                    case VERDE:
                        aux = new Color(0,color,0).getRGB();
                        break;
                    case AZUL:
                        aux = new Color(0,0,color).getRGB();
                        break;
                }
                return aux;
	}

	public static int agregarColores(int color1, int color2) {
		return color1 | color2;
	}
	public static int validarRango(int color) {
		int nuevo = color;
		if (nuevo < 0)
			nuevo = 0;
		if (nuevo > 255)
			nuevo = 255;
		return nuevo;
	}
	public static int obtenerValorPorCanal(int argb, CanalColor channel) {
		Color color =  new Color(argb);
		int channelValue = 0;
		switch (channel) {
		case ROJO:
			channelValue = color.getRed();
			break;
		case VERDE:
			channelValue = color.getGreen();
			break;
		case AZUL:
			channelValue = color.getBlue();
			break;
		}
		return channelValue;
	}
}
