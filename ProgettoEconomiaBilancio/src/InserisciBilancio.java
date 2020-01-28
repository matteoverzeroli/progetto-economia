import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import javax.swing.JButton;
import net.miginfocom.swing.MigLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;


public class InserisciBilancio extends JFrame {

	private ProgettoEconomiaBilancio homeWindow;
	private JPanel contentPane;
	private JSpinner spinnerAnno;

	private int idAzienda = 0;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					InserisciBilancio frame = new InserisciBilancio();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void setIdAzienda(int idAzienda) {
		this.idAzienda = idAzienda;
	}

	/**
	 * Create the frame.
	 */
	public InserisciBilancio() {

		homeWindow = Globs.getHomeWindow();

		setBackground(Color.BLACK);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBackground(Color.BLACK);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JButton btnChiudi = new JButton("Chiudi");
		btnChiudi.setBounds(10, 231, 63, 23);
		contentPane.add(btnChiudi);

		JButton btnOK = new JButton("OK");
		btnOK.setBounds(365, 231, 63, 23);
		btnOK.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				buttonOkClicked();
			}
		});
		contentPane.add(btnOK);

		JLabel lblAnno = new JLabel("Anno:");
		lblAnno.setForeground(Color.WHITE);
		lblAnno.setBounds(10, 11, 48, 14);
		contentPane.add(lblAnno);

		spinnerAnno = new JSpinner();
		spinnerAnno.setModel(new SpinnerNumberModel(2019, 2000, 2019, 1));
		spinnerAnno.setBounds(68, 8, 133, 20);
		contentPane.add(spinnerAnno);
		setVisible(true);
	}

	/**
	 * ATTENZIONE: Bisogna inserire il controllo per cui uno non puo scegliere un
	 * anno di bilancio che ha gia inserito
	 */
	private void buttonOkClicked() {
		int anno = (int) spinnerAnno.getValue();
		String qry = "INSERT INTO Bilanci (id, Anno) VALUES (" + idAzienda + ", " + anno + ")";
		try (Connection conn = Globs.connect(); PreparedStatement pstmt = conn.prepareStatement(qry)) {
			pstmt.executeUpdate();
		} catch (SQLException p) {
			System.out.println(p.getMessage());
		}

		homeWindow.aggiornaComboBilancio(anno, idAzienda);
		this.dispose();
	}
}