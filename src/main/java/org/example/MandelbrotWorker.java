package org.example;

import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;

class MandelbrotWorker implements Callable<Void> {
    private BufferedImage image;
    private int startY;
    private int endY;

    public MandelbrotWorker(BufferedImage image, int startY, int endY) {
        this.image = image;
        this.startY = startY;
        this.endY = endY;
    }
 @Override
 public Void call() throws Exception {
     // Calcula el conjunto de Mandelbrot en la franja de startY a endY
     for (int y = startY; y < endY; y++) {
         for (int x = 0; x < image.getWidth(); x++) {
             // Realiza los cálculos del conjunto de Mandelbrot para (x, y)
             // y establece el color en la imagen
             // (debes implementar esta parte según el algoritmo de Mandelbrot)
             double zx = 0;
             double zy = 0;
             double cX = (x - 400) / 200.0;
             double cY = (y - 300) / 200.0;
             int iter = 0;
             int maxIter = 570;
             while (zx * zx + zy * zy < 4 && iter < maxIter) {
                 double tmp = zx * zx - zy * zy + cX;
                 zy = 2.0 * zx * zy + cY;
                 zx = tmp;
                 iter++;
             }
             if (iter < maxIter) {
                 int color = 0x10000 * iter / maxIter;
                 image.setRGB(x, y, color);
             } else {
                 image.setRGB(x, y, 0);
             }

         }
     }
     return null;
 }

}
