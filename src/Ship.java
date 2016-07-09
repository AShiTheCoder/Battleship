public class Ship {
	private Point lowerLeft; // lower left coordinate of ship
	private boolean horiz;   // true if horizontal, false if vertical
	private int shipLength;  // length of ship
	private String name;     // name of ship
	private boolean[] hit;   // which coordinates have been hit

	public Ship(int shipLength, Point lowerLeft, boolean horiz, String name) {
		this.shipLength = shipLength;
		this.lowerLeft = lowerLeft;
		this.name = name;
		this.horiz = horiz;
		hit = new boolean[shipLength];  // initializes to all false
	}

	public Point getLowerLeft() {
		return lowerLeft;
	}

	public int length() {
		return shipLength;
	}

	public boolean getDirection() {
		return horiz;
	}

	public boolean[] getHits() {
		return hit;
	}

	// YOU WRITE THIS
	public void hit(Point p) {
		int dist = horiz ? p.getX()-lowerLeft.getX() : p.getY() - lowerLeft.getY();
		hit[dist] = true;
	}

	public boolean contains(Point p) {
		if (horiz) {
			return (p.getY() == lowerLeft.getY()) &&
					Utilities.between(p.getX(), lowerLeft.getX(), lowerLeft.getX()+shipLength-1);
		} else {
			return (p.getX() == lowerLeft.getX()) &&
					Utilities.between(p.getY(), lowerLeft.getY(), lowerLeft.getY()+shipLength-1);
		}
	}

	public boolean isDead() {
		for (int i = 0; i < shipLength; i++) {
			if (!hit[i]) return false;
		}
		return true;
	}

	public String displayChar() {
		return name.substring(0,1);
	}

	// Is this a good test for equality?
	public boolean equals(Ship other) {
		return lowerLeft.equals(other.lowerLeft);
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name + " " + lowerLeft + " (len = " + shipLength + ")";
	}
}