package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.*;

public class MandelbrotApp extends JFrame{
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private JPanel mandelbrotPanel;
    private JSpinner workerSpinner;
    private ExecutorService executorService;
    private JButton repaintButton;
    private boolean isRepainting = false;



    public MandelbrotApp() {
        setTitle("Conjunto de Mandelbrot");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mandelbrotPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                renderMandelbrot(g);
            }
        };

        workerSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 16, 1));

        workerSpinner.addChangeListener(e -> restartCalculation());

        JPanel controlPanel = new JPanel();
        controlPanel.add(new JLabel("Número de Trabajadores:"));
        controlPanel.add(workerSpinner);

        add(controlPanel, BorderLayout.NORTH);
        add(mandelbrotPanel, BorderLayout.CENTER);

        executorService = Executors.newCachedThreadPool();

        repaintButton = new JButton("Repintar");
        repaintButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isRepainting) {
                    isRepainting = true;
                    repaintButton.setEnabled(false);
                    setBlackBackground();
                }
            }
        });

        controlPanel.add(repaintButton);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MandelbrotApp app = new MandelbrotApp();
            app.setVisible(true);
        });
    }

    private void renderMandelbrot(Graphics g) {
        int numWorkers = (int) workerSpinner.getValue();
        int width = mandelbrotPanel.getWidth();
        int height = mandelbrotPanel.getHeight();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        //Aqui lo que se está haciendo es dividir la altura de la imagen entre el número de trabajadores
        //para que cada uno se encargue de una parte de la imagen
        int stripHeight = height / numWorkers;

        //Utilidad de Java para poder obtener los resultados de los trabajadores de manera paralela,
        // no esperamos valor de retorno por ello se devuelve un void
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executorService);

        for (int i = 0; i < numWorkers; i++) {
            int startY = i * stripHeight;
            int endY = (i == numWorkers - 1) ? height : startY + stripHeight;
            MandelbrotWorker worker = new MandelbrotWorker(image, startY, endY);
            completionService.submit(worker);
        }

        // Esperar a que todos los trabajadores terminen
        for (int i = 0; i < numWorkers; i++) {
            try {
                completionService.take().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        g.drawImage(image, 0, 0, null);
    }

    private void restartCalculation() {
        executorService.shutdown();
        executorService = Executors.newFixedThreadPool((int) workerSpinner.getValue());
        mandelbrotPanel.repaint();
    }
    private void setBlackBackground() {
        getContentPane().setBackground(Color.BLACK);

        // Utiliza SwingUtilities.invokeLater() para actualizar la interfaz gráfica
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Timer timer = new Timer(1000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        getContentPane().setBackground(Color.WHITE);
                        repaintMandelbrot();
                    }
                });

                timer.setRepeats(false); // Ejecutar una sola vez
                timer.start();
            }
        });
    }

    private void repaintMandelbrot() {
        int numWorkers = (int) workerSpinner.getValue();
        int width = mandelbrotPanel.getWidth();
        int height = mandelbrotPanel.getHeight();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Restablece el fondo a blanco antes de repintar
        getContentPane().setBackground(Color.WHITE);

        System.out.println("Repintando el Mandelbrot con " + numWorkers + " trabajadores");

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                mandelbrotPanel.repaint();
                repaintButton.setEnabled(true); // Habilita el botón de repintar nuevamente
                isRepainting = false;
            }
        });
    }
}