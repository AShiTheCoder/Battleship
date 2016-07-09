public class Player {
	// Grid on which the player places ships
	Grid myGrid;

	public Player(Grid grid) {
		myGrid = grid;
	}

	public Grid getGrid() {
		return myGrid;
	}

	public boolean hasShips() {
		return !myGrid.gameOver();
	}
}