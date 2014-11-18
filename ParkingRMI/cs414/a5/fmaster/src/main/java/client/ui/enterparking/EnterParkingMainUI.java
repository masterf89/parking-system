package a5.fmaster.src.main.java.client.ui.enterparking;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import a5.fmaster.src.main.java.client.ui.MainUIInterface;
import a5.fmaster.src.main.java.client.ui.ViewRatesUIInterface;
import a5.fmaster.src.main.java.common.ParkingServerInterface;
import a5.fmaster.src.main.java.server.domain.ParkingRate;

public class EnterParkingMainUI extends JFrame implements MainUIInterface {
	private ParkingServerInterface parking;
	public EnterParkingUI enterParkingUI;
	public EnterParkingViewRatesUI viewRatesUI;

	public JPanel mainPnl = new JPanel(new GridBagLayout());
	public JPanel mainContentPnl = new JPanel(new GridBagLayout());
	private JLabel messageLbl = new JLabel("");

	public EnterParkingMainUI(ParkingServerInterface parking) throws RemoteException {
		this.parking = parking;
		initializeChildUI();
		setupChildUI();
	}

	public void initializeChildUI() {
		enterParkingUI = EnterParkingUI.getInstance(this, parking);
		viewRatesUI = EnterParkingViewRatesUI.getInstance(this, parking);
	}

	public void setupChildUI() throws RemoteException {
		setupMainUI();
		enterParkingUI.setupUI();
		viewRatesUI.setupUI();
	}

	public void setupMainUI() throws RemoteException {
		setSize(500, 500);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		add(mainPnl);
		setVisible(true);

		// Top Portion of Main Panel - Content
		JPanel topLeftPnl = new JPanel();
		JPanel topRightPnl = new JPanel();
		addGridBagComponent(mainContentPnl, topLeftPnl, GridBagConstraints.BOTH, 0, 0);
		addGridBagComponent(mainContentPnl, topRightPnl, GridBagConstraints.BOTH, 1, 0);

		// Left Panel with buttons
		JButton enterParkingBtn = new JButton("Enter Parking");
		JButton viewRatesBtn = new JButton("View Parking Rates");
		topLeftPnl.setLayout(new GridLayout(2, 1));
		topLeftPnl.add(enterParkingBtn);
		topLeftPnl.add(viewRatesBtn);

		enterParkingBtn.addActionListener(new MainUIListener());
		viewRatesBtn.addActionListener(new MainUIListener());

		// Right Panel with Image
		ImageIcon mainImageIcon = null;
		java.net.URL imgURL = EnterParkingMainUI.class.getResource("../resources/mainImage.jpg");
		if (imgURL != null) {
			mainImageIcon = new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find image file.");
		}
		JLabel mainImageLbl = new JLabel("");
		mainImageLbl.setIcon(mainImageIcon);
		topRightPnl.add(mainImageLbl);

		// Bottom Portion of Main Panel - Messages and Parking Availability
		JPanel messagePnl = new JPanel();
		messagePnl.add(messageLbl);
		updateWelcomeMessage();

		// pack();
		// Main Panel
		addGridBagComponent(mainPnl, mainContentPnl, GridBagConstraints.BOTH, 0, 0);
		addGridBagComponent(mainPnl, messagePnl, GridBagConstraints.BOTH, 0, 1);
	}

	public void addGridBagComponent(JPanel parent, JComponent child, int gridBagFill, int gridx, int gridy) {
		GridBagConstraints localgbc = new GridBagConstraints();
		localgbc.fill = gridBagFill;
		localgbc.gridx = gridx;
		localgbc.gridy = gridy;
		parent.add(child, localgbc);
	}

	public void addGridBagComponent(JPanel parent, JComponent child, int gridBagFill, int gridx, int gridy, int gridWidth, int gridHeight) {
		GridBagConstraints localgbc = new GridBagConstraints();
		localgbc.fill = gridBagFill;
		localgbc.gridx = gridx;
		localgbc.gridy = gridy;
		localgbc.gridwidth = gridWidth;
		localgbc.gridheight = gridHeight;
		parent.add(child, localgbc);
	}

	public void showHideContentPanel(JPanel pnlToShow, JPanel pnlToHide) {
		pnlToHide.setVisible(false);
		pnlToShow.setVisible(true);
		if(pnlToShow.equals(mainContentPnl)) {
			messageLbl.setVisible(true);
		}
		else {
			messageLbl.setVisible(false);
		}
	}

	public void updateWelcomeMessage() throws RemoteException {
		int availableParking = parking.getCurrentAvailability();
		int parkingSize = parking.getCurrentParkingSize();
		displayMessage("Welcome to My Parking!! " + availableParking + " out of " + parkingSize + " parking spots available.");
	}

	public void displayMessage(String message) {
		messageLbl.setText(message);
	}

	public void populateParkingRatesInTable(JTable parkingRatesTbl) throws RemoteException {
		DefaultTableModel model = (DefaultTableModel) parkingRatesTbl.getModel();
		model.setRowCount(0);
		List<ParkingRate> parkingRatesList = new ArrayList<ParkingRate>();
		parkingRatesList = parking.getParkingRates();
		for (ParkingRate pr : parkingRatesList) {
			model.addRow(new Object[] { String.valueOf(pr.getHours()), String.valueOf(pr.getRate()) });
		}
	}

	private class MainUIListener implements ActionListener {

		private MainUIListener() {
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("Enter Parking")) {
				enterParking();
			}
			if (e.getActionCommand().equals("View Parking Rates")) {
				viewParkingRates();
			}
		}

		private void enterParking() {
			boolean isParkingAvailable;
			try {
				isParkingAvailable = parking.isParkingAvailable();
				if (isParkingAvailable) {
					showHideContentPanel(enterParkingUI.mainContentPnl, mainContentPnl);
					populateParkingRatesInTable(enterParkingUI.parkingRatesTbl);
				} else {
					displayMessage("Sorry. No parking spot available.");
				}
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
		}

		private void viewParkingRates() {
			showHideContentPanel(viewRatesUI.mainContentPnl, mainContentPnl);
			try {
				populateParkingRatesInTable(viewRatesUI.parkingRatesTbl);
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}

		}
	}
}
