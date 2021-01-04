import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;

public class US_elections {


	public static int solution(int num_states, int[] delegates, int[] votes_Biden, int[] votes_Trump, int[] votes_Undecided){

		if(num_states == 0) {
			return -1;
		}

		int delegatesTrump = 0;
		int delegatesBiden = 0;

		int winState = 0;
		int totalDelegates = 0;

		for(int i=0; i<delegates.length; i++) {
			winState += delegates[i];
			totalDelegates += delegates[i];

		}

		winState = (winState/2) + 1;										//Minimum delegates required to win election


		int[] votesNeeded = calcVotesNeeded(num_states, votes_Biden, votes_Trump, votes_Undecided);

		ArrayList<Integer> statesNotWonYet = new ArrayList<Integer>();



		for(int i=0; i<votesNeeded.length; i++) {
			boolean removed = false;

			//If Biden has higher or equal than the votes needed to win, he gets the delegates

			if(votesNeeded[i] <= votes_Biden[i]) {
				delegatesBiden += delegates[i];
				//System.out.println("hel" + delegatesBiden);
				removed = true;

			}

			//If Trump has higher or equal than the votes needed to win, he gets the delegates
			//Or, if it's a tie (Trump has one less than what's needed to win), he gets the delegates

			if(votesNeeded[i] <= votes_Trump[i] || (votes_Biden[i] + votes_Undecided[i]) <= votes_Trump[i]) {
				delegatesTrump += delegates[i];
				//System.out.println(delegatesTrump);
				removed = true;

			}

			//Trump won the election if he has more or equal delegates than the minimum required to win.
			if(delegatesTrump >= winState) {
				//System.out.println("Hi");
				return -1;
			}

			//Biden wins if he has more or equal delegates than the minimum required to win.
			if(delegatesBiden >= winState) {
				return 0;
			}

			if(delegatesTrump == (totalDelegates-delegatesTrump)) {

				return -1;
			}

			if(!removed) {
				statesNotWonYet.add(i);
			}

		}


		int[] votesRemainingForBiden = calcVotesRemainingBiden(num_states, statesNotWonYet, votesNeeded, votes_Biden);


		int minVotes = findMinVotes(statesNotWonYet, delegates, votesRemainingForBiden, delegatesBiden, winState);


		return minVotes;

	}

	/**
	 * This helper method implements a knapsack to retrieve the minimum amount of votes required for Biden to win.
	 * The first row is initialized with infinity and the first column with 0s. It is a slight variant of the usual knapsack.
	 * If our current weight is bigger than the maximum weight, we do the usual and set the value to M[i-1,j]. Otherwise,
	 * we will take the minimum between M[i-1,j] and M[i-1, w - wi]. This allows us to retrieve the minimum value compared
	 * to the knapsack seen in class that gives us the maximal value.
	 * @param statesNotWonYet
	 * @param delegates
	 * @param votesRemaining
	 * @param delegatesBiden
	 * @param cap
	 * @return votes
	 */

	private static int findMinVotes(ArrayList<Integer> statesNotWonYet, int[] delegates, int[] votesRemaining, int delegatesBiden, int cap) {


		int votes = Integer.MAX_VALUE;
		int rows = statesNotWonYet.size()+1;
		int columns = 0;
		int index = statesNotWonYet.size()-1;
		boolean reduce = false;
		int stateIndex = 0;

		//If there's only one state, we return the votes needed for it
		if(statesNotWonYet.size() == 1) {
			return votesRemaining[statesNotWonYet.get(0)];
		}

		//The amount of columns will be the total of delegates
		for(int i=0; i<statesNotWonYet.size(); i++) {
			columns += delegates[statesNotWonYet.get(i)];
		}		
		

		int[][] knapsack = new int[rows][columns+1];

		//We fill the first row with infinity
		for(int i=1; i<columns+1; i++) {
			knapsack[0][i] = Integer.MAX_VALUE;
		}

		knapsack[0][0] = 0;

		//Here we fill up the knapsack using the methodology described in previously
		for(int i=1; i<rows; i++) {
			knapsack[i][0] = 0;

			for(int j=1; j<columns+1; j++) {


				if(delegates[statesNotWonYet.get(index)] <= j) {

					//Both are infinity, we keep infinity
					if(knapsack[i-1][j-delegates[statesNotWonYet.get(index)]] == Integer.MAX_VALUE && knapsack[i-1][j] == Integer.MAX_VALUE) {

						knapsack[i][j] = Integer.MAX_VALUE;


					} //The left side is infinity, so the top is smaller and we keep it
					else if((knapsack[i-1][j-delegates[statesNotWonYet.get(index)]]) > knapsack[i-1][j] && knapsack[i-1][j-delegates[statesNotWonYet.get(index)]] == Integer.MAX_VALUE) {

						knapsack[i][j] = knapsack[i-1][j];

					}//The left side is bigger, we keep the top value
					else if((knapsack[i-1][j-delegates[statesNotWonYet.get(index)]] + votesRemaining[statesNotWonYet.get(index)]) > knapsack[i-1][j] && knapsack[i-1][j-delegates[statesNotWonYet.get(index)]] != Integer.MAX_VALUE) {

						knapsack[i][j] = knapsack[i-1][j];

					} //The top is bigger, we keep the left value
					else if((knapsack[i-1][j-delegates[statesNotWonYet.get(index)]] + votesRemaining[statesNotWonYet.get(index)]) <= knapsack[i-1][j]) {

						knapsack[i][j] = knapsack[i-1][j-delegates[statesNotWonYet.get(index)]] + votesRemaining[statesNotWonYet.get(index)];

					} 

				} else {
					knapsack[i][j] = knapsack[i-1][j];
				}
			}
			index--;
		}


		//We go through all the values in the last row till we get to the last column. 
		//The smallest value that also corresponds to a correct 
		//combination of votes required will be the minimum amount of votes needed.

		int tmpRows = rows-1;
		int tmpCols = 1;
		while(true) {
			reduce = false;

			if(tmpCols == columns+1) {
				break;
			}

			if(knapsack[tmpRows][tmpCols] != Integer.MAX_VALUE) {	

				if(votes > knapsack[tmpRows][tmpCols]) {
					
					//If the value right on top isn't the same, it means that value
					//is a part of the solution, we check if it gives a valid combination.
					if(knapsack[tmpRows][tmpCols] != knapsack[tmpRows-1][tmpCols]) {
						if(validVotes(stateIndex, knapsack, tmpRows, tmpCols, knapsack[tmpRows][tmpCols], statesNotWonYet, votesRemaining, delegates, cap, delegatesBiden)) {

							votes = knapsack[tmpRows][tmpCols];
						}

						tmpRows = rows-1;
						stateIndex = 0;

					} else {
						//If the upper row has the same value, the current state is not a part
						//of the solution, so we go to the upper row and we increase the state
						//index so we don't consider this state.
						reduce = true;
						tmpRows--;
						stateIndex++;
					}
				}
			} 

			if(!reduce) {
				tmpCols++;
			}
		}

		return votes;

	}

	/**
	 * This method checks if the amount of votes given as input corresponds to a valid combination of our values (votes
	 * required to win). I go through the values one by one and check if their combination gives a total of 0 
	 * which would mean it is a valid solution.
	 * @param stateIndex
	 * @param knapsack
	 * @param row
	 * @param col
	 * @param totalVotes
	 * @param statesNotWonYet
	 * @param votesRemaining
	 * @param delegates
	 * @param cap
	 * @param delegatesBiden
	 * @return valid
	 */

	private static boolean validVotes(int stateIndex, int[][] knapsack, int row, int col, int totalVotes, ArrayList<Integer> statesNotWonYet, int[] votesRemaining, int[] delegates, int cap, int delegatesBiden) {
		boolean valid = false;
		int nbDelegates = delegatesBiden;
		
		for(int i=stateIndex; i<statesNotWonYet.size(); i++) {
			int index = statesNotWonYet.get(i);
			col = col - delegates[index];
			row--;
			
			totalVotes = totalVotes - votesRemaining[index];
			
			if(col < 0 || row < 0) {
				break;
			}
				
			if(row-1 >= 0) {
				if(knapsack[row][col] != 0) {
					//If the top value is the same, it means this state
					//is not a part of the solution, go to the next row.
					while(knapsack[row][col] == knapsack[row-1][col]) {
						row--;
						i++;
					}
				}
			}
				//If the value doesn't match, it is not a valid solution.
				if(knapsack[row][col] != totalVotes) {
					break;
				} else {
					
					nbDelegates += delegates[index];
					
					//Once the total is 0 and we have enough delegates, 
					//it's valid solution.
					if(totalVotes == 0 && nbDelegates >= cap) {
						valid = true;
						break;
					}
				}
		}

		return valid;
	}

	/**
	 * This helper method uses the amount of votes needed to win and the votes that Biden already has
	 * to calculate how much he has left to win.
	 * @param num_states
	 * @param undecided
	 * @param votesNeeded
	 * @param votes_Biden
	 * @return votesRemaining
	 */
	private static int[] calcVotesRemainingBiden(int num_states, ArrayList<Integer> undecided, int[] votesNeeded, int[] votes_Biden) {

		int[] votesRemaining = new int[num_states];


		for(int i=0; i<undecided.size(); i++) {

			votesRemaining[undecided.get(i)] = votesNeeded[undecided.get(i)] - votes_Biden[undecided.get(i)];
		}

		return votesRemaining;
	}

	/**
	 * This goes through all the states and calculates how many votes are required to win that specific state.
	 * @param num_states
	 * @param bidenVotes
	 * @param trumpVotes
	 * @param undecidedVotes
	 * @return votesNeeded
	 */
	private static int[] calcVotesNeeded(int num_states, int[] bidenVotes, int[] trumpVotes, int[] undecidedVotes) {

		int[] votesNeeded = new int[num_states];

		for(int i=0; i<votesNeeded.length; i++) {

			int votesToWinDeleguate = bidenVotes[i] + trumpVotes[i] + undecidedVotes[i];
			votesNeeded[i] = (votesToWinDeleguate/2) + 1;

		}

		return votesNeeded;

	}


	public static void main(String[] args) {
		 try {
				String path = args[0];
	      File myFile = new File(path);
	      Scanner sc = new Scanner(myFile);
	      int num_states = sc.nextInt();
	      int[] delegates = new int[num_states];
	      int[] votes_Biden = new int[num_states];
	      int[] votes_Trump = new int[num_states];
	 			int[] votes_Undecided = new int[num_states];	
	      for (int state = 0; state<num_states; state++){
				  delegates[state] =sc.nextInt();
					votes_Biden[state] = sc.nextInt();
					votes_Trump[state] = sc.nextInt();
					votes_Undecided[state] = sc.nextInt();
	      }
	      sc.close();
	      int answer = solution(num_states, delegates, votes_Biden, votes_Trump, votes_Undecided);
	      	System.out.println(answer);
	    	} catch (FileNotFoundException e) {
	      	System.out.println("An error occurred.");
	      	e.printStackTrace();
	    	}
	  	}


}