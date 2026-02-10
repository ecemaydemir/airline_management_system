package airlinesystem.ui;

import javax.swing.*;

public class AirlineSystemApp {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            AppContext context = new AppContext();
            LoginFrame frame = new LoginFrame(context);
            frame.setVisible(true);
        });
    }
}
