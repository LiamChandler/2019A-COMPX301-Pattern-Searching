/*
Liam Chandler	ID:	1286559
Daniel Bartley  ID: 1331132

Our grammar:
E   ->  E|E
E   ->  T
E   ->  TE

T   ->  ^[]
T   ->  []
T   ->  F
T   ->  F*
T   ->  F?

F   ->  \v
F   ->  (E)
F   ->  .
F   ->  v
*/
public class REcompiler
{
	private static String RegExp;   // Variable for the given regexp.
	private static int pointer = 0; // Pointer for where in the regexp we are.
	static int state = 1;           // The current state number we are up to.

	public static void main(String[] args)
	{
		if (args.length == 1)                               // Check how many args were given.
		{                                                   // If only one was interpret it as a regexp.
			State output;
			RegExp = args[0];
			if ((output = Expression(true)) == null)
				System.out.println("Invalid expression");   // Input isn't valid regular expression
			else
			{
				output.condense();                          // Remove extra states
				System.out.println(output.Print());         // Print output to console
			}
		} else                                              // Else report incorrect usage.
			System.out.println("Please give one valid regexp regexp");
	}

	// Make an expression from the current point in the regexp.
	private static State Expression(boolean makeMore)
	{
		State output = Term();
		if (output != null) // Check if there was a result.
			if (RegExp.length() > pointer && RegExp.charAt(pointer) == '|') // Check if we are up to a | char
			{   // If so make a branching state that links to the previous object and make the state for the other half.
				pointer++;
				State tmp = new State("");
				tmp.s1 = output;
				tmp.s2 = Expression(false); // Set S2 to be the next and only the next expression.
				output = tmp;
				if (output.s2 == null) // If an expression couldn't be made then return null
					return null;
				// Create a blank exit state and link both of the expressions to that state
				tmp = new State("");
				output.s1.addS1(tmp);
				output.s2.addS1(tmp);
			}
			if (makeMore && RegExp.length() > pointer)  // Check if we are meant to make more than one expression and that the current char exists and is valid
				if (isVocab(RegExp.charAt(pointer)))
				{
					State tmp = Expression(true);
					if (tmp == null)
						output = null;
					output.addS1(tmp);
				} else if (RegExp.charAt(pointer) != ')')
					output = null;
		return output;
	}

	// Make a term from the current point in the regexp.
	private static State Term()
	{
		State output;
		try
		{
			if (RegExp.charAt(pointer) == '^' && RegExp.charAt(pointer + 1) == '[')     // Checks for beginning of a list of excluded characters.
			{
				pointer += 3;       // Moves over "^[" and the first character since list cannot be empty.
				StringBuilder s = new StringBuilder("^^"+RegExp.charAt(pointer - 1));   // Creates beginning of the list with first character and "^^" flag to indicate exclusion.

				while (RegExp.charAt(pointer) != ']')       // Search for the end of the list.
				{
					s.append(RegExp.charAt(pointer));       // Add next character to the list.
					pointer++;
				}
				pointer++;
				output = new State(s.toString());       // Create state with the list as its content.

				if(RegExp.length() > pointer)
					if (RegExp.charAt(pointer) == '*')      // Checks for repeat once or more character.
					{
						pointer++;
						output.addS2(output);               // Allows for repeating.
					} else if (RegExp.charAt(pointer) == '?')       // Checks for repeat once or none character.
					{
						pointer++;
						State start = new State("");        // Creates two new states that can bypass the term.
						start.s1 = output;
						start.s2 = new State("");
						start.s1.addS1(start.s2);
						output = start;
					}
			} else if (RegExp.charAt(pointer) == '[')       // Checks for beginning of a list of characters.
			{
				pointer += 2;    // Moves over "[" and the first character since list cannot be empty.
				StringBuilder s = new StringBuilder(String.valueOf(RegExp.charAt(pointer - 1)));        // Creates beginning of list with first character.

				while (RegExp.charAt(pointer) != ']')       // Search for the end of the list.
				{
					s.append(RegExp.charAt(pointer));       // Add next character to the list.
					pointer++;
				}
				pointer++;
				output = new State(s.toString());       // Create state with the list as its content.
				if (RegExp.length() > pointer)
					if (RegExp.charAt(pointer) == '*')      // Checks for repeat once or more character.
					{
						pointer++;
						output.addS2(output);               // Allows for repeating.
					} else if (RegExp.charAt(pointer) == '?')       // Checks for repeat once or none character.
					{
						pointer++;
						State start = new State("");        // Creates two new states that can bypass the term.
						start.s1 = output;
						start.s2 = new State("");
						start.s1.addS1(start.s2);
						output = start;
					}

			} else if ((output = Factor()) != null)         // If term is not a list then create a new factor.
			{
				if (RegExp.length() > pointer)
				{
					if (RegExp.charAt(pointer) == '*')      // Checks for repeat once or more character.
					{
						pointer++;
						output.addS2(output);               // Allows for repeating.
						output.addS1(new State(""));        // Create next state.
					} else if (RegExp.charAt(pointer) == '?')       // Checks for repeat once or none character.
					{
						pointer++;
						State start = new State("");        // Creates two new states that can bypass the term.
						start.s1 = output;
						start.s2 = new State("");
						start.s1.addS1(start.s2);
						output = start;
					}
				}
			}
		} catch (Exception e)
		{
			output = null;
		}
		return output;      // Returns the created state machine.
	}


	// Make a factor from the current point in the regexp.
	private static State Factor()
	{
		State output = null;
		if (RegExp.length() > pointer)  // If there is more regexp
		{
			if (RegExp.charAt(pointer) == '\\') // Check if the current char is a \.
			{   // If so then skip that char and add the next one unconditionally.
				pointer++;
				output = new State(String.valueOf(RegExp.charAt(pointer)));
				pointer++;
			} else if (RegExp.charAt(pointer) == '(') // Check if the current char is a (.
			{   // Skip the ( then make an expression.
				pointer++;
				output = Expression(true);
				if (RegExp.charAt(pointer) == ')')  // If the next char after the expression is a ) then we move the pointer over it.
					pointer++;
				else    // Else the regexp is incorrect and we return a null.
					output = null;
			} else if (RegExp.charAt(pointer) == '.') // Check if the current char is a ".".
			{   // Create a state with a . but can be differentiated from "\."
				output = new State("..");
				pointer++;
			} else if (isVocab(RegExp.charAt(pointer))) // Check if the current char is not a special char.
			{
				output = new State(String.valueOf(RegExp.charAt(pointer)));
				pointer++;
			}
		}
		return output;
	}

	// Basic check to see if the given char is a special char
	private static boolean isVocab(Character c)
	{
		return (c != '|' && c != ')'&& c != ']');
	}
}

// Allows the creation of a FSM
class State
{
	private int stateNum;
	State s1, s2;
	String content;
	private boolean Printed = false;

	// Make a state with the given content and the current state number
	State(String content)
	{
		this.stateNum = REcompiler.state;
		this.content = content;
		REcompiler.state++;
	}

	// Make a state with the given content and the given state number
	State(String content, int stateNum)
	{
		this.stateNum = stateNum;
		this.content = content;
	}

	// Recursively remove any state that has no content and only one sub-state
	void condense()
	{
		if (s1 != null)
		{
			// Check if this state has a child that needs to be removed
			if (s1.content.isEmpty() && s1.s2 == null && s1.s1 != null)
			{   // If so then remove it and recall condense on this object.
				s1 = s1.s1;
				this.condense();
			} else  // Else move to the child.
				s1.condense();
		}
		// Check that S2 exists and that it is not a loop
		if (s2 != null && s2 != this)
		{
			// Check if this state has a child that needs to be removed
			if (s2.content.isEmpty() && s2.s2 == null && s2.s1 != null)
			{   // If so then remove it and recall condense on this object.
				s2 = s2.s1;
				this.condense();
			} else  // Else move to the child.
				s2.condense();
		}
	}

	//  Loop until the end is found and set the S1 state to be the given state
	void addS1(State s)
	{
		State t = this;
		while (t.s1 != null)
		{
			t = t.s1;
		}
		t.s1 = s;
	}

	//  Loop until the end is found and set the S2 state to be the given state
	void addS2(State s)
	{
		State t = this;
		while (t.s1 != null)
		{
			t = t.s1;
		}
		t.s2 = s;
	}

	// Return the information that defines this state and any sub-state that hasn't been printed.
	String Print()
	{
		if (Printed)
			return "";
		else
		{
			int S1 = -1, S2 = -1;
			String c = "";
			Printed = true;
			if (s1 != null)
			{
				S1 = s1.stateNum;
				c += s1.Print();
			}
			if (s2 != null)
			{
				S2 = s2.stateNum;
				c += s2.Print();
			}
			return stateNum + "," + content + "," + S1 + "," + S2 + "\n" + c;
		}
	}
}
