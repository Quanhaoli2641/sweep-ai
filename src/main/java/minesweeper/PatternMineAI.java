package minesweeper;

import java.util.ArrayList;
import java.util.Arrays;
import java.lang.Math.*;
import java.util.Collections;

public class PatternMineAI {

	Minesweeper minesweeper;
	MineReader fieldReader;

	boolean fieldChanged = false;

	public int numGuesses;
	public int lastNumGuesses;
	public int numIterations;

	public int flagEvents;
	public int clearEvents;

	long startTime;
	long endTime;

	public PatternMineAI(Minesweeper minesweeper){
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

		if(fieldChanged) {
			fieldChanged = false;
			System.out.println("Processing Field...");
			processField();
		}
		else {
			fieldReader.updateField();
			System.out.println("Processing Field...");

			if(!processField()){
				doAdvancedSolving();
			}

			if(!fieldChanged && !fieldReader.dead && !fieldReader.won) {
				digRandom(true);
			}
		}
		lastNumGuesses = numGuesses - lastNumGuesses;
		System.out.println("Iteration " + numIterations + ":");
		System.out.println(flagEvents + " flag events | " + clearEvents + " clear events | " + lastNumGuesses + " guesses");
	}

	public boolean processField() {
		int counter = 0;
		for(int column = 1; column < fieldReader.columns + 1; column++) {
			for(int row = 1; row < fieldReader.rows + 1; row++) {
				if(processTile(column,row)) counter++;
			}
		}
		return (counter > 0);
	}

	public boolean processTile(int x, int y) {
		int number = fieldReader.field[x][y];

		if(number > 0) {
			if(numBombsNear(x,y) >= number && numFreshSquaresNear(x,y) > 0){
				clearSurrounding(x,y);
				return true;
			}
			else if ((numFreshSquaresNear(x,y) + numBombsNear(x,y)) == number && numBombsNear(x,y) != number){
				flagSurrounding(x,y);
				return true;
			}
		}
		return false;
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
				if(fieldReader.field[x + column][y + row] == 0) flag(x + column,y + row);
			}
		}
	}

	public int numBombsNear(int x, int y) {
		int n = 0;
		for(int row = -1; row < 2; row++) {
			for (int column = -1; column < 2; column++) {
				try {
					if (fieldReader.field[x + column][y + row] == -2) n++;
				} catch(Exception e){
					continue;
				}
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
				} catch(Exception e){
					continue;
				}
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

	public boolean numberOneNear(int x, int y) {
		for(int row = -1; row < 2; row++) {
			for (int column = -1; column < 2; column++) {
				if(fieldReader.field[x + column][y + row] - numBombsNear(x + column,y + row) == 1) return true;
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

	public boolean freshAreNextToEachOther(int x, int y){
		ArrayList<Pair> freshList = new ArrayList<Pair>();
		for(int row = -1; row < 2; row++) {
			for (int column = -1; column < 2; column++) {
				if(fieldReader.field[x + column][y + row] == 0){
					freshList.add(new Pair(x + column, y + row));
				}
			}
		}
		if(Math.abs(freshList.get(0).getFirst() + freshList.get(0).getSecond() - freshList.get(1).getFirst() - freshList.get(1).getSecond()) == 1){
			return true;
		}
		return false;
	}

	public class Pair {
		private int first;
		private int second;

		public Pair(int first, int second) {
			this.first = first;
			this.second = second;
		}

		public void setFirst(int first) {
			this.first = first;
		}

		public void setSecond(int second) {
			this.second = second;
		}

		public int getFirst() {
			return first;
		}

		public int getSecond() {
			return second;
		}

	}

	public Pair getLocationOneNear(int x, int y){
		Pair location = new Pair(-1,-1);
		for(int row = -1; row < 2; row++) {
			for (int column = -1; column < 2; column++) {
				if(fieldReader.field[x + column][y + row] - numBombsNear(x + column,y + row) == 1 && row != 0 && column != 0){
					location.setFirst(x + column);
					location.setSecond(y + row);
				}
			}
		}
		return location;
	}

	public void fillFreshList(int x, int y, ArrayList<Pair> list){
		for(int row = -1; row < 2; row++) {
			for (int column = -1; column < 2; column++) {
				if(fieldReader.field[x + column][y + row] == 0){
					list.add(new Pair(x + column, y + row));
				}
			}
		}
	}

	public void fillNumList(int x, int y, ArrayList<Pair> list){
		for(int row = -1; row < 2; row++) {
			for (int column = -1; column < 2; column++) {
				if(fieldReader.field[x + column][y + row] > 0 && row != 0 && column != 0){
					list.add(new Pair(x + column, y + row));
				}
			}
		}
		for(int i = 0; i < list.size(); i++){
			if(Math.abs(list.get(i).first + list.get(i).second - x - y) != 1){
				list.remove(i);
				return;
			}
		}
	}

	public void clickAllExceptEdgeAndAdjecentToEdge(int x, int y){
		ArrayList<Pair> list = new ArrayList<Pair>();
		ArrayList<Pair> removeList = new ArrayList<Pair>();
		Pair location = getLocationOneNear(x,y);
		fillFreshList(x,y,list);
		fillFreshList(location.getFirst(),location.getSecond(),removeList);
		removeList.removeAll(list);
		for(int i = 0; i < removeList.size(); i++){
			dig(removeList.get(i).getFirst(),removeList.get(i).getSecond());
		}
		list.clear();
		removeList.clear();
	}

	public boolean doAdvancedSolving(){
		int counter = 0;
		for(int x = 1; x < fieldReader.columns + 1; x++){
			for(int y = 1; y < fieldReader.rows + 1; y++){
				if(solve(x,y)) counter++;
			}
		}
		if(counter > 0) return true;
		else return false;
	}

	private boolean solve(int x, int y){
		if (fieldReader.field[x][y] - numBombsNear(x,y) == 1){
			if (numFreshSquaresNear(x,y) == 2){
				firstTech(x,y);
				return true;
			}
		}
		else if (fieldReader.field[x][y] - numBombsNear(x,y) == 2){
			if (numFreshSquaresNear(x,y) == 3){
				secondTech(x,y);
				return true;
			}
		}
		return false;
	}

	private void firstTech(int x, int y){
		Pair oneNeighbor = getLocationOneNear(x,y);
		if (freshAreNextToEachOther(x,y)){
			if (numberOneNear(x,y)) {
				if (numFreshSquaresNear(oneNeighbor.getFirst(), oneNeighbor.getSecond()) > 2) {
					clickAllExceptEdgeAndAdjecentToEdge(x, y);
				}
			}
		}
	}

	private void secondTech(int x, int y){
		ArrayList<Pair> numList = new ArrayList<Pair>();
		fillNumList(x,y,numList);
		ArrayList<Pair> list = new ArrayList<Pair>();
		ArrayList<Pair> removeList = new ArrayList<Pair>();
		if (freshAreNextToEachOther(x,y)){
			try {
				if (fieldReader.field[numList.get(0).getFirst()][numList.get(0).getSecond()] - numBombsNear(numList.get(0).getFirst(), numList.get(0).getSecond()) == 1) {
					fillFreshList(x, y, list);
					fillFreshList(numList.get(0).getFirst(), numList.get(0).getSecond(), removeList);
					removeList.removeAll(list);
					for (int i = 0; i < removeList.size(); i++) {
						flag(removeList.get(i).getFirst(), removeList.get(i).getSecond());
					}
				} else if (fieldReader.field[numList.get(1).getFirst()][numList.get(1).getSecond()] - numBombsNear(numList.get(1).getFirst(), numList.get(1).getSecond()) == 1) {
					fillFreshList(x, y, list);
					fillFreshList(numList.get(1).getFirst(), numList.get(1).getSecond(), removeList);
					removeList.removeAll(list);
					for (int i = 0; i < removeList.size(); i++) {
						flag(removeList.get(i).getFirst(), removeList.get(i).getSecond());
					}
				}
			}catch (Exception e) {}
		}
		numList.clear();
		removeList.clear();
		list.clear();
	}

}