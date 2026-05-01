package TeriaComputacion;

import TeriaComputacion.interfaz.MainWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            System.setProperty("sun.java2d.uiScale", "1");
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
