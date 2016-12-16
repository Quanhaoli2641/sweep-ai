package minesweeper;

import java.util.HashMap;
import java.util.Map;

public class HillClimbingMineAI {

	Minesweeper minesweeper;
	MineReader fieldReader;

	int threshhold = 2;
	int coordinate[] = new int[2];

	boolean fieldChanged = false;

	public int numGuesses;
	public int lastNumGuesses;
	public int numIterations;

	public int flagEvents;
	public int clearEvents;

	long startTime;
	long endTime;

	public HillClimbingMineAI(Minesweeper minesweeper){
		this.minesweeper = minesweeper;
		fieldReader = new MineReader(this.minesweeper);
	}

	public void initialize() {
		endTime = startTime;
		fieldReader.init();
		startTime = System.currentTimeMillis();
		digRandom(false);
		numGuesses = 0;
		lastNumGuesses = 0;
		numIterations = 0;
	}

	public void mainLoop() {
		while(!fieldReader.won && !fieldReader.dead) {
			System.out.println("Iterating: Field Change: " + fieldChanged);
			processEntireField();
		}
		fieldReader.updateField();
		System.out.print("Status: ");
		if(fieldReader.dead) System.out.println("Defeat");
		else System.out.println("Victory");
		System.out.println("Stats: " + numIterations + " iterations with " + numGuesses + " guesses");
		System.out.println("Time: " + (double)(endTime - startTime) / 1000.0 + " seconds");
	}

	public void processEntireField() {
		flagEvents = 0;
		clearEvents = 0;
		numIterations++;

		int[][] ticketArray = new int[fieldReader.columns + 2][fieldReader.rows + 2];

		if(fieldChanged) {
			fieldChanged = false;
			System.out.println("Processing Field...");
			processField();
		}
		else {
			fieldReader.updateField();
			System.out.println("Processing Field...");
			processField();

			if(!fieldChanged && !fieldReader.dead && !fieldReader.won) {
				if(!makeMove(ticketArray))
					digRandom(true);
			}
		}
		lastNumGuesses = numGuesses - lastNumGuesses;
		System.out.println("Iteration " + numIterations + ":");
		System.out.println(flagEvents + " flag events | " + clearEvents + " clear events | " + lastNumGuesses + " guesses");
	}

	public void processField() {
		for(int column = 1; column < fieldReader.columns + 1; column++) {
			for(int row = 1; row < fieldReader.rows + 1; row++) {
				processTile(column,row);
			}
		}
	}

	public void processTile(int x, int y) {
		int number = fieldReader.field[x][y];

		if(number > 0) {
			if(numBombsNear(x,y) >= number && numFreshSquaresNear(x,y) > 0){
				clearSurrounding(x,y);
			}
			else if ((numFreshSquaresNear(x,y) + numBombsNear(x,y)) == number && numBombsNear(x,y) != number){
				flagSurrounding(x,y);
			}
		}
	}

	public void clearSurrounding(int x, int y) {
		clearEvents++;

		for(int row = -1; row < 2; row++) {
			for (int column = -1; column < 2; column++) {
				if(fieldReader.field[x + column][y + row] == 0) dig(x + column,y + row);
			}
		}
	}

	public void flagSurrounding(int x, int y) {
		flagEvents++;

		for(int row = -1; row < 2; row++) {
			for (int column = -1; column < 2; column++) {
				if(fieldReader.field[x+column][y+row] == 0) flag(x+column,y+row);
			}
		}
	}

	public int numBombsNear(int x, int y) {
		int n = 0;

		for(int row = -1; row < 2; row++) {
			for (int column = -1; column < 2; column++) {
				if(fieldReader.field[x + column][y + row] == -2) n++;
			}
		}
		return n;
	}

	public int numFreshSquaresNear(int x, int y) {
		int n = 0;

		for(int row = -1; row < 2; row++) {
			for (int column = -1; column < 2; column++) {
				try{
					if(fieldReader.field[x + column][y + row] == 0) n++;
				} catch(Exception e){}
			}
		}
		return n;
	}

	public void dig(int x, int y) {
		if(fieldReader.field[x][y] == 0) fieldReader.click(x,y,false);
		fieldChanged = true;
		fieldReader.field[x][y] = -3;
		endTime = System.currentTimeMillis();
	}

	public void flag(int x, int y) {
		fieldReader.click(x,y,true);
		fieldReader.field[x][y] = -2;
		fieldChanged = true;
	}

	public Boolean digRandom(Boolean makeGuessOnNeighbor) {
		if(fieldReader.numEmpty < 2) {
			System.out.println("Guessing Completely Randomly");
			int xToDig = (int)(Math.random() * fieldReader.columns);
			int yToDig = (int)(Math.random() * fieldReader.rows);
			dig(xToDig,yToDig);
			return true;
		}
		else {
			numGuesses++;
			System.out.println("Guessing...");
			boolean dug = false;
			do{
				for(int column = 1; column < fieldReader.columns; column++) {
					for(int row = 1; row < fieldReader.rows; row++) {
						boolean numberNear = numberNear(column,row);
						if(!makeGuessOnNeighbor)numberNear =! numberNear;
						if(fieldReader.field[column][row] == 0 && numberNear) {
							dig(column,row);
							dug = true;
							row = 99999;
							column = 99999;
						}
					}
				}
			} while(!dug);
			return dug;
		}
	}

	public boolean numberNear(int x, int y) {
		for(int row = -1; row < 2; row++) {
			for (int column = -1; column < 2; column++) {
				if(fieldReader.field[x + column][y + row] > 0) return true;
			}
		}
		return false;
	}

	public void digRandomSurrounding(int x, int y) {
		int xOffset;
		int yOffset;

		do{
			xOffset = (int)(Math.random() * 3) - 2;
		} while(xOffset == 0);

		do {
			yOffset = (int)(Math.random() * 3) - 2;
		} while(yOffset == 0);

		dig(x + xOffset + 1,y + yOffset + 1);
	}

	public void ticketSurrounding(int ticketArray[][], int x, int y) {
		for(int row = -1; row < 2; row++) {
			for (int column = -1; column < 2; column++) {
				if (x + row >= 0 && y + column >= 0 && x + row < fieldReader.columns + 2 && y + column < fieldReader.rows + 2) {
					if (ticketArray[x + row][y + column] == Integer.MAX_VALUE && fieldReader.field[x + row][y + column] == 0) {
						ticketArray[x + row][y + column] = 0;
					}
					if (fieldReader.field[x + row][y + column]  == 0) {
						ticketArray[x + row][y + column] += 1;
					}
				}
			}
		}
	}

	public void fillMatrix(int ticketArray[][]){
		for(int x = 0; x < ticketArray.length; x++){
			for (int y = 0; y < ticketArray[x].length; y++) {
				ticketArray[x][y] = Integer.MAX_VALUE;
			}
		}
		for(int x = 0; x < ticketArray.length; x++){
			for (int y = 0; y < ticketArray[x].length; y++) {
				if (fieldReader.field[x][y] > 0) ticketSurrounding(ticketArray,x,y);
			}
		}
	}

	public int[] findMin (int ticketArray[][]) {
		int minTicket = Integer.MAX_VALUE;
		for(int x = 0; x < ticketArray.length; x++){
			for (int y = 0; y < ticketArray[x].length; y++) {
				if(ticketArray[x][y] < minTicket){
					minTicket = ticketArray[x][y];
					coordinate[0] = x;
					coordinate[1] = y;
				}
			}
		}
		if(minTicket > threshhold){
			coordinate[0] = -1;
			coordinate[1] = -1;
		}
		return coordinate;
	}

	public int[] findMax (int ticketArray[][]) {
		int maxTicket = Integer.MIN_VALUE;
		for(int x = 0; x < ticketArray.length; x++){
			for (int y = 0; y < ticketArray[x].length; y++) {
				if(ticketArray[x][y] > maxTicket){
					maxTicket = ticketArray[x][y];
					coordinate[0] = x;
					coordinate[1] = y;
				}
			}
		}
		if(maxTicket <= threshhold || maxTicket == Integer.MAX_VALUE){
			coordinate[0] = -1;
			coordinate[1] = -1;
		}
		return coordinate;
	}

	public boolean makeMove(int ticketArray[][]) {
		fillMatrix(ticketArray);
		coordinate = findMin(ticketArray);
		if(coordinate[0] >= 0 && coordinate[1] >= 0){
			dig(coordinate[0],coordinate[1]);
			clearEvents++;
			return true;
		}
		return false;
	}

	public int getOccurence(int ticketArray[][], int minValue){
		int occurence = 0;
		for(int x = 0; x < ticketArray.length; x++){
			for (int y = 0; y < ticketArray[x].length; y++) {
				if(ticketArray[x][y] == minValue)
					occurence++;
			}
		}
		return occurence;
	}

}