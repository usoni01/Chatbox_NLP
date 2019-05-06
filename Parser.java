//Authors: Chance Potter, Urja Soni
package nlp.parser;


import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.tokensregex.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.util.CoreMap;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.ArrayList; // import the ArrayList class
import java.sql.*;	// sql java import


public class Parser {
	
	static Connection c = null;

	public static void main(String[] args) throws IOException {
		
		// open database connection
		try {
	         Class.forName("oracle.jdbc.driver.OracleDriver");
	         c = DriverManager.getConnection("jdbc:sqlite:oscar-movie_imdb.sqlite");
	      } catch ( Exception e ) {
	         System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	         System.exit(0);
	      }
	      //System.out.println("Opened database successfully");

        // read input.txt from command line arguments
        if(!args[0].isEmpty() && !args[0].endsWith(".txt")) {
            System.err.println("Please provide the valid input .txt file");
            return;
        }

        String input = args[0];

        Properties props = new Properties();

        //list of annotators being used
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, regexner, relation");
        props.setProperty("ner.useSUTime", "false");

        props.setProperty("ner.combinationMode", "HIGH_RECALL");
        props.setProperty("tokenize.language", "English");



        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        File f = new File("parsed.txt");
        PrintWriter printwriter = new PrintWriter(f);

        //read text file line by line
        try (Stream<String> stream = Files.lines(Paths.get(input))) {

            stream.forEach(line -> {

                Annotation document = new Annotation(line);

                //set up environment properties to read .rules file which has custom rules.
                Env env = TokenSequencePattern.getNewEnv();
                env.setDefaultStringMatchFlags(NodePattern.CASE_INSENSITIVE);
                env.setDefaultStringPatternFlags(Pattern.CASE_INSENSITIVE);


                Set<String> categories = new HashSet<>();
                pipeline.annotate(document);

                List<CoreMap> sentences = document.get(SentencesAnnotation.class);
                CoreMapExpressionExtractor extractor = CoreMapExpressionExtractor.createExtractorFromFiles(env, "custom.rules");
                for (CoreMap sentence : sentences) {

                    printwriter.print("<QUESTION>" + sentence + "\n");
                    System.out.println("<QUESTION> \n"+ sentence);
                   // System.out.println(sentence);
                    String category = "";
                    // matches the rules defined in custom.rules file
                    List<MatchedExpression> matched = extractor.extractExpressions(sentence);
                    if(matched.size() > 0) {
                        //iterate over matched rules
                        for(MatchedExpression phrase : matched){
                            //get the category of matched rule
                            category = phrase.getValue().get().toString();
                            if (category != null && !category.isEmpty()) {
                                //add matched category to final set
                                addToSet(categories, category);
                            }
                        }
                    }

                    for (CoreLabel label : sentence.get(TokensAnnotation.class)) {
                        //keep looking for other categories.
                        category = label.category();
                        if (category != null && !category.isEmpty()) {
                            addToSet(categories, category);
                        }
                        // find named entity relations between different parts of sentence
                        String ne = label.get(NamedEntityTagAnnotation.class);
                        if (!"O".equalsIgnoreCase(ne)) {
                            addToSet(categories, ne);
                        }

                    }
                    
                    //System.out.println(categories);
                    int locationPersonTag = 0;
                    String finalResult = categorize(categories, locationPersonTag);

                    //write the output removing duplicates
                    //System.out.println(finalResult);	// Debug
                    printwriter.print(removeDuplicates("<CATEGORY>" + finalResult));

                    //write the parse tree
                    printwriter.print("\n");
                    printwriter.print("<PARSETREE>" + "\n");
                    Tree tree = sentence.get(TreeAnnotation.class);
                    TreePrint tp = new TreePrint("penn");
                    tp.printTree(tree, printwriter);

                    printwriter.print("\n");
                    treeTraversal(tree);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
			c.close();
			//System.out.println("Database connection closed.");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void treeTraversal(Tree t) {
    	ArrayList<String> tags = new ArrayList<String>();
    	for (Tree subt : t) {
    		//System.out.println(subt.label().value());	// Debug
    		tags.add(subt.label().value());
    	}
    	//System.out.println(tags);
    	if(tags.get(1).equals("SQ") || tags.get(1).equals("S")) {
    		yesNoQuestion(tags);
    	}
    	else {
    		whQuestions(tags);
    	}
    }
    
    public static void whQuestions(ArrayList<String> tags) {
    	//System.out.println("In Wh Questions function");	// Debug
    	//System.out.println(tags); 	// Debug
    	Statement statement= null;
    	boolean directed = false;
    	String year = "0";
    	boolean getyear = false;
    	boolean actor = false;
    	boolean actress = false;
    	boolean findMovie = false;
    	String select = null;
    	String from = null;
    	String where = null;
    	String movieName = null;
    	
    	for(int i=0; i<tags.size(); i++) {
    		if(tags.get(i).equals("directed")) {
    			directed = true;
    		}
    		if(tags.get(i).equals("CD")) {
    			year = tags.get(i+1);
    		}
    		if(tags.get(i).equals("NNP")) {
    			movieName = tags.get(i+1);
    		}
    		if(tags.get(i).equals("actor")) {
    			actor = true;
    		}
    		if(tags.get(i).equals("actress")) {
    			actress = true;
    		}
    		if(tags.get(i).equals("WRB")) {
    			getyear = true;
    		}
    		if(tags.get(i).equals("movie")) {
    			if(tags.get(i-2).equals("Which")) {
    				findMovie = true;
    			}
    		}
    	}
    	
    	if(getyear == true) {
    		select = "SELECT O.year FROM OSCAR O ";
    		String subject = null;
    		for(int i=0; i<tags.size();i++) {
    			if(tags.get(i).equals("NNP")) {
    				//System.out.println("found nnp");
    				//get the subject of the when
    				subject = tags.get(i+1);
    			}
    			if(actress == true) {
    				from = "INNER JOIN PERSON P on P.id = O.person_id ";
    				where = "WHERE O.type = \"BEST-ACTRESS\" and P.name LIKE '%"+subject+"%';";
    			}
    			else {
    				from = "INNER JOIN PERSON P on P.id = O.person_id ";
    				where = "WHERE O.type = \"BEST-ACTOR\" and P.name LIKE '%"+subject+"%';";
    			}
    		}
    		String queryString = select + from + where;
        	//System.out.println(queryString);
    		System.out.println("\n<QUERY>\n" );
        	System.out.println(select + "\n" + from + "\n" + where + "\n\n<ANSWER>");
        	try {
    			statement = c.createStatement();
    	    	ResultSet rs = statement.executeQuery(queryString);
    	    	System.out.println(rs.getString("year"));
    		} catch (SQLException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} 
    	}
    	
    	
    	// if directed then we are looking for a director of a movie
    	else if(directed == true) {
    		select = "SELECT P.name FROM PERSON P ";
    		if(year.equals("0")) {
    			from = "INNER JOIN Director D on P.id = D.director_id "
    					+ "INNER JOIN MOVIE M on D.movie_id = M.id ";
    			where = "WHERE M.name LIKE '%"+movieName+"%';";
    		}
    		else {
    			from = "INNER JOIN Director D on P.id = D.director_id "
    					+"INNER JOIN OSCAR O on D.movie_id = O.movie_id ";
    			where = "WHERE O.type = \"BEST-PICTURE\" and O.year = "+year+";";
    		}
    		
    		// Run Query
    		String queryString = select + from + where;
        	//System.out.println(queryString);
    		System.out.println("\n<QUERY>\n" );
        	System.out.println(select + "\n" + from + "\n" + where + "\n\n<ANSWER>");
        	try {
    			statement = c.createStatement();
    	    	ResultSet rs = statement.executeQuery(queryString);
    	    	System.out.println(rs.getString("name"));
    		} catch (SQLException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} 
    	}
    	
    	
    	// if actor tag then we are looking for a actor/actress 
    	else if(actor == true || actress == true) {
    		select = "SELECT DISTINCT P.name FROM PERSON P ";
    		if(actor == true) {
    			from = "INNER JOIN ACTOR A on P.id = A.actor_id"
    					+" INNER JOIN OSCAR O on P.id = O.person_id ";
    			where = "WHERE O.year = "+year+" AND O.type = \"BEST-ACTOR\";";
    		}
    		else {
    			from = "INNER JOIN ACTOR A on P.id = A.actor_id"
    					+" INNER JOIN OSCAR O on P.id = O.person_id ";
    			where = "WHERE O.year = "+year+" AND O.type = \"BEST-ACTRESS\";";
    		}
    		
    		String queryString = select + from + where;
        	//System.out.println(queryString);
    		System.out.println("\n<QUERY>\n" );
        	System.out.println(select + "\n" + from + "\n" + where + "\n\n<ANSWER>");
        	try {
    			statement = c.createStatement();
    	    	ResultSet rs = statement.executeQuery(queryString);
    	    	System.out.println(rs.getString("name"));
    		} catch (SQLException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} 
    	}
    	
    	else if(findMovie == true) {
    		select = "SELECT M.name FROM MOVIE M ";
    		from = "INNER JOIN OSCAR O ON M.id = O.movie_id ";
    		where = "WHERE O.type = \"BEST-PICTURE\" AND O.year = "+year+";";
    		String queryString = select + from + where;
        	System.out.println("\n<QUERY>\n" );
        	System.out.println(select + "\n" + from + "\n" + where + "\n\n<ANSWER>");
        	try {
    			statement = c.createStatement();
    	    	ResultSet rs = statement.executeQuery(queryString);
    	    	System.out.println(rs.getString("name"));
    		} catch (SQLException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} 
    	} 
    	
    }
    
    public static void runQueries(String query) {
    	try {
			c.close();
			System.out.println("Database connection closed.");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void yesNoQuestion(ArrayList<String> tags) 
    {
    	Statement statement= null;
    	String movie = "null";
    	String person = "null";
    	ArrayList<String> people = new ArrayList<String>();
    	people.add("Kubrick");
    	people.add("Allen");
    	people.add("Loren");
    	people.add("Neeson");
    	people.add("Swank");
    	ArrayList<String> movies = new ArrayList<String>();
    	movies.add("Mighty");
    	movies.add("Birdman");
    	movies.add("List");
    	
    	ArrayList<String> location = new ArrayList<String>();
    	location.add("Italy");
    	
    	String select = "Select Count(*) ";
    	String from = "FROM Person P ";
    	//System.out.println("In yesNoQuestion function");	// Debug
    	//System.out.println(tags);	// Debug
    	String where = " ";
    	String pob = "null";
    	ArrayList<String> nouns = new ArrayList<String>();
    	//ArrayList<String> year = new ArrayList<String>();
    	String year = " ";
    	for (int i=0; i<tags.size(); i++) {
    		
    		if(tags.get(i).equals("French")) {
    			pob = "France";
    		}
    		if(tags.get(i).equals("NNP") && tags.get(i-1).equals("NP") && tags.get(i-2).equals("NP")){
    			//if we have two NNP in a row its probably a movie name so dont add it
    		}
    		else if(tags.get(i).equals("NNP") || tags.get(i).equals("NN")) {
    			nouns.add(tags.get(i+1));
    		}
    		else if((tags.get(tags.size()-4)).equals("CD")) {
    			//if the last word is CD, it is a year
    			year = (tags.get(tags.size()-3));
    		}
    		
    	}
    	//Find the person of the sentence
    	for(int i=0; i<nouns.size();i++) {
    		for(int j=0; j<people.size();j++) {
    			if(nouns.get(i).equals(people.get(j))) {
    				person = nouns.get(i);
    			}
    		}
    	}
    	
    	String a_type = "null";
    	for(int i=0; i<nouns.size(); i++) {
    		//if the sentence includes word oscar, then use the table oscar
    		if(nouns.get(i).equals("oscar")) {
    			from = "FROM oscar INNER JOIN person on oscar.person_id=person.id ";
    		}
    		if(nouns.get(i).equals("film")) {
    			a_type ="BEST_PICTURE";
    		}
    	}
    	
    	//find the name of movie
    	for(int i=0; i<nouns.size();i++) {
    		for(int j=0; j<movies.size();j++) {
    			if(nouns.get(i).equals(movies.get(j))) {
    				movie = nouns.get(i);
    			}
    		}
    	}
    	//System.out.println(nouns);	// Debug
    	where = "WHERE P.name LIKE '%" + person + "%'";
    	
    	
    	
    	if(nouns.size() > 2) {
    		if( nouns.get(2).equals("oscar")) {
    			if(a_type != "null") {
            		where ="WHERE person.name LIKE '%" + person + "%' AND type LIKE '%" + a_type + "%'";
            	}
    		}
    	}
    	
    	if(nouns.get(1).equals("oscar")) {
    		
    		if(person != "null") {
    		from = from + "INNER JOIN actor on person.id=actor.actor_id";
    		where = " WHERE person.name LIKE '%" + person +"%' AND oscar.year LIKE '%" + year + "%'";
    		}
    		//if the question is about location inside oscar catagory 
    		if(pob.equals("France")) {
        		
        	where = "WHERE pob LIKE '%" + pob + "%' AND oscar.year LIKE '%" + year + "%'";
        	}
    	}
    	
    	
    	if(nouns.get(1).equals(location.get(0))) {
    			where = "WHERE P.name LIKE '%" + person + "%' AND pob LIKE '%" + location.get(0) + "%'"  ;
    		}		
    	if(movies.get(1).equals(nouns.get(0))) {
    		movie = nouns.get(0);
    		from = "FROM movie INNER JOIN oscar on movie.id=oscar.movie_id ";
    		where = "WHERE movie.name LIKE '%" + movie + "%' AND type LIKE '%BEST-PICTURE%' AND oscar.year LIKE '%" + year + "%'";
    	}
    	// Question about person
    	else if(!person.isEmpty()) {
    		if(nouns.size() == 2) {
    			if(nouns.get(1).equals("director")) {
    				from = from + "INNER JOIN Director D on P.id = D.director_id ";
    			}
    			// assume the other noun is talking about a movie
    			else if (!movie.equals("null")){
    				// Found a movie title in the string
    				from = from + "INNER JOIN Movie M on P.id = M.id ";
    				// Get noun that is not subject
    				int x = 1;
    				if(nouns.get(1).equals(person)) {
    					x = 0;
    					
    				}
    				where = where + " AND M.name LIKE '%" + nouns.get(x) + "%'";
    			}
    			
    		}
    	}
    	
    	
    	
    	String query = select + from + where + ";";
    	System.out.println("\n<QUERY>\n" + select + "\n" + from + "\n" + where + ";");
    	try {
			statement = c.createStatement();
	    	ResultSet rs = statement.executeQuery(query);
	    	System.out.println("\n<ANSWER>");
	    	if(rs.getInt(1) > 0) {
	    		System.out.println("Yes");
	    	}
	    	else if(rs.getInt(1) == 0) {
	    		System.out.println("No");
	    	}
	    	else {
	    		System.out.println(rs.getInt(1));
	    	}
	    	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
    
    //This function takes the tags returned from parser and organizes it into three main catagories 
    public static String categorize(Set<String> categories, int locationPersonTag) {
    	String finalResult = "MOVIES";
    	
    	for(String c : categories) {
        	//System.out.println(c);
        	if(c.equals("MOVIES") || c.equals("[MOVIES]")) {
        		finalResult = "MOVIES";
        		return finalResult;
        	}
        	if(c.equals("MUSIC") || c.equals("[MUSIC]")) {
        		finalResult = "MUSIC";
        		return finalResult;
        	}
        	
        	if(c.equals("LOCATION") || c.equals("GEOGRAPHY") || c.equals("[GEOGRAPHY]")) {
        		finalResult = "GEOGRAPHY";
        		locationPersonTag++;
        		//return finalResult;
        	}
        	if (c.equals("PERSON") && locationPersonTag > 0) {
        		finalResult = "MUSIC";
        		return finalResult;
        	}
        }
    	return finalResult;
    }

    public static void addToSet(Set<String> categories, String value) {
        if (!categories.contains(value)) {
            categories.add(value);
        }
    }

    public static String removeDuplicates(String str) {
        return str.replaceAll("(.)\\1+", "$1");
    }
}
