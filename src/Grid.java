import java.util.ArrayList;

// You should not need to change any code in this class.
//
// You may find it useful to read the comments and to see what
// methods exist in the class
public class Grid {
	private final int ROWS = 10;
	private final int COLUMNS = 10;
	private ArrayList<Ship> ships = new ArrayList<Ship>();
	// If x, y contains an unhit ship, a point is set to true
	private ArrayList<Ship> deadShips = new ArrayList<Ship>();

	// occupied: is a square occupied or not?
	// Using Ships instead of booleans because if a ship is
	// fired on, it is important to be able to ask that ship if
	// it has been sunk.  If no ship is on the square, then
	// null is used.
	private Ship[][] occupied = new Ship[ROWS][COLUMNS];

	// The array fired tells us the following about squares:
	// 0 = unfired upon
	// 1 = fired upon, with ship hit (* for display)
	// 2 = fired upon, with ship not hit (X for display)
	// 3 = fired upon, ship killed
	private int[][] fired = new int[ROWS][COLUMNS];

	public Grid() {
		initGrid();
	}
	
	public int getRows() { return ROWS; }  // number of cells in x direction
	public int getColumns() { return COLUMNS; }    // number of cells in y direction
	public int getFired(int x, int y) { return fired[x][y]; } // 0, 1, 2, or 3
	public ArrayList<Ship> getShips() { return ships; }
	public ArrayList<Ship> getDeadShips() { return deadShips; }

	public void initGrid() {
		for (int x = 0; x < COLUMNS; x++) {
			for (int y = 0; y < ROWS; y++) {
				occupied[x][y] = null;  // currently empty
				fired[x][y] = 0;	        // not yet attacked
			}
		}	
		ships = new ArrayList<Ship>();
		deadShips = new ArrayList<Ship>();
	}

	// Precondition: Point p has valid [x,y]
	public void fire(Point p) {
		Ship s = occupied[p.getX()][p.getY()];
		if (s != null) {
			s.hit(p);
			System.out.println("BLAM! Direct hit at: " + p);
			if (s.isDead()) {
				deadShips.add(s);
				Point ll = s.getLowerLeft();
				int x = ll.getX();
				int y = ll.getY();
				boolean dir = s.getDirection();
				if (dir) { // horizontal
					for (int i = 0; i < s.length(); i++) {
						fired[x+i][y] = 3;
					}
				} else {
					for (int i = 0; i < s.length(); i++) {
						fired[x][y+i] = 3;
					}
				}
				ships.remove(s);
				System.out.println(s + " died horribly.");
			} else {
				fired[p.getX()][p.getY()] = 1;      // fired at, ship hit
			}
			occupied[p.getX()][p.getY()] = null;
		} else {
			if (fired[p.getX()][p.getY()] == 0) {
				fired[p.getX()][p.getY()] = 2;  // fired at, no ship
			}
			System.out.println("Whiff");
		}
		s = null;
	}

	public boolean gameOver() {
		return deadShips.size() == 5;
	}

	// For printing the grid in the console for testing purposes
	public void print() {
		System.out.print("[ ");
		for (int y = occupied.length-1; y >= 0; y--) {
			System.out.print("( ");
			for (int x = 0; x < occupied[0].length; x++) {
				Ship occ = occupied[x][y];
				if (occ != null) {
					System.out.print(occ.displayChar() + " ");
				} else {
					System.out.print("- ");
				}
			} 
			if (y != 0) {
				System.out.println(")");
				System.out.print("  ");
			} else {
				System.out.print(") ");
			}
		} System.out.println("]");
	}

	public String toString() {
		return "" + ships;
	}

	// Returns true if p is a legal point on the Grid
	public boolean isValid(Point p)
	{
		return Utilities.between(p.getX(), 0, COLUMNS - 1) &&
				Utilities.between(p.getY(), 0, ROWS - 1);
	}

	// Returns true if ship added successfully, false otherwise
	// The following assumes that the click location on the grid to place a
	// ship is the LOWER LEFT square on which the ship should be placed.
	//
	// Conditions for putting a ship on the grid:
	// 1. All parts of the ship must be on squares within the grid.
	//    If the user tries to place a Battleship (length == 4)
	//    horizontally at H5, it will fail because I5 and J5 exists,
	//    but there is no K5 to put the other end of the ship.
	// 2. No other ship can occupy a square where the new ship is to be
	//    placed.  However, the user should be able to decide that an
	//    original placement of a ship can be changed, so if an attempt
	//    is made to place a Destroyer on the board, an existing Destroyer
	//    would not be in conflict since it would be removed before the
	//    new one is placed.
	public boolean addShip(int shipLength, Point p, boolean horiz, String name) {
		int x = p.getX();
		int y = p.getY();

		// Verify that the squares on which the ship is to be placed
		// are open and on the grid
		if (horiz) {  // horizontal
			if (!(Utilities.between(x+shipLength-1, 0, COLUMNS-1) &&
					Utilities.between(x, 0, COLUMNS-1) &&
					Utilities.between(y, 0, ROWS-1)))
				return false;
			for (int i = 0; i < shipLength; i++) {
				Ship placedShip = occupied[x+i][y];
				if (placedShip != null) {
					// If the conflicting ship is the same type as the
					// one we are trying to place, it is not actually
					// a conflict because we would remove it to do the
					// new placement.  Ships of other types are a problem.
					// (We cannot put a submarine where a destroyer already is,
					// for example...)
					if (!placedShip.getName().equals(name)) {
						return false;
					}
				}
			} 
			// Vertical placements similar to horizontal placements
		} else {  // vertical
			if (!(Utilities.between(x, 0, COLUMNS-1) &&
					Utilities.between(y, 0, ROWS-1) &&
					Utilities.between(y+shipLength-1, 0, ROWS-1)))
				return false;
			for (int i = 0; i < shipLength; i++) {
				Ship placedShip = occupied[x][y+i];
				if (placedShip != null) {
					if (!placedShip.getName().equals(name)) {
						return false;
					}
				}
			}
		}

		// If we get to this point, the ship can be placed
		// Need to first remove any existing placement of this type
		int j = 0;
		while (j < ships.size()) {
			Ship shp = ships.get(j);
			if (shp.getName().equals(name)) {
				Point ll = shp.getLowerLeft();
				boolean dir = shp.getDirection();
				if (dir) { // horizontal
					for (int k = 0; k < shp.length(); k++) {
						occupied[ll.getX()+k][ll.getY()] = null;
					}
				} else { // vertical
					for (int k = 0; k < shp.length(); k++) {
						occupied[ll.getX()][ll.getY()+k] = null;
					}
				}
				ships.remove(j);
			} else {
				j++;
			}
		}

		Ship s = new Ship(shipLength, p, horiz, name);
		if (horiz) {
			for (int i = 0; i < shipLength; i++) {
				occupied[x+i][y] = s;
			}
		} else {
			for (int i = 0; i < shipLength; i++) {
				occupied[x][y+i] = s;
			}
		}
		ships.add(s);
		return true;
	}

	public boolean areAllShipsPlaced() {
		return (ships.size() == 5);
	}
}
