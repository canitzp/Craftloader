package craftloader.remap;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 * @author canitzp
 */
public class RemapWindow {

    public static File jar;

    public static void main(String[] args){
        JFrame frame = new JFrame("Remapper");
        frame.setSize(600, 400);

        //init layout
        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();
        JButton jarButton = new JButton("Choose Minecraft Jar");
        JButton startButton = new JButton("Start");
        JProgressBar bar = new JProgressBar(0, 4);
        panel1.add(jarButton);
        panel2.add(bar);
        frame.setLayout(new BorderLayout());
        frame.add(panel1, BorderLayout.NORTH);
        frame.add(startButton, BorderLayout.CENTER);
        frame.add(panel2, BorderLayout.SOUTH);

        startButton.setEnabled(false);

        frame.setVisible(true);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        jarButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(new FileNameExtensionFilter("*.jar", "jar"));
            int i = chooser.showOpenDialog(frame);
            if(i == JFileChooser.APPROVE_OPTION){
                jar = chooser.getSelectedFile();
                startButton.setEnabled(true);
            }
        });

        startButton.addActionListener(e -> {
            Thread remapTread = new Thread(() -> {
                Remap remap = new Remap(jar, new File(jar.getParentFile(), "dump"));
                remap.runDeobfuscation(bar);
            });
            remapTread.setDaemon(true);
            remapTread.start();
        });
    }

}
