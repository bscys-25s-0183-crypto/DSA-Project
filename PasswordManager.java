import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class PasswordManager {
    static Map<String, String[]> db = new HashMap<>();
    static SecretKeySpec key;
    static final String FILE = "v.dat";
    static final Color BG = new Color(15, 23, 42), CARD = new Color(30, 41, 59);
    static final Color TEXT = new Color(241, 245, 249), ACCENT = new Color(56, 189, 248);

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("OptionPane.background", CARD);
            UIManager.put("Panel.background", CARD);
            UIManager.put("OptionPane.messageForeground", TEXT);
            
            String p = JOptionPane.showInputDialog(null, "Enter Master Password:", "Aegis Vault", 3);
            if (p == null || p.length() < 3) System.exit(0);
            
            key = new SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(p.getBytes()), "AES");
            if (Files.exists(Paths.get(FILE))) load();

            JFrame f = new JFrame("Web Vault Dashboard");
            f.setSize(950, 600);
            f.setLayout(new BorderLayout());
            f.setLocationRelativeTo(null);
            f.getContentPane().setBackground(BG);

            JPanel sidebar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
            sidebar.setPreferredSize(new Dimension(230, 0));
            sidebar.setBackground(CARD);
            sidebar.setBorder(new MatteBorder(0, 0, 0, 1, ACCENT));

            JLabel logo = new JLabel("🛡️ Web Vault", SwingConstants.CENTER);
            logo.setFont(new Font("Segoe UI", Font.BOLD, 24));
            logo.setForeground(ACCENT);
            logo.setPreferredSize(new Dimension(200, 60));
            sidebar.add(logo);

            JButton bAdd = btn("➕ Add Account", ACCENT, BG);
            JButton bView = btn("👁 Reveal Password", BG, TEXT);
            JButton bDel = btn("🗑 Delete Account", new Color(225, 29, 72), TEXT);
            
            sidebar.add(bAdd); sidebar.add(bView); sidebar.add(bDel);

            JPanel main = new JPanel(new BorderLayout());
            main.setBackground(BG);
            main.setBorder(new EmptyBorder(30, 40, 30, 40));

            JLabel title = new JLabel("Dashboard / Overview");
            title.setFont(new Font("Segoe UI", Font.BOLD, 22));
            title.setForeground(TEXT);
            title.setBorder(new EmptyBorder(0, 0, 25, 0));
            main.add(title, BorderLayout.NORTH);

            DefaultTableModel tm = new DefaultTableModel(new String[]{"Website Platform", "Username"}, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            JTable t = new JTable(tm);
            t.setBackground(CARD); t.setForeground(TEXT); t.setRowHeight(45);
            t.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            t.setSelectionBackground(ACCENT); t.setSelectionForeground(BG);
            t.setShowGrid(false); t.setIntercellSpacing(new Dimension(0, 0));
            
            JTableHeader th = t.getTableHeader();
            th.setBackground(BG); th.setForeground(ACCENT); 
            th.setFont(new Font("Segoe UI", Font.BOLD, 16));
            th.setPreferredSize(new Dimension(0, 45));
            th.setBorder(new MatteBorder(0, 0, 2, 0, ACCENT));

            JScrollPane sp = new JScrollPane(t);
            sp.getViewport().setBackground(BG);
            sp.setBorder(BorderFactory.createEmptyBorder());
            main.add(sp, BorderLayout.CENTER);

            Runnable ref = () -> { tm.setRowCount(0); db.forEach((k, v) -> tm.addRow(new Object[]{k, v[0]})); };
            ref.run();

            bAdd.addActionListener(e -> {
                JTextField s = new JTextField(), u = new JTextField(); JPasswordField pw = new JPasswordField();
                if (JOptionPane.showConfirmDialog(f, new Object[]{"Site Name:", s, "Username:", u, "Password:", pw}, "New Entry", 2) == 0) {
                    try { db.put(s.getText(), new String[]{u.getText(), enc(new String(pw.getPassword()), 1)}); save(); ref.run(); } catch(Exception ex){}
                }
            });

            bView.addActionListener(e -> {
                int r = t.getSelectedRow();
                if (r >= 0) try { 
                    String k = tm.getValueAt(r,0).toString();
                    JOptionPane.showMessageDialog(f, "Platform: " + k + "\nPassword: " + enc(db.get(k)[1], 2), "Secure Reveal", 1); 
                } catch(Exception ex){ JOptionPane.showMessageDialog(f, "Wrong Master Password!"); }
            });

            bDel.addActionListener(e -> {
                int r = t.getSelectedRow();
                if (r >= 0) { db.remove(tm.getValueAt(r,0).toString()); try{save(); ref.run();}catch(Exception ex){} }
            });

            f.add(sidebar, BorderLayout.WEST);
            f.add(main, BorderLayout.CENTER);
            f.setVisible(true);
        } catch (Exception e) {}
    }

    static JButton btn(String txt, Color bg, Color fg) {
        JButton b = new JButton(txt);
        b.setPreferredSize(new Dimension(190, 45));
        b.setBackground(bg); b.setForeground(fg);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false); b.setBorder(new LineBorder(ACCENT, 1));
        b.setCursor(new Cursor(12));
        return b;
    }

    static String enc(String d, int m) throws Exception {
        Cipher c = Cipher.getInstance("AES"); c.init(m, key);
        return m == 1 ? Base64.getEncoder().encodeToString(c.doFinal(d.getBytes())) : new String(c.doFinal(Base64.getDecoder().decode(d)));
    }

    static void save() throws Exception {
        PrintWriter w = new PrintWriter(FILE);
        db.forEach((k, v) -> w.println(k + "," + v[0] + "," + v[1]));
        w.close();
    }

    static void load() throws Exception {
        Files.readAllLines(Paths.get(FILE)).forEach(l -> {
            String[] p = l.split(","); if (p.length == 3) db.put(p[0], new String[]{p[1], p[2]});
        });
    }
}