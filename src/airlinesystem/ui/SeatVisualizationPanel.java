package airlinesystem.ui;

import javax.swing.*;
import java.awt.*;

public class SeatVisualizationPanel extends JPanel {

    private final int rows;
    private final int cols;
    private boolean[] occupied;  // length = rows * cols

    public SeatVisualizationPanel(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.occupied = new boolean[rows * cols];

        setPreferredSize(new Dimension(380, 260));
        setBorder(BorderFactory.createTitledBorder("Seat Map"));
    }

    public void setSeatStates(boolean[] occupied) {
        if (occupied == null || occupied.length != rows * cols) {
            throw new IllegalArgumentException("Seat array length must be rows*cols");
        }
        this.occupied = occupied.clone();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (occupied == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int width  = getWidth();
        int height = getHeight();

        int cellW = width  / cols;
        int cellH = height / rows;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int idx = r * cols + c;

                int x = c * cellW + 2;
                int y = r * cellH + 2;
                int w = cellW - 4;
                int h = cellH - 4;

                if (occupied[idx]) {
                    g2.setColor(Color.RED);   // dolu
                } else {
                    g2.setColor(Color.GREEN); // boş
                }
                g2.fillRect(x, y, w, h);

                g2.setColor(Color.BLACK);
                g2.drawRect(x, y, w, h);
            }
        }
    }
}
