package net.bubbaland.megaciv.client.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import net.bubbaland.gui.BubbaDragDropTabFrame;
import net.bubbaland.megaciv.game.Civilization;
import net.bubbaland.megaciv.game.Game;

public class MegaCivFrame extends BubbaDragDropTabFrame implements ActionListener {

	private static final long	serialVersionUID	= -8995125745966985308L;

	private final GuiController	controller;
	private final GuiClient		client;

	protected MegaCivFrame(GuiClient client, GuiController controller) {
		super(controller);
		this.client = client;
		this.controller = controller;
		this.initTabInfoHash();

		// Create Menu
		final JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);

		final JMenu gameMenu = new JMenu("Game");
		gameMenu.setMnemonic(KeyEvent.VK_G);
		menuBar.add(gameMenu);

		JMenuItem menuItem = new JMenuItem("New Game");
		menuItem.setMnemonic(KeyEvent.VK_N);
		menuItem.setActionCommand("New Game");
		menuItem.addActionListener(this);
		gameMenu.add(menuItem);

		menuItem = new JMenuItem("Load Defaults");
		menuItem.setActionCommand("Load Defaults");
		menuItem.addActionListener(this);
		gameMenu.add(menuItem);
	}

	@Override
	public Set<String> getTabNames() {
		Set<String> tabNames = super.getTabNames();
		ArrayList<Civilization.Name> civNames = this.client.getGame().getCivilizationNames();
		for (Iterator<String> iterator = tabNames.iterator(); iterator.hasNext();) {
			String tabName = iterator.next();
			if (Civilization.Name.contains(tabName)
					&& !civNames.contains(Civilization.Name.valueOf(tabName.toUpperCase()))) {
				iterator.remove();
			}
		}
		return tabNames;
	}

	@Override
	protected void initTabInfoHash() {
		super.initTabInfoHash();
		this.tabInformationHash.put("AST",
				new TabInformation("Panel showing AST", AstTabPanel.class,
						new Class<?>[] { GuiClient.class, GuiController.class, MegaCivFrame.class },
						new Object[] { this.client, this.controller, this }));
		for (Civilization.Name name : EnumSet.allOf(Civilization.Name.class)) {
			this.tabInformationHash.put(Game.capitalizeFirst(name.toString()),
					new TabInformation(name.toString() + " Information", CivInfoPanel.class,
							new Class<?>[] { GuiClient.class, GuiController.class, MegaCivFrame.class,
									Civilization.Name.class },
							new Object[] { this.client, this.controller, this, name }));
		}
	}

	public void updateGui(boolean forceUpdate) {
		super.updateGui(forceUpdate);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
		switch (command) {
			case "New Game":
				new NewGameDialog(this.client, this.controller);
				break;
			case "Load Defaults":
				this.controller.loadDefaults();
				break;
			default:
				this.log("Unknown action command " + command + "received by " + this.getClass().getSimpleName());
		}
	}

}
