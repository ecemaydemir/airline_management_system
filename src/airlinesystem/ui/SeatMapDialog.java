package airlinesystem.ui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class SeatMapDialog extends JDialog {

    // koltuk sayısı = seatStates.length
    // sabit kolon sayısını biz veriyoruz (örn. 6)
    private final boolean[] seatStates;
    private final int cols;
    private final int rows;

    /**
     * @param owner     Ana pencere (StaffMainFrame)
     * @param title     Pencere başlığı (örneğin "SYNC Seat Map")
     * @param seatStates index = row * cols + col; true = dolu, false = boş
     * @param cols      sütun sayısı (biz 6 kullanacağız)
     */
    public SeatMapDialog(Frame owner, String title, boolean[] seatStates, int cols) {
        super(owner, title, true);
        this.seatStates = (seatStates != null) ? seatStates : new boolean[0];
        this.cols = cols <= 0 ? 6 : cols;
        this.rows = this.seatStates.length == 0 ? 0 : this.seatStates.length / this.cols;

        initUI();
    }

    private void initUI() {
        setSize(700, 800);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(10, 10));

        // Üst açıklama
        JLabel info = new JLabel("Green: Reserved  |  White: Empty", SwingConstants.CENTER);
        info.setFont(info.getFont().deriveFont(Font.BOLD, 14f));
        add(info, BorderLayout.NORTH);

        // Grid panel
        JPanel gridPanel = new JPanel();
        if (rows > 0 && cols > 0) {
            gridPanel.setLayout(new GridLayout(rows, cols, 3, 3));
        } else {
            gridPanel.setLayout(new BorderLayout());
            gridPanel.add(new JLabel("No seats", SwingConstants.CENTER), BorderLayout.CENTER);
        }

        // Koltuk kutucukları
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int idx = r * cols + c;
                boolean reserved = (idx < seatStates.length) && seatStates[idx];

                // Koltuk label'ı: 1A, 1B, ..., 30F
                char colLetter = (char) ('A' + c);
                String seatLabel = (r + 1) + String.valueOf(colLetter);

                JLabel cell = new JLabel(seatLabel, SwingConstants.CENTER);
                cell.setOpaque(true);
                cell.setBorder(new LineBorder(Color.DARK_GRAY));

                if (reserved) {
                    cell.setBackground(new Color(0x4CAF50)); // yeşilimsi
                    cell.setForeground(Color.WHITE);
                } else {
                    cell.setBackground(Color.WHITE);
                    cell.setForeground(Color.BLACK);
                }

                gridPanel.add(cell);
            }
        }

        add(new JScrollPane(gridPanel), BorderLayout.CENTER);

        // Alt kısım: Close butonu
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        bottom.add(closeBtn);
        add(bottom, BorderLayout.SOUTH);
    }
}
