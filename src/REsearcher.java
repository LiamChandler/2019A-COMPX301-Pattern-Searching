//	Liam Chandler	ID:	1286559
//	Daniel Bartley  ID: 1331132

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class REsearcher
{
	static State root = new State("",0);

	public static void main(String[] args)
	{
		if (args.length == 1)
		{
			State[] states;
			List<String[]> regexp = new LinkedList<>();     // List of component parts of states.
			int maxSize = 0;                                // Maximum state number.
			BufferedReader br;
			try
			{
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				String input = in.readLine();       // Getting first state
				String[] Tokens;
				while (input.length() > 0)
				{
					Tokens = input.split(",");                  // Splitting state into component parts.
					if (Integer.valueOf(Tokens[0]) > maxSize)
						maxSize = Integer.valueOf(Tokens[0]);       // Finding the maximum state number for the array.
					regexp.add(Tokens);         // Adding state to list.
					input = in.readLine();      // Getting next state.
				}

				states = new State[maxSize];        // Creating an array of all states.
				for (int i = 0; i < maxSize; i++)
					states[i] = new State("", i + 1);      // Populating the array.
				root.s1 = states[Integer.valueOf(regexp.get(0)[0]) - 1];  // Creating the first state.

				State tmp;
				for (String[] s : regexp)    // Adding data from regexp list to array and building.
				{
					tmp = states[Integer.valueOf(s[0]) - 1];  // Creating new state.
					tmp.content = s[1];
					if (Integer.valueOf(s[2]) != -1)
						tmp.s1 = states[Integer.valueOf(s[2]) - 1];       // Add s1 child if it exists.
					if (Integer.valueOf(s[3]) != -1)
						tmp.s2 = states[Integer.valueOf(s[3]) - 1];       // Add s2 child if it exists otherwise set it to s1.
					else
						tmp.s2 = tmp.s1;
				}

				br = new BufferedReader(new FileReader(args[0]));
				String s = br.readLine();       // Read first line of the file.
				while (s != null)               // Run until there are no more lines.
				{
					for (int i = 0; i < s.length(); i++)        // Checks that the line still has characters.
					{
						if (FSM.check(s.subSequence(i, s.length()).toString(), root.s1, 0))
						{
							System.out.println(s);      // If a match is found print the line.
							break;
						}
					}
					s = br.readLine();      // Read next line.
				}
				br.close();
			} catch (FileNotFoundException fe)
			{
				System.err.println("File not found");
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
			System.out.println("Please give the name of the file you want to search.");
	}
}

class FSM
{
	FSM()
	{
	}
	static boolean check(String line,State currState,int pointer)
	{
		if (pointer >= line.length())   // If the line has ended before the expression it does not match.
			return false;
		String checking = String.valueOf(line.charAt(pointer));     // The character that must match the contents of the state.
		if (currState.content.length() == 0)        // An empty state is either a branching state or the end of the expression.
		{
			if (currState.s1 == null)   // End of the expression
				return true;
			return check(line, currState.s1,pointer) || check(line, currState.s2,pointer);      // Check the children of branching state.
		}
		if (currState.content.contains(checking) || currState.content.contains(".."))
		{
			if (currState.content.length() > 1 && currState.content.contains("^^"))     // If the state is checking for characters not to match search the list without '^' characters.
			{
				pointer++;
				if (currState.content.subSequence(2, currState.content.length()).toString().contains(checking))
					return false;           // If the character is in the list and doesn't match the '^' characters, its does not match.
			}
			pointer++;
			if (currState.s1 == null)   // End of the expression
				return true;
			if (currState.s1 == currState.s2)       // If both child states are the same don't recurse on both of them.
				return check(line,currState.s1,pointer);
			return check(line,currState.s1,pointer) || check(line,currState.s2,pointer);       // if character matches the contents of the state or the state is a wildcard then it matches the expression.
		}
		return false;
	}
}
