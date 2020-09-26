# Chatbox_NLP
Java and Stanford Parser


-We are using Stanford Core NLP for this project.

#Assumptions:
tools/Stanford directory has the following jar files in it:

Note: 
This has 2 new Jar files to run the database on the terminal:
-	Ojdbc6
-	Sqlite-jdbc-3.8.11.2
 

1. Unzip the folder which will contain following files:
-custom.rules
-Parser.java (This file contains the output from the sentences mentioned in the document.)
-usoni3_cpotte4.jar
-pom.xml
- Ojdbc6
- Sqlite-jdbc-3.8.11.2
-parsed.txt (This is file contains the output of the sentences that were      being asked to turn in with the assignment)

-#Run the jar file [in order to run the file, you need to have the two jar files mentioned above in the tools\stanford directory]

NOTE: We were having problem running the following jar file on the terminal using the command mentioned in the guideline file. For the alternative, there are two ways you can run the code to get the outfit:
1.	In terminal
2.	Eclipse


1.	To run on terminal:

run the following command:

java -classpath ./tools/stanford/*;usoni3_cpotte4.jar nlp.parser.Parser input.txt

This will show the output of the code with questions, query and it’s answers.

2.	To run it on eclipse:
-Make sure there is tools\stanford folder in same place as all the other files. That directory should have all the jar files from Stanford in addition to two additional one to run the database.
-In eclipse import the folder that contains everything 
-Run the Parser.java file
-This should give you the same output which has questions, query and answer.



Yes/No questions with the verb ’to be’
(1a) Is Kubrick a director?
(1b) Is Mighty Aphrodite by Allen?
(1c) Was Loren born in Italy?
(1d) Was Birdman the best movie in 2015?
Yes/No questions with the auxiliary Do
(2a) Did Neeson star in Schindler’s List?
(2b) Did Swank win the oscar in 2000?
(2c) Did a French actor win the oscar in 2012?
(2d) Did a movie with Neeson win the oscar for best film?
1
Wh-questions Crucially, we only answer Wh-questions where the Wh-NP is the subject, or refers to the
year via When e.g.:
(3a) Who directed Schindler’s List?
(3b) Who won the oscar for best actor in 2005?
(3c) Who directed the best movie in 2010?
(3d) Which American actress won the oscar in 2012?
(3e) Which movie won the oscar in 2000?
(3f) When did Blanchett win an oscar for best actress?
2 Comments, assumptions, simplifications etc
1. Movie titles are most often composed by more than one word. For movie titles, you can use the
operator LIKE used in the example SQL query in the description of project part 2. This will also be
useful for actor / director names, since the movie database provides both first and last names, but we
will use only last names in the queries.
2. No determiner will be processed semantically, i.e., the semantic attachment for a/an/the will be empty.
3. We will exclude questions where the WhNP is the object, or starting with a prepositional phrase
containing a wh- word, such as:
(4a) Which movie did Allen direct?
(4b) In which year did Loren win an Oscar?
(4c) In which country was Blanchett born?
4. The only adjectives to be dealt with are best and nationality adjectives, such as American, Italian,
French, British, German. Since you will have to think about semantics for each of these nationalities,
you can limit yourself to these 5. You will no be required to include supporting, unless you wish to
do so.
5. Note that the rule for S given in the example in project part 2 for Did Allen direct Mighty Aphrodite
will not work for sentences such as Did a French actor win the oscar in 2012?, since the subject is not
a proper noun. You will have to think how to build the semantics of an NP such as a French actor, and
how to incorporate it into S. As already mentioned, a / an will not contribute anything, i.e., we will
not treat the indefinite determiner as a quantifier. The “trick” of extracting the semantic variable that
a formula mainly is about may be useful – see VP.sem.variable pg. 568 of the semantic attachment
handout, here we may need to use NP.sem.variable.
6. We will limit ourselves to one prepositional phrase attached to an NP, e.g a movie with Neeson, a
movie by Kubrick, excluding e.g. a movie by Kubrick with Neeson. Note that the temporal PP, e.g. in
2010, attaches to the VP.
2
2.1 The verb “to win”
The verb to win will require special attention. It appears in three configurations, of which we will consider
two. If we classify oscar as PRIZE; and movie category (best actor, actress, director etc) as CATEGORY,
we can produce the following schemata:
1. Win the oscar: schematically, win + prize
2. Win best actress: win + category
3. Win the oscar for best actress: win + prize + category
Our examples are all in the form (1) or (3), don’t worry about (2). However, you will have to devise
appropriate semantic attachments for win according to these schemata, but try to be as general as possible.
Note that inclusion of the category may change the answer to the question. Consider:
1. Did Hathaway win an oscar in 2013?: the answer is yes, she won the oscar for best supporting actress.
2. Did Hathaway wi the oscar for best actress in 2013? the answer is no. A real system should answer
no, but she won for best supporting actress, but this is too complicated for us.
Note that the examples just discussed do not take into account the eventual in-PP that carries the date,
as in Did Hathaway win an oscar in 2013?. You will have to think of a way of dealing with such PPattachments
– again the “trick” of extracting the VP.sem.variable may be helpful.
*******************
*******************
In the second part of the project, you will derive a semantic representation from the syntactic trees
the parser you used in part 1 returns to you. To provide a semantic interpretation for the input means to
transform the input into a suitable representation, and then build a small interpreter that uses the semantic
representation to compute answers from the database. To give you a taste of a realistic application, we are
using SQL, a database query language, rather than first order logic, like the book does. As you will see, a
SQL query can be built by using similar semantic attachments to the ones used for logic.
IMPORTANT NOTE: Simplification. To make the assignment doable, we will significantly simplify the
queries from Part 1. Part 1 was meant to give you a taste of how a broader range of queries would be
parsed; Part 2 a taste of how complicated deriving a semantic representation is. Please see additional file
project-part2-questions.pdf for the specific queries you are asked to answer.
Additionally:
1. For undergraduates: you will limit yourselves to the Movie domain, unless you want to do extracredit,
see next.
2. For graduates: you must be able to answer queries in the other two domains. Graduate students
must do more work for a 400-level class, as per University requirements.
3. Undergraduate extra credit (50 points): same as for graduate students.
2 SQL
SQL is a popular language used to submit queries to a relational database system. SQL is a declarative
language, i.e., we tell the database engine what we want to get, without specifying any detail on how the
system should actually perform the retrieval.
A separate file, sqlite.pdf, in this same folder provides some references to the tool side of SQL. Here,
we introduce how we can use the SQL language to obtain information from a database.
1
The main data structure of a relational database is called table. Each row in a table (record) stores some
information. Such information is divided into or more labeled fields. For example, in a hypothetical DB, we
could have (we will use examples from our own DBs later in this document):
Person
id first name last name
1 John Smith
2 Mary Green
3 Susan Brown
4 William Smith
Phone numbers
person number
1 312 123 4567
2 773 321 7654
3 312 111 2233
4 312 555 0000
The process of answering a query consists of the retrieval of a set of records matching the required conditions.
For example, to find the people whose last name is Smith, we can write the query:
SELECT *
FROM people
WHERE last_name = ’Smith’;
The database engine will return two records (’*’ means, return every field in the selected records):
id first name last name
1 John Smith
4 William Smith
If the information we are looking for is scattered in more than one table, we can join tables by setting up
appropriate equality conditions on related fields. For example, to find the phone number of Susan Brown, we
need to join the tables people and phone numbers on the fields id of people and person of phone numbers:
SELECT number
FROM people P
INNER JOIN phone_numbers PN ON P.id = PN.person
WHERE first_name = "Susan"
AND last_name = "Brown";
The result is:
number
773 321 7654
In case of name conflicts, that can occur while joining tables having columns with the same name, we should
make explicit reference to the specific columns, using the dotted notation (table.column). For example, the
previous query could also be written as:
SELECT PN.number
FROM people P
INNER JOIN phone_numbers PN ON P.id = PN.person
WHERE P.first_name = "Susan"
AND P.last_name = "Brown";
For further details on the SQL language, consult a book or one of the many online resources available, e.g.
http://sqlzoo.net/.
2
3 Representative SQL queries
In this project, we will build SQL queries using the idea of compositional semantics. Instead of building a
semantic representation for a sentence using first order logic, we will represent the semantics in the form
of a SQL query. This approach is possible because of the declarative nature of the SQL language, that
makes it somewhat similar to a logic representation. In practice, we will attach pieces of an appropriate
SQL-semantic representation to the rules of the grammar that have been used to build the parse trees, such
that the final SQL query can be built compositionally from these pieces. A possible implementation of this
approach is illustrated in the following example.
Examples. Consider the sentence:
Which Italian actress won the oscar in 1962?
Answer: Sophia Loren
This sentence is a WH question. We want to retrieve the answer from the movie database, that consists of
five tables; three are relevant here, Actor, Oscar, and Person (note: the numeric ids were copied from the
DB. I make no guarantee that every single digit is accurate in the tables below)
Person
id name dob pob
0000047 Sophia Loren 1934-09-20 Pozzuoli, Italy
0000138 Leonardo DiCaprio 1974-11-11 Hollywood, California, USA
0000473 Dianne Keaton 1946-01-05 Los Angeles, California, USA
Oscar
movie id person id type year
0075686 0000473 best-actress 1978
0054749 0000047 best-actress 1962
Actor
actor id movie id
0000047 0054749
0000138 0120338
0000138 0407887
0000473 0082979
0000473 0075686
The answer to the former question can be retrieved using this query:
Select P.name FROM Person P
INNER JOIN Oscar O ON P.id = person_id
INNER JOIN Actor A ON A.actor_id = person_id
WHERE P.pob LIKE "%Italy%" AND
O.year = "1962";
This query returns Sophia Loren. This query can easily be run e.g. within SQLite Manager, by cutting and
pasting it at the the “Enter SQLite commands” prompt. Note that technically this query is not fully specified,
3
because we did not specify that we are asking about the Oscar for best actress, implicit in the actress word
in the query. Ie, if an Italian actor had also won an Oscar in 1962, or another Italian actress had won the
oscar for best supporting actress, their names would also be returned.
A question like Did Bigelow win an oscar for best director? would result in the following SQL query
Select Count(*) FROM Oscar O
INNER JOIN Person P ON person_id = P.id
WHERE P.name LIKE "%Bigelow%" AND
O.type="BEST-DIRECTOR"
This query would return 1, hence, “yes”. If we asked the same question but about Sophia Loren (i.e.,
P.name LIKE "%Loren%"), the query would return 0, hence, “no”.
Note that the LIKE ‘‘%string%" operator is used to allow flexibility in matching proper names (e.g.
in the movie database, full names are provided but we will use only last names in the queries).
4 SQL queries and compositional semantics
How can we compositionally build such a query? We will sketch one way of doing so, which is based on a
much simplified grammar and lexicon.
IMPORTANT NOTE. While you’ll use a similarly simplified grammar and lexicon to what is presented
below, you’re welcome to devise a different, principled way of assembling the SQL query. The way we
propose is not necessarily the best. Principled here means a method that is compositional in nature, and not
too ad hoc.
Let’s use as an example Did Allen direct MightyAphrodite? One simple grammar we can use follows (this
grammar is for illustrative purposes only!!! your grammar is the grammar used by the parser you
chose, please see NOTE at the end of this section):
S! Aux NP VP
VP!Verb NP
NP!ProperNoun
ProperNoun! Allen j MightyAphrodite
Verb! direct
Aux! did
Now, let us attach a function to each rule of the grammar, as shown on the next page. Each attachment can
be implemented as a function that can do three main things:
1. Return a string that will be used by another function in the lambda reduction mechanism.
2. Call another function belonging to a child node of the parse tree.
3. Add elements to data structures that will be used to compose the final query.
4
S! Aux NP VP
{[SELECT] += "count(*)"
VP.sem(NP.sem)}
VP! Verb NP
{Verb.sem(NP.sem)}
NP! ProperNoun
{‘ProperNoun.sem’}
ProperNoun!Allen
{Allen}
ProperNoun!MightyAphrodite
{Aphrodite}
Verb!direct
(lambda :y :x)
{ [FROM] += " FROM Person P "
[FROM] += " INNER JOIN Director D ON P.id = D.director_id "
[FROM] += " INNER JOIN Movie M ON D.movie_id = M.id "
[WHERE] += " P.name LIKE ‘%:x%’ "
[WHERE] += " M.name LIKE ‘%:y%’ "}
In the example above, we have three data structures: [SELECT], [FROM], and [WHERE]. In practice,
they are sets of strings. At the last step of semantic processing, we can transform the three data structures
into plain strings using the following rules – the indices below (1, 2 etc), simply refer to members of the
corresponding sets of strings.
SelectString := [SELECT].1, [SELECT].2, ... , [SELECT].n
FromString := [FROM].1, [FROM].2, ... , [FROM].n
WhereString := [WHERE].1 and [WHERE].2 and ... and [WHERE].n
Finally, we can build the final, well formed SQL query:
select SelectString
from FromString
where WhereString;
Important Notes:
1. The semantic structure provided for the verb “direct” implies that its arguments are Proper Nouns,
given the WHERE constraints that include proper nouns directly, via P.name and M.name. This
5
would not work for, say, Did a French director direct MightyAphrodite? It would probably be better
to add WHERE specifications to the Proper Noun semantics itself, even if at that point we wouldn’t
know to which entity (movie or director) the proper noun refers to. It is something you will need to
explore, and that will need to be dealt with in the last steps of semantic processing we just described.
2. You do not have a grammar written declaratively as the one just described. This is due to using
existing parsers such as those provided by OpenNLP or NLTK. Basically, each subtree Node1 !
Subnode1 Subnode2 ::: Subnoden in the parse trees you obtain can be taken as denoting one such
rule.
5 Sentences to be covered
See additional file project-part2-questions.pdf. For the bulk of the project, we will limit ourselves to the
Movie domain. For graduate students or for undergraduate extra credit, queries in the other two domains
will be considered as well.
6 Input/Output of your program
Your program should parse the sentence with the parser of your choice, take the resulting syntactic tree,
produce the SQL query, route it to the SQLite engine with corresponding API in the programming language
your choose, and process it against the DBs we provide. Your program should output: the original sentence,
the SQL query and the answer you obtain from the DB. The final answer, produced by SQLite, does not
need to be in natural language, but it has to be unambiguous and understandable. For example,
SELECT count(*)
will return a number n, where n can be 0 or > 0: you should transform 0 into ’no’ and any other number
into ’yes’.
