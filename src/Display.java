import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

// Note that the JComponent is set up to listen for mouse clicks
// and mouse movement.  To achieve this, the MouseListener and
// MousMotionListener interfaces are implemented and there is additional
// code in init() to attach those interfaces to the JComponent.

public class Display extends JFrame implements MouseListener {
	private final int DISPLAY_WIDTH;   // Note how initialization works with constants
	private final int DISPLAY_HEIGHT;
	private final int COMPUTER_X_OFFSET = 25; // 10 pixels from left
	private final int HUMAN_X_OFFSET = 500; // 10 pixels from left
	private final int GRID_Y_OFFSET = 40; // 10 pixels from top
	private final int CELL_WIDTH = 36;
	private final int CELL_HEIGHT = 36;
	private Player[] player = new Player[2];  // player[0] = computer, player[1] = human
	private int xClicked, yClicked;  // grid position clicked, not mouse coords
	private int gameState;  // 0 = setup, 1 = play

	private Rectangle horizontal = new Rectangle(500, 500, 150, 32);
	private Rectangle vertical = new Rectangle(500, (int) horizontal.getY()+32, 150, 32);
	private Rectangle startRect = new Rectangle(500, 576, 150, 100);
	private boolean horizFlag = true;  // default to horizontal placement

	private Rectangle patrolRect = new Rectangle(676, 500, 150, 32);
	private int shipSelected = 0;  // default to patrol ship
	private final int[] SHIP_LENGTHS = { 2, 3, 3, 4, 5 };
	private final String[] SHIP_NAMES = { "Patrol", "Submarine", "Destroyer", "Battleship", "Carrier" };

	public Display(int width, int height, Player[] player) {
		DISPLAY_WIDTH = width;    // Width, height initialized
		DISPLAY_HEIGHT = height;
		init();
	}

	public void init() {
		setBackground(Color.WHITE);
		setSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
		initPlayers();
		addMouseListener(this);
	}

	public void initPlayers() {
		initComputerShips();		
		Grid grid = new Grid();
		player[1] = new Player(grid);  // Human
	}

	public void initComputerShips() {
		Grid grid = new Grid();
		// YOU MUST CHANGE THIS CODE SO THAT THE COMPUTER PLACES
		// THE SHIPS RANDOMLY INSTEAD OF (0, 0), (1, 1), (6, 6), 
		// (5, 7), and (0, 5)... (5 POINTS)

		//three arrays for ease of iteration
		String[] names = {"Carrier","Battleship","Submarine","Destroyer","Patrol"}; //ship names
		int[] lengths = {5,4,3,3,2}; //corresponding ship lengths
		boolean[] orientations = new boolean[5]; //true at each position if that corresponding ship is horizontal
		for (int i = 0; i < 5; i++){ //randomizes orientations
			orientations[i] = Math.random() > 5;
		}

		int c = 0;
		while (c < 5){
			int z = (int)(Math.random() * 100), x = z / 10, y = z % 10; //chooses random coordinate
			boolean placed = grid.addShip(lengths[c], new Point(x, y), orientations[c], names[c]);
			if (placed){ //if ship has been successfully placed, move on
				c++;
			} 
		}

		List<Point> list = new ArrayList<Point>();		
		player[0] = new Computer(grid, list);  // Computer
		//grid.print();  // Can see what grid looks like in console if you want
	}
	public void paint(Graphics g) {
		BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics gr = image.getGraphics();
		gr.fillRect(0, 0, getWidth(), getHeight());
		Font f1 = new Font("Monospaced", Font.BOLD, 24);
		gr.setFont(f1);
		gr.setColor(Color.WHITE);
		gr.fillRect(0, 0, getWidth(), getHeight());
		gr.setColor(Color.BLACK);
		showBoard(gr, 0);
		showBoard(gr, 1);
		if (gameState == 0) {
			showShipPlacementButtons(gr);
		}
		if (!player[0].hasShips()){ //if computer has no ships left
			JOptionPane.showMessageDialog(new JFrame(), "The human wins!");
		}
		if (!player[1].hasShips()){ //if human has no ships left
			JOptionPane.showMessageDialog(new JFrame(), "The computer wins!");
		}
		super.paint(g);
		g.drawImage(image, 0, 0, null);
	}

	public void showShipPlacementButtons(Graphics g) {
		int x = 	(int) horizontal.getX();
		int y = (int) horizontal.getY();
		int width = (int) horizontal.getWidth();
		int height = (int) horizontal.getHeight();

		// Draw horizontal box
		g.drawString("Orientation", 500, 492);
		if (horizFlag) g.setColor(new Color(0xff8800));
		else g.setColor(Color.WHITE);
		g.fillRect(x, y, width, height);
		g.setColor(Color.BLACK);
		g.drawRect(x, y, width, height);
		g.drawString("Horizontal", x+6, y+24);

		// Draw horizontal box
		y += 32;
		if (!horizFlag) g.setColor(new Color(0xff8800));
		else g.setColor(Color.WHITE);
		g.fillRect(x, y, width, height);
		g.setColor(Color.BLACK);
		g.drawRect(x, y, width, height);
		g.drawString("Vertical", x+20, y+24);

		x = (int) patrolRect.getX();
		y = (int) patrolRect.getY();
		width = (int) patrolRect.getWidth();
		height = (int) patrolRect.getHeight();

		g.drawString("Ship Type", x, 492);
		for (int i = 0; i < SHIP_NAMES.length; i++) {
			if (shipSelected == i) g.setColor(new Color(0xff8800));
			else g.setColor(Color.WHITE);
			g.fillRect(x, y+32*i, width, height);
			g.setColor(Color.BLACK);
			g.drawRect(x, y+32*i, width, height);
			g.drawString(SHIP_NAMES[i], x+6, y+32*i+24);			
		}

		if (player[1].getGrid().getShips().size() == 5) {
			g.setColor(Color.GREEN);
			g.fillRect(startRect.x, startRect.y, startRect.width, startRect.height);
			g.setColor(Color.BLACK);
			g.drawRect(startRect.x, startRect.y, startRect.width, startRect.height);
			Font f = g.getFont();
			g.setFont(new Font("Monospaced", Font.BOLD, 36));
			g.drawString("START", 519, 636);
			setFont(f);
		}
	}

	public void showBoard(Graphics g, int playerNum) {
		drawGrid(g, playerNum);
		drawRowLabels(g, playerNum);
		drawColumnLabels(g, playerNum);
		drawComputerShips(g);
		drawHumanShips(g);
		drawName(g, playerNum);
		drawShipStatus(g, playerNum);
	}

	public void drawGrid(Graphics g, int playerNum) {
		int xOffset = (playerNum == 0) ? COMPUTER_X_OFFSET : HUMAN_X_OFFSET;
		Grid grid = player[playerNum].getGrid();
		// y is the y coordinate in the grid
		for (int y = 0; y <= player[playerNum].getGrid().getRows(); y++) {
			g.drawLine(xOffset, GRID_Y_OFFSET + (y * (CELL_HEIGHT + 1)),
					xOffset + grid.getColumns() * (CELL_WIDTH + 1), GRID_Y_OFFSET
					+ (y * (CELL_HEIGHT + 1)));
		}
		// x is the x coordinate in the grid
		for (int x = 0; x <= player[playerNum].getGrid().getColumns(); x++) {
			g.drawLine(xOffset + (x * (CELL_WIDTH + 1)), GRID_Y_OFFSET,
					xOffset + (x * (CELL_WIDTH + 1)), GRID_Y_OFFSET
					+ grid.getRows() * (CELL_HEIGHT + 1));
		}
	}
	public void drawRowLabels(Graphics g, int playerNum) {
		int xOffset = (playerNum == 0) ? COMPUTER_X_OFFSET : HUMAN_X_OFFSET;
		Grid grid = player[playerNum].getGrid();
		for (int y = 0; y < grid.getRows(); y++) {
			g.drawString("" + (char) (57-y),
					xOffset + (CELL_WIDTH + 1) * grid.getColumns() + 12, 
					GRID_Y_OFFSET + (y * (CELL_HEIGHT + 1) + CELL_HEIGHT/2) + g.getFont().getSize()/2);
		}
	}

	public void drawColumnLabels(Graphics g, int playerNum) {
		int xOffset = (playerNum == 0) ? COMPUTER_X_OFFSET : HUMAN_X_OFFSET;
		Grid grid = player[playerNum].getGrid();
		for (int i = 0; i < grid.getColumns(); i++) {
			g.drawString("" + (char) (i + 65),
					xOffset + (i * (CELL_WIDTH + 1)) + CELL_WIDTH/2 - 6,
					GRID_Y_OFFSET + (CELL_HEIGHT + 1) * grid.getRows() + 28);
		}
	}
	public void drawName(Graphics g, int playerNum) {
		int xOffset = (playerNum == 0) ? COMPUTER_X_OFFSET : HUMAN_X_OFFSET;
		int xNameOffset = xOffset + 128;
		int yNameOffset = GRID_Y_OFFSET + 424;
		String str = (playerNum == 0) ? "Computer" : "  Human  ";
		g.drawString(str, xNameOffset, yNameOffset);
	}

	public void drawShipStatus(Graphics g, int playerNum) {
		ArrayList<Ship> deadShips = player[playerNum].getGrid().getDeadShips();
		int xOffset = (playerNum == 0) ? COMPUTER_X_OFFSET : HUMAN_X_OFFSET;
		int yOffset = GRID_Y_OFFSET + 456;
		if (gameState == 1) {
			g.drawString("Killed:", xOffset, yOffset);
			if (deadShips.size() == 0) {
				g.drawString("None", xOffset+28, yOffset+28);
			} else {
				for (int y = 0; y < deadShips.size(); y++) {
					g.drawString(deadShips.get(y).toString(), xOffset+28, yOffset+28*(y+1));
				}
			}
		} else {	
			if (playerNum == 1) {
				//				vertical.repaint();
				//				horizontal.repaint();				
			}
		}
	}

	public void drawComputerShips(Graphics g) {
		Grid grid = player[0].getGrid();
		int rows = grid.getRows();
		int columns = grid.getColumns();
		ArrayList<Ship> deadShips = grid.getDeadShips();
		Color c;
		String s;

		for (int x = 0; x < columns; x++) {
			for (int y = 0; y < rows; y++) {
				switch(grid.getFired(x, y)) {
				case 0: c = Color.BLACK;
				s = "";
				break;
				case 1: c = Color.RED;
				s = "*";
				break;
				case 2: c = Color.GREEN;
				s = "-";
				break;
				case 3: c = Color.GRAY;
				s = getDeadShip(deadShips, x, y).displayChar();
				break;
				default: c = Color.BLUE; // unanticipated
				s = "?";
				break;
				}
				g.setColor(c);
				g.drawString(s,
						COMPUTER_X_OFFSET + (x * (CELL_WIDTH + 1)) + CELL_WIDTH/2 - 6,
						GRID_Y_OFFSET + (rows-1-y) * (CELL_HEIGHT + 1) + CELL_HEIGHT/2 + g.getFont().getSize()/2 - 4);
			}
		}
		g.setColor(Color.BLACK);
	}

	public void drawHumanShips(Graphics g) {
		int xOffset = HUMAN_X_OFFSET;
		Grid grid = player[1].getGrid();
		int rows = grid.getRows();
		int columns = grid.getColumns();
		ArrayList<Ship> ships = grid.getShips();
		ArrayList<Ship> deadShips = grid.getDeadShips();
		//ships.addAll(grid.getDeadShips());

		g.setColor(Color.BLACK);
		for (Ship s : ships) {
			Point ll = s.getLowerLeft();
			for (int i = 0; i < s.length(); i++) {
				if (s.getDirection()) { // horizontal
					g.drawString(s.displayChar(),
							xOffset + ((ll.getX()+i) * (CELL_WIDTH + 1)) + CELL_WIDTH/2 - 6,
							GRID_Y_OFFSET + (rows-1-ll.getY()) * (CELL_HEIGHT + 1) + CELL_HEIGHT/2 + g.getFont().getSize()/2 - 6);
				} else {
					g.drawString(s.displayChar(),
							xOffset + (ll.getX() * (CELL_WIDTH + 1)) + CELL_WIDTH/2 - 6,
							GRID_Y_OFFSET + (rows-1-ll.getY()-i) * (CELL_HEIGHT + 1) + CELL_HEIGHT/2 + g.getFont().getSize()/2 - 6);					
				}
			}
		}

		Color c;
		String s;
		for (int x = 0; x < columns; x++) {
			for (int y = 0; y < rows; y++) {
				switch(grid.getFired(x, y)) {
				case 0: c = Color.BLACK;
				s = "";
				break;
				case 1: c = Color.RED;
				s = "*";
				break;
				case 2: c = Color.GREEN;
				s = "-";
				break;
				case 3: c = Color.GRAY;
				s = getDeadShip(deadShips, x, y).displayChar();
				break;
				default: c = Color.BLUE; // unanticipated
				s = "?";
				break;
				}
				g.setColor(c);
				g.drawString(s,
						HUMAN_X_OFFSET + (x * (CELL_WIDTH + 1)) + CELL_WIDTH/2 - 6,
						GRID_Y_OFFSET + (rows-1-y) * (CELL_HEIGHT + 1) + CELL_HEIGHT/2 + g.getFont().getSize()/2 - 4);
			}
		}
		g.setColor(Color.BLACK);

	}

	// If there is a dead ship at (x, y), return it
	public Ship getDeadShip(ArrayList<Ship> deadShips, int x, int y) {
		for (Ship s : deadShips) {
			if (s.contains(new Point(x, y))) {
				return s;
			}
		}
		return null;
	}

	// Add a ship to the player's grid
	public static void addToGrid(Grid grid, Ship s) {
		String shipLabel = s.toString();
		int shipLength = s.length();
		Scanner scan = new Scanner(System.in);
		System.out.println("Please enter x coordinate of one end of the " + shipLabel + ".");
		int x = scan.nextInt();
		System.out.println("Please enter x coordinate of one end of the " + shipLabel + ".");
		int y = scan.nextInt();
		System.out.println("Please enter direction from end of the " + shipLabel + ".");
		System.out.println("Use \"N\" for NORTH, \"E\" for EAST, \"S\" for SOUTH, \"W\" for WEST.");
		//		char dir = scan.next().charAt(0);
		char d = scan.next().charAt(0);
		boolean dir = (d == 'E' || d == 'e');

		if (!(grid.addShip(shipLength, new Point(x,y), dir, "test"))) {  		
			System.out.println("Illegal placement!  Try again!");
			addToGrid(grid, s);
		}
	}

	// mouseClicked, mouseEntered, mouseExited all unused, but need to
	// be present because of MouseListener interface
	public void mouseClicked(MouseEvent arg0) { }
	public void mouseEntered(MouseEvent arg0) {	}
	public void mouseExited(MouseEvent arg0) { }

	public void mousePressed(MouseEvent arg0) { 
		int xOffset = (gameState == 0 ? HUMAN_X_OFFSET : COMPUTER_X_OFFSET);
		int xCoord = arg0.getPoint().x;
		int yCoord = arg0.getPoint().y;
		xClicked = (xCoord - xOffset) / (CELL_WIDTH + 1);
		yClicked = 9 - ((yCoord - GRID_Y_OFFSET) / (CELL_HEIGHT + 1));
		//		System.out.println("[" + xClicked + ", " + yClicked + "]");
	}

	public void mouseReleased(MouseEvent arg0) {
		int xOffset = (gameState == 0 ? HUMAN_X_OFFSET : COMPUTER_X_OFFSET);
		int x = arg0.getPoint().x;
		int y = arg0.getPoint().y;
		int xCoord = (x - xOffset) / (CELL_WIDTH + 1);
		int yCoord = 9 - ((y - GRID_Y_OFFSET) / (CELL_HEIGHT + 1));

		if (gameState == 0) {
			// Horizontal/Vertical boxes
			if (horizFlag &&
					Utilities.between(x, vertical.getMinX(), vertical.getMaxX()) &&
					Utilities.between(y, vertical.getMinY(), vertical.getMaxY())) {
				horizFlag = false;
			} else if ((!horizFlag) &&
					Utilities.between(x, horizontal.getMinX(), horizontal.getMaxX()) &&
					Utilities.between(y, horizontal.getMinY(), horizontal.getMaxY())) {
				horizFlag = true;
				// Ship type boxes
			} else if (Utilities.between(x, patrolRect.getMinX(), patrolRect.getMaxX()) &&
					Utilities.between(y, patrolRect.getMinY(), patrolRect.getMaxY()+128)) {
				shipSelected = ((int) (y-patrolRect.getMinY())) / 32;
				// Human playing grid boxes
			} else if (Utilities.between(xCoord, 0, player[1].getGrid().getColumns()) &&
					Utilities.between(yCoord, 0, player[1].getGrid().getRows())) {
				System.out.println("[" + xCoord + ", " + yCoord + "]");
				boolean addShip = player[1].getGrid().addShip(SHIP_LENGTHS[shipSelected], new Point(xCoord, yCoord), horizFlag, SHIP_NAMES[shipSelected]);
				if (!addShip) {
					JOptionPane.showMessageDialog(new JFrame(), "Cannot place a " + SHIP_NAMES[shipSelected] + " at " +
							((char) (xCoord + 65)) + yCoord + (horizFlag ? " horizontally." : " vertically."));
				}
				// START button once ships have been placed
			} else if (Utilities.between(x, startRect.getMinX(), startRect.getMaxX()) &&
					Utilities.between(y, startRect.getMinY(), startRect.getMaxY()) &&
					player[1].getGrid().areAllShipsPlaced()) {
				gameState = 1;
			}
			//System.out.println("[" + x + ", " + y + "]");
		} else {	 // gameState == 1 --> Human fires, then computer fires
			System.out.println("[" + xCoord + ", " + yCoord + "]");
			if (xCoord == xClicked && yCoord == yClicked) {
				if (Utilities.between(xCoord, 0, player[1].getGrid().getColumns()) &&
						Utilities.between(yCoord, 0, player[1].getGrid().getRows())) {
					if (player[0].getGrid().getFired(xCoord, yCoord) == 0){
						fireOnComputer(xCoord, yCoord);
						fireOnHuman();
					}
				}
			}
		}
		repaint();
	}

	// YOU NEED TO WRITE THIS
	//
	// Make sure that the grid confirms that the point (x, y) is
	// on the grid.  If so, fire at it.  (Look at the Grid class
	// to see what methods can help you do this.)
	public void fireOnComputer(int xCoord, int yCoord) {
		Grid grid = player[0].getGrid();
		Point p = new Point(xCoord, yCoord);
		// YOU WRITE CODE HERE
		if (grid.isValid(p)){
			grid.fire(p);
		}
	}

	// THIS IS THE MOST IMPORTANT PART OF YOUR GRADE ON THIS TEST!
	//
	// fireOnHuman() needs improvement. Your mission is to change
	// it from randomly firing on the human's ships to firing in
	// an intelligent pattern.  If a ship is hit, following up until
	// that ship is destroyed is important to winning a game of
	// Battleship.  If a ship is not hit, then firing at random
	// is not optimal; the computer can surely do better than that.
	//
	// You can do make whatever edits you need to fireOnHuman.
	// You can add parameter(s) if you so desire.
	public void fireOnHuman() {
		Grid grid = player[1].getGrid();
		// CHANGE THE CODE BELOW TO MAKE THIS METHOD WORK
		((Computer) player[0]).fire(grid);
	}	
}
