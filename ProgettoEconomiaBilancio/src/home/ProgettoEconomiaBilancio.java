package home;

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.Color;
import javax.swing.JToolBar;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.JButton;
import java.awt.Component;
import javax.swing.Box;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Vector;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.FormSpecs;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import com.jgoodies.forms.layout.Sizes;
import net.miginfocom.swing.MigLayout;
import pdfcreator.PdfCreator;
import vocibilancio.VociBilancioAttivo;
import vocibilancio.VociBilancioContoEconomico;
import vocibilancio.VociBilancioPassivo;

import javax.swing.BoxLayout;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import java.awt.Dimension;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.ActionEvent;
import java.awt.Font;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.ListSelectionModel;

public class ProgettoEconomiaBilancio {

	private JFrame frame;
	private JComboBox<String> comboAzienda;
	private JComboBox<String> comboBoxBilancio;
	private JTextArea textNote;
	private JComboBox<String> comboBoxVociBilancio;
	private JSpinner spinnerValore;
	private JSpinner spinnerImposte;
	private JRadioButton rdbtnAttivo;
	private JRadioButton rdbtnPassivo;
	private JRadioButton rdbtnDare;
	private JRadioButton rdbtnAvere;
	private JRadioButton rdbtnContoEconomico;
	private JTable table;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ProgettoEconomiaBilancio window = new ProgettoEconomiaBilancio();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ProgettoEconomiaBilancio() {
		Globs.createNewDatabase("Database.db");
		initialize();
	}

	/**
	 * Funzione per l'inserimento delle aziende dal database alla comboAzienda
	 */
	public void aggiornaComboAzienda() {
		comboAzienda.removeAllItems();
		String sql = "SELECT id, Nome, Descrizione FROM Aziende";
		try (Connection conn = Globs.connect();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			comboAzienda.addItem("*Azienda non selezionata!*");
			// loop through the result set
			while (rs.next()) {
				comboAzienda.addItem(rs.getString("Nome"));
				comboAzienda.setSelectedItem(rs.getString("Nome"));
			}
			comboAzienda.setSelectedIndex(0);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		aggiornaTabella();
	}

	/**
	 * Aggiorna la combo bilancio con tutti i bilanci dell'azienda in questione e
	 * setta come attivo l'ultimo bilancio creato
	 * 
	 * @param idAzienda
	 */
	public void aggiornaComboBilancio(int idAzienda) {
		comboBoxBilancio.removeAllItems();
		String sql = "SELECT Anno FROM Bilanci WHERE id = " + idAzienda + " ";
		try (Connection conn = Globs.connect();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			// loop through the result set
			while (rs.next()) {
				comboBoxBilancio.addItem(rs.getString("Anno"));
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		aggiornaTabella();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		Globs.setHomeWindow(this); // setta nella classe Globs il riferimento a questa finestra

		frame = new JFrame();
		frame.setBackground(Color.WHITE);
		frame.setMinimumSize(new Dimension(1000, 650));
		frame.getContentPane().setBackground(new Color(220, 220, 220));
		frame.getContentPane()
				.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("438px:grow"), },
						new RowSpec[] { RowSpec.decode("24px"), FormSpecs.RELATED_GAP_ROWSPEC,
								new RowSpec(RowSpec.CENTER,
										Sizes.bounded(Sizes.DEFAULT, Sizes.constant("100dlu", false),
												Sizes.constant("100dlu", false)),
										0),
								FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));

		JToolBar toolBar = new JToolBar();
		toolBar.setBackground(Color.LIGHT_GRAY);
		toolBar.setFloatable(false);
		frame.getContentPane().add(toolBar, "1, 1, fill, top");

		JLabel lblAzienda = new JLabel("Azienda:");
		toolBar.add(lblAzienda);

		comboAzienda = new JComboBox<String>();

		/*
		 * metodo per aggiornare tabella bilanci al cambiare dell'azienda selezionata
		 * TODO aggiornamento dei mastrini relativi all'azienda
		 */
		comboAzienda.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				if (comboAzienda.getItemCount() != 0 && comboAzienda.getSelectedItem().toString() != null) {
					if (comboAzienda.getSelectedItem().toString() == "*Azienda non selezionata!*") {
						String azienda = comboAzienda.getSelectedItem().toString();
						comboBoxBilancio.removeAllItems();
						aggiornaTabella();
					} else {
						String azienda = comboAzienda.getSelectedItem().toString();
						String sql = "SELECT id FROM Aziende WHERE Nome = '" + azienda + "' ";
						try (Connection conn = Globs.connect();
								Statement stmt = conn.createStatement();
								ResultSet rs = stmt.executeQuery(sql)) {
							if (rs.next()) {
								aggiornaComboBilancio(rs.getInt("id"));
							}
						} catch (SQLException e) {
							System.out.println(e.getMessage());
						}
					}

					// aggiornaTabella();
				}
			}

		});
		toolBar.add(comboAzienda);

		JButton btnAggiungiAzienda = new JButton("Aggiungi Azienda");
		btnAggiungiAzienda.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnAggiungiAzienda.setForeground(Color.BLACK);
		btnAggiungiAzienda.setBackground(Color.WHITE);
		btnAggiungiAzienda.addMouseListener(new MouseAdapter() {
			@Override
			/**
			 * @author Davide Evento di click sul pulsante btnAggiungiAzienda, serve per
			 *         aprire il pannello in cui vengono inseriti i dati di una nuova
			 *         azienda.
			 */
			public void mouseClicked(MouseEvent e) {
				apriPannelloAzienda();
			}
		});
		toolBar.add(btnAggiungiAzienda);

		JButton btnCancellaAzienda = new JButton("Cancella Azienda");
		btnCancellaAzienda.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnCancellaAzienda.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		btnCancellaAzienda.setBackground(Color.WHITE);

		btnCancellaAzienda.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				cancellaAziendaDalDB();
			}
		});

		btnCancellaAzienda.setForeground(Color.RED);
		toolBar.add(btnCancellaAzienda);

		Component horizontalGlue = Box.createHorizontalGlue();
		toolBar.add(horizontalGlue);

		JPanel panel = new JPanel();
		panel.setBackground(new Color(220, 220, 220));
		frame.getContentPane().add(panel, "1, 3, fill, top");
		panel.setLayout(new MigLayout("", "[][grow]", "[][]"));

		JLabel lblBilancio = new JLabel("Bilancio:");
		panel.add(lblBilancio, "cell 0 0,alignx trailing");

		comboBoxBilancio = new JComboBox<String>();
		comboBoxBilancio.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (comboBoxBilancio.getItemCount() != 0 && comboBoxBilancio.getSelectedItem().toString() != null) {
					aggiornaTabella();
				}
			}
		});
		panel.add(comboBoxBilancio, "flowx,cell 1 0,growx");

		JButton btnCreaBilancio = new JButton("Inserisci Bilancio");
		btnCreaBilancio.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				apriPannelloBilancio();
			}
		});
		panel.add(btnCreaBilancio, "cell 1 0");

		JButton btnRimuoviBilancio = new JButton("Rimuovi Bilancio");
		btnRimuoviBilancio.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				rimuoviBilancioDalDB();
			}
		});
		panel.add(btnRimuoviBilancio, "cell 1 0");

//		JButton btnImportaBilancio = new JButton("Importa Bilancio");
//		panel.add(btnImportaBilancio, "flowx,cell 1 1");

		JButton btnEsportaBilancio = new JButton("Esporta Bilancio");
		btnEsportaBilancio.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				PdfCreator.creaPdfBilancio();
			}
		});
		panel.add(btnEsportaBilancio, "cell 1 1");

		JPanel panel_1 = new JPanel();
		panel_1.setBackground(new Color(220, 220, 220));
		frame.getContentPane().add(panel_1, "1, 5, fill, fill");
		panel_1.setLayout(new MigLayout("", "[210px,grow][550px]", "[65px,grow]"));

		JScrollPane scrollPane = new JScrollPane();

		panel_1.add(scrollPane, "cell 0 0,grow");

		table = new JTable();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		scrollPane.setViewportView(table);

		JPanel panel_2 = new JPanel();
		panel_2.setBackground(new Color(220, 220, 220));
		panel_1.add(panel_2, "cell 1 0,alignx center,growy");
		panel_2.setLayout(new MigLayout("", "[grow]", "[100px:n:100px][grow][grow][grow]"));

		JPanel panel_3 = new JPanel();
		panel_3.setBackground(new Color(220, 220, 220));
		panel_2.add(panel_3, "cell 0 0,growx,aligny top");
		panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));

		Box verticalBox_2 = Box.createVerticalBox();
		verticalBox_2.setBackground(new Color(220, 220, 220));
		verticalBox_2.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel_3.add(verticalBox_2);

		rdbtnAttivo = new JRadioButton("Attivo");
		rdbtnAttivo.setBackground(new Color(220, 220, 220));
		verticalBox_2.add(rdbtnAttivo);
		/*
		 * Creazione gruppo di radiobuttons affinch� sia possibile selezionare un solo
		 * radiobutton per gruppo alla volta
		 * 
		 */
		ButtonGroup gruppoAttivoPassivoContoEconomico = new ButtonGroup();

		rdbtnPassivo = new JRadioButton("Passivo");
		rdbtnPassivo.setBackground(new Color(220, 220, 220));
		verticalBox_2.add(rdbtnPassivo);
		gruppoAttivoPassivoContoEconomico.add(rdbtnPassivo);

		rdbtnContoEconomico = new JRadioButton("Conto Economico");
		rdbtnContoEconomico.setBackground(new Color(220, 220, 220));
		rdbtnContoEconomico.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				comboBoxVociBilancio.removeAllItems();
				aggiornaComboVociBilancio(comboBoxVociBilancio, 2);
			}
		});
		verticalBox_2.add(rdbtnContoEconomico);
		gruppoAttivoPassivoContoEconomico.add(rdbtnContoEconomico);

		rdbtnPassivo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				comboBoxVociBilancio.removeAllItems();
				aggiornaComboVociBilancio(comboBoxVociBilancio, 1);
			}
		});

		rdbtnAttivo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				comboBoxVociBilancio.removeAllItems();
				aggiornaComboVociBilancio(comboBoxVociBilancio, 0);
			}
		});
		gruppoAttivoPassivoContoEconomico.add(rdbtnAttivo);

		Box verticalBox = Box.createVerticalBox();
		verticalBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel_3.add(verticalBox);

		rdbtnDare = new JRadioButton("Dare");
		rdbtnDare.setMaximumSize(new Dimension(61, 23));
		rdbtnDare.setAlignmentX(Component.CENTER_ALIGNMENT);
		rdbtnDare.setBackground(new Color(220, 220, 220));
		verticalBox.add(rdbtnDare);

		rdbtnAvere = new JRadioButton("Avere");
		rdbtnAvere.setAlignmentX(Component.CENTER_ALIGNMENT);
		rdbtnAvere.setBackground(new Color(220, 220, 220));
		rdbtnAvere.setMinimumSize(new Dimension(61, 23));
		rdbtnAvere.setMaximumSize(new Dimension(61, 23));
		verticalBox.add(rdbtnAvere);

		ButtonGroup gruppoDareAvere = new ButtonGroup();
		gruppoDareAvere.add(rdbtnDare);
		gruppoDareAvere.add(rdbtnAvere);

		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		horizontalStrut_1.setBackground(new Color(220, 220, 220));
		panel_3.add(horizontalStrut_1);

		Box verticalBox_4 = Box.createVerticalBox();
		panel_3.add(verticalBox_4);

		Box horizontalBox_1 = Box.createHorizontalBox();
		horizontalBox_1.setAlignmentY(Component.CENTER_ALIGNMENT);
		verticalBox_4.add(horizontalBox_1);

		JLabel lblImposte = new JLabel("Imposte (%):");
		lblImposte.setAlignmentX(Component.CENTER_ALIGNMENT);
		horizontalBox_1.add(lblImposte);

		Component horizontalStrut_2 = Box.createHorizontalStrut(20);
		horizontalStrut_2.setMaximumSize(new Dimension(10, 25));
		horizontalBox_1.add(horizontalStrut_2);

		spinnerImposte = new JSpinner();
		spinnerImposte.setModel(new SpinnerNumberModel(40.0, 0.0, 100.0, 5.0));
		spinnerImposte.setMaximumSize(new Dimension(120, 20));
		horizontalBox_1.add(spinnerImposte);

		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.setAlignmentY(Component.CENTER_ALIGNMENT);
		verticalBox_4.add(horizontalBox);
		horizontalBox.setBackground(new Color(220, 220, 220));

		JLabel lblValore = new JLabel("Valore (\u20AC):");
		horizontalBox.add(lblValore);
		lblValore.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblValore.setBackground(Color.LIGHT_GRAY);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		horizontalStrut.setMaximumSize(new Dimension(20, 25));
		horizontalBox.add(horizontalStrut);

		spinnerValore = new JSpinner();
		horizontalBox.add(spinnerValore);
		spinnerValore.setModel(new SpinnerNumberModel(new Double(0), new Double(0), null, new Double(1)));
		spinnerValore.setMinimumSize(new Dimension(120, 20));
		spinnerValore.setMaximumSize(new Dimension(120, 20));

		JPanel panel_6 = new JPanel();
		panel_6.setBackground(new Color(220, 220, 220));
		panel_2.add(panel_6, "cell 0 1,growx,aligny top");

		Box verticalBox_3 = Box.createVerticalBox();
		verticalBox_3.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel_6.add(verticalBox_3);

		JLabel lblCodice = new JLabel("Voce bilancio:");
		lblCodice.setBackground(new Color(220, 220, 220));
		lblCodice.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblCodice.setOpaque(true);
		lblCodice.setVerticalTextPosition(SwingConstants.TOP);
		verticalBox_3.add(lblCodice);

		comboBoxVociBilancio = new JComboBox<String>();
		comboBoxVociBilancio.setToolTipText("");
		verticalBox_3.add(comboBoxVociBilancio);
		comboBoxVociBilancio.setPreferredSize(new Dimension(450, 22));
		comboBoxVociBilancio.setMinimumSize(new Dimension(450, 22));
		comboBoxVociBilancio.setMaximumSize(new Dimension(450, 22));

		JPanel panel_4 = new JPanel();
		panel_4.setBackground(new Color(220, 220, 220));
		panel_2.add(panel_4, "cell 0 2,grow");

		Box verticalBox_1 = Box.createVerticalBox();
		verticalBox_1.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel_4.add(verticalBox_1);

		JLabel lblDescrizione = new JLabel("Descrizione:");
		lblDescrizione.setAlignmentX(Component.CENTER_ALIGNMENT);
		verticalBox_1.add(lblDescrizione);

		textNote = new JTextArea();
		verticalBox_1.add(textNote);
		textNote.setSize(new Dimension(400, 50));

		textNote.setWrapStyleWord(true);
		textNote.setLineWrap(true);
		JScrollPane scroll = new JScrollPane(textNote);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		verticalBox_1.setPreferredSize(new Dimension(400, 100));
		verticalBox_1.add(scroll);

		JPanel panel_5 = new JPanel();
		panel_5.setBackground(new Color(220, 220, 220));
		panel_2.add(panel_5, "cell 0 3,grow");

		JButton btnInserisciMastrino = new JButton("Inserisci Mastrino");
		btnInserisciMastrino.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				aggiungiMastrinoAlDB();
			}
		});

		JButton btnRimuoviMastrino = new JButton("Rimuovi Mastrino");
		btnRimuoviMastrino.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				rimuoviMastrino();
			}
		});
		panel_5.add(btnRimuoviMastrino);
		panel_5.add(btnInserisciMastrino);
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);

		/**
		 * Metodi attivati al click su radio button
		 * 
		 * @author Matteo
		 */

		/* Inserisce nella comboAzienda le aziende che sono state inserite */
		aggiornaComboAzienda();
	}

	/**
	 * Metodo per aggiornare la combo che cotiene le voci del bilancio in base alla
	 * selezione dei radio button corripondenti
	 * 
	 * @author Matteo
	 * @param type 0 ->attivo, 1 ->passivo, 2->conto economico
	 */

	private void aggiornaComboVociBilancio(JComboBox<String> comboBoxVociBilancio, int type) {
		/*
		 * Aggiunta alla combo delle voci del bilancio
		 */
		if (type == 0) {
			for (VociBilancioAttivo voce : VociBilancioAttivo.values()) {
				comboBoxVociBilancio.addItem(voce.toString());
			}
		} else if (type == 1) {
			for (VociBilancioPassivo voce : VociBilancioPassivo.values()) {
				comboBoxVociBilancio.addItem(voce.toString());
			}
		} else if (type == 2) {
			for (VociBilancioContoEconomico voce : VociBilancioContoEconomico.values()) {
				comboBoxVociBilancio.addItem(voce.toString());
			}
		}

	}

	/**
	 * @author Matteo Apre il pannello aggiungi azienda
	 *
	 */
	private void apriPannelloAzienda() {
		new AggiungiAzienda();
	}

	/**
	 * @author Davide Apre il pannello inserisci bilancio
	 */
	private void apriPannelloBilancio() {
		String azienda = comboAzienda.getSelectedItem().toString();
		String sql = "SELECT id FROM Aziende WHERE Nome = '" + azienda + "' ";
		try (Connection conn = Globs.connect();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			if (rs.next()) {
				InserisciBilancio bilancio = new InserisciBilancio();
				bilancio.setIdAzienda(rs.getInt("id"));
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Metodo per cancellare una azienda dal db
	 * 
	 * @author Matteo
	 */
	private void cancellaAziendaDalDB() {
		Object[] possibilities = { "Azienda non selezionata!" };

		String sql = "SELECT id, Nome, Descrizione FROM Aziende";
		try (Connection conn = Globs.connect();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			// loop through the result set
			int i = 0;
			while (rs.next()) {
				/*
				 * autore matteo : poco elegante solo provvisorio
				 */
				Object[] temp = new Object[i + 1];
				temp = possibilities;
				possibilities = new Object[i + 2];
				for (int k = 0; k < i + 1; k++) {
					possibilities[k] = temp[k];
				}
				possibilities[i + 1] = rs.getString("Nome");
				i++;
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		String aziendaselezionata = (String) JOptionPane.showInputDialog(frame,
				"Seleziona il nome dell'azienda che vuoi cancellare", "Cancellazione Azienda",
				JOptionPane.PLAIN_MESSAGE, null, possibilities, "ham");
		while (aziendaselezionata != null && aziendaselezionata.compareTo("Azienda non selezionata!") == 0) {
			JFrame frame = new JFrame("Show Message Box");
			JOptionPane.showMessageDialog(frame, "Selezionare una azienda!", "ERRORE", JOptionPane.ERROR_MESSAGE);
			aziendaselezionata = (String) JOptionPane.showInputDialog(frame,
					"Seleziona il nome dell'azienda che vuoi cancellare", "Cancellazione Azienda",
					JOptionPane.PLAIN_MESSAGE, null, possibilities, "ham");
		}

		String qry = "DELETE FROM Aziende WHERE Nome = '" + aziendaselezionata + "'";
//TODO: bisogna eliminare anche i bilanci e i mastrini collegati all'azienda.
		try (Connection conn = Globs.connect(); PreparedStatement pstmt = conn.prepareStatement(qry)) {
			pstmt.executeUpdate();
		} catch (SQLException p) {
			System.out.println(p.getMessage());
		}

		aggiornaComboAzienda();
	}

	private void rimuoviBilancioDalDB() {
		Object[] possibilities = { "Bilancio non selezionato!" };

		int idAzienda = 0;
		try (Connection conn = Globs.connect(); Statement stmt = conn.createStatement();) {
			String qry1 = "SELECT id FROM Aziende WHERE Nome = '" + comboAzienda.getSelectedItem().toString() + "';";
			ResultSet rs1 = stmt.executeQuery(qry1);
			if (rs1.next())
				idAzienda = rs1.getInt("id");
			String sql = "SELECT Reference, Anno FROM Bilanci WHERE id = " + idAzienda + ";";
			ResultSet rs = stmt.executeQuery(sql);

			int i = 0;
			while (rs.next()) {
				/*
				 * autore matteo : poco elegante solo provvisorio
				 */
				Object[] temp = new Object[i + 1];
				temp = possibilities;
				possibilities = new Object[i + 2];
				for (int k = 0; k < i + 1; k++) {
					possibilities[k] = temp[k];
				}
				possibilities[i + 1] = rs.getString("Anno");
				i++;
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		String aziendaselezionata = (String) JOptionPane.showInputDialog(frame,
				"Seleziona l'anno del bilancio dell'azienda '" + comboAzienda.getSelectedItem().toString()
						+ "' che vuoi cancellare",
				"Cancellazione Bilancio", JOptionPane.PLAIN_MESSAGE, null, possibilities, "ham");
		while (aziendaselezionata != null && aziendaselezionata.compareTo("Bilancio non selezionato!") == 0) {
			JFrame frame = new JFrame("Show Message Box");
			JOptionPane.showMessageDialog(frame, "Selezionare un bilancio!", "ERRORE", JOptionPane.ERROR_MESSAGE);
			aziendaselezionata = (String) JOptionPane.showInputDialog(frame,
					"Seleziona l'anno del bilancio dell'azienda '" + comboAzienda.getSelectedItem().toString()
							+ "' che vuoi cancellare",
					"Cancellazione Bilancio", JOptionPane.PLAIN_MESSAGE, null, possibilities, "ham");
		}

		String qry = "DELETE FROM Bilanci WHERE id = " + idAzienda + " AND  Anno = " + aziendaselezionata + ";";
//TODO: bisogna eliminare anche i bilanci e i mastrini collegati all'azienda.
		try (Connection conn = Globs.connect(); PreparedStatement pstmt = conn.prepareStatement(qry)) {
			pstmt.executeUpdate();
		} catch (SQLException p) {
			System.out.println(p.getMessage());
		}
		comboAzienda.setSelectedIndex(0);
	}

	/**
	 * @author Davide Qua dentro bisogner� estrarre tutte le informazioni della
	 *         pagina e inserirle all'interno del DB. Inseriti anche i controlli
	 *         sulla selezione dei radio button.
	 */
	private void aggiungiMastrinoAlDB() {
		if (comboAzienda.getSelectedItem().toString() == "*Azienda non selezionata!*") {
			JFrame frame = new JFrame("Show Message Box");
			JOptionPane.showMessageDialog(frame, "Selezionare un'azienda prima di procedere!", "ERRORE",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (comboBoxBilancio.getItemCount() == 0) {
			JFrame frame = new JFrame("Show Message Box");
			JOptionPane.showMessageDialog(frame, "Selezionare un bilancio prima di procedere!", "ERRORE",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		try (Connection conn = Globs.connect()) {
			int idAzienda = 0;
			int idBilancio = 0;
			Statement stmt = conn.createStatement();
			String qry1 = "SELECT id FROM Aziende WHERE Nome = '" + comboAzienda.getSelectedItem().toString() + "';";
			ResultSet rs1 = stmt.executeQuery(qry1);
			if (rs1.next())
				idAzienda = rs1.getInt("id");

			String qry2 = "SELECT Reference FROM Bilanci WHERE id = " + idAzienda + " AND Anno = "
					+ Integer.valueOf(comboBoxBilancio.getSelectedItem().toString()) + ";";
			ResultSet rs2 = stmt.executeQuery(qry2);
			if (rs2.next())
				idBilancio = rs2.getInt("Reference");

			String dare_avere;
			if (rdbtnAvere.isSelected())
				dare_avere = "Avere";
			else if (rdbtnDare.isSelected())
				dare_avere = "Dare";
			else {
				JFrame frame = new JFrame("Show Message Box");
				JOptionPane.showMessageDialog(frame, "Selezionare 'Dare' o 'Avere'!", "ERRORE",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			String attivo_passivo;
			if (rdbtnAttivo.isSelected())
				attivo_passivo = "Attivo";
			else if (rdbtnPassivo.isSelected())
				attivo_passivo = "Passivo";
			else if (rdbtnContoEconomico.isSelected())
				attivo_passivo = "Conto Economico";
			else {
				JFrame frame = new JFrame("Show Message Box");
				JOptionPane.showMessageDialog(frame, "Selezionare 'Attivo', 'Passivo' o 'Conto Economico'!", "ERRORE",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			String qry = "INSERT INTO Mastrini (id, Anno, Voce, Euro, InOut, Attivo, Note) VALUES ("
					+ String.valueOf(idBilancio) + ", '"
					+ Integer.valueOf(comboBoxBilancio.getSelectedItem().toString()) + "', '"
					+ comboBoxVociBilancio.getSelectedItem().toString().replaceAll("'", "''") + "', "
					+ spinnerValore.getValue() + ", '" + dare_avere + "', '" + attivo_passivo + "', '"
					+ textNote.getText() + "' )";

			PreparedStatement pstmt = conn.prepareStatement(qry);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		aggiornaTabella();
	}

	/**
	 * @author Davide Funzione che permette di fare il resize delle colonne della
	 *         tabella
	 * @param table
	 */
	public void resizeColumnWidth(JTable table) {
		final TableColumnModel columnModel = table.getColumnModel();
		for (int column = 0; column < table.getColumnCount(); column++) {
			int width = 15; // Min width
			for (int row = 0; row < table.getRowCount(); row++) {
				TableCellRenderer renderer = table.getCellRenderer(row, column);
				Component comp = table.prepareRenderer(renderer, row, column);
				width = Math.max(comp.getPreferredSize().width + 1, width);
			}
			if (width > 500)
				width = 500;
			if (width > 100 && column == 4)
				width = 100;
			columnModel.getColumn(column).setPreferredWidth(width);
		}
	}

	/**
	 * @author Davide qui bisogner� andare a cercare tutti i mastrini dell'azienda
	 *         selezionata e dell'anno selezionato ed andare a inserirli nella
	 *         tabella popolandola con tutti i campi
	 */
	private void aggiornaTabella() {
		if (comboBoxBilancio.getItemCount() == 0) {
			DefaultTableModel dm = (DefaultTableModel) table.getModel();
			int rowCount = dm.getRowCount();
			for (int i = rowCount - 1; i >= 0; i--) {
				dm.removeRow(i);
			}
			return;
		}
		try (Connection conn = Globs.connect()) {
			int idAzienda = 0;
			int idBilancio = 0;
			Statement stmt = conn.createStatement();
			String qry1 = "SELECT id FROM Aziende WHERE Nome = '" + comboAzienda.getSelectedItem().toString() + "';";
			ResultSet rs1 = stmt.executeQuery(qry1);
			if (rs1.next()) {
				idAzienda = rs1.getInt("id");
			}
			String qry2 = "SELECT Reference FROM Bilanci WHERE id = " + idAzienda + " AND Anno = "
					+ Integer.valueOf(comboBoxBilancio.getSelectedItem().toString()) + ";";
			ResultSet rs2 = stmt.executeQuery(qry2);
			if (rs2.next()) {
				idBilancio = rs2.getInt("Reference");
			}

			String query = "SELECT idMastrino, Voce, Euro, InOut, Note FROM Mastrini WHERE id = " + idBilancio + ";";
			ResultSet rs = stmt.executeQuery(query);
			table.setModel(buildTableModel(rs));
			resizeColumnWidth(table);
			table.getColumnModel().getColumn(0).setMinWidth(0);
			table.getColumnModel().getColumn(0).setMaxWidth(0);

		} catch (SQLException e) {
			e.printStackTrace();
		}

//        Vector<String> columnNames;
//		columnNames.add(" ");
//        columnNames.add("Column 1");
//        columnNames.add("Column 2");
//        DefaultTableModel model = new DefaultTableModel(buildTableModel(rs), columnNames);
	}

	public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {

		ResultSetMetaData metaData = rs.getMetaData();

		// names of columns
		Vector<String> columnNames = new Vector<String>();
		int columnCount = metaData.getColumnCount();
//		for (int column = 1; column <= columnCount; column++) {
//			columnNames.add(metaData.getColumnName(column));
//		}

		// data of the table
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		while (rs.next()) {
			Vector<Object> vector = new Vector<Object>();
			for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
				vector.add(rs.getObject(columnIndex));
			}
			data.add(vector);
		}

		columnNames.add("id");
		columnNames.add("Voce di bilancio");
		columnNames.add("Valore");
		columnNames.add("Dare/Avere");
		columnNames.add("Descrizione");
		return new DefaultTableModel(data, columnNames);

	}

	/**
	 * @author Davide Qui viene creato l'header della tabella
	 */
	private void creaHeaderTabella() {

	}

	/**
	 * @author Davide Metodo utilizzato dalla classe inserisci bilancio per
	 *         verificare che non sia gi� stato inserito un bilancio con lo stesso
	 *         anno.
	 * @param anno
	 * @return false -> inserito true -> non inserito
	 */
	public boolean getAnnoBilanci(int anno) {
		try (Connection conn = Globs.connect()) {
			int idAzienda = 0;
			Statement stmt = conn.createStatement();
			String qry1 = "SELECT id FROM Aziende WHERE Nome = '" + comboAzienda.getSelectedItem().toString() + "';";
			ResultSet rs1 = stmt.executeQuery(qry1);
			if (rs1.next())
				idAzienda = rs1.getInt("id");

			String qry2 = "SELECT Reference FROM Bilanci WHERE Anno = '" + String.valueOf(anno) + "' AND id = "
					+ idAzienda + ";";
			ResultSet rs2 = stmt.executeQuery(qry2);
			if (rs2.next())
				return false;
			else
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * @author Matteo Metodo che restituisce l'anno bilancio selezionato nella
	 *         combox bilanci
	 * @return anno selezionato nella combox bilanci
	 */

	public int getBilancioSelected() {
		if (comboBoxBilancio.getItemCount() > 0) {
			return Integer.valueOf(comboBoxBilancio.getSelectedItem().toString());
		} else {
			JFrame frame = new JFrame("Show Message Box");
			JOptionPane.showMessageDialog(frame, "Bilancio non selezionato!!!", "ERRORE", JOptionPane.ERROR_MESSAGE);
			return -1;
		}
	}

	/**
	 * @author Matteo Metodo che restituisce l'azienda selezionata nella combox
	 *         azienda
	 * @return nome azienda selezionata nella combox
	 */

	public String getAziendaSelected() {
		if (comboAzienda.getItemCount() > 0) {
			return comboAzienda.getSelectedItem().toString();
		} else {
			JFrame frame = new JFrame("Show Message Box");
			JOptionPane.showMessageDialog(frame, "Azienda selezionata!!!", "ERRORE", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	/**
	 * @author Davide Funzione per eliminare il mastrino dal db
	 */
	private void rimuoviMastrino() {

		if (table.getSelectedRowCount() > 0) {
			int input = JOptionPane.showConfirmDialog(null, "Sei sicuro di voler procedere?", "Attenzione...",
					JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
			// 0=yes, 1=no, 2=cancel
			if (input == 0) {
				String idMastrino = table.getValueAt(table.getSelectedRow(), 0).toString();
				String qry = "DELETE FROM Mastrini WHERE idMastrino = " + idMastrino + ";";
				try (Connection conn = Globs.connect(); PreparedStatement pstmt = conn.prepareStatement(qry)) {
					pstmt.executeUpdate();
				} catch (SQLException p) {
					System.out.println(p.getMessage());
				}
				aggiornaTabella();
			} else {
				return;
			}

		} else {
			JFrame frame = new JFrame("Show Message Box");
			JOptionPane.showMessageDialog(frame, "Selezionare un mastrino prima di procedere conl'eliminazione.",
					"ERRORE", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * @author Matteo
	 * 
	 * @return valore spinner percentuale imposte
	 */

	public double getValueSpinnerImposte() {
		return (double) spinnerImposte.getValue();
	}

}
