## How to compile and run
javac -d classes -encoding UTF-8 ir/*.java
sh run_search_engine.sh
sh run_persistent.sh
find . -type f | xargs grep -l -w "money"
find . -type f | xargs grep -l -w "money" | wc -l

## Compile and run KGram
javac -cp . -d classes -encoding UTF-8 ir/KGramIndex.java
java -cp classes ir.KGramIndex -f kgram_test.txt -p patterns.txt -k 3 -kg "ove mea"


## Task 1.5

#### Label the documents

1 data/davisWiki/Elaine_Kasimatis.f 3 - It is about a mathematics program graduate.  
1 data/davisWiki/Events_Calendars.f 0 - Only urls.  
1 data/davisWiki/Student_Organizations.f 1 - Points to graduation topic but no further information.  
1 data/davisWiki/Quantitative_Biology_and_Bioinformatics.f 2 - Related to program graduate and mathematics (bio-mathematics)  
1 data/davisWiki/Private_Tutoring.f 3 - Provide information needed if one wants to hire a private tutor in order to graduate a program in maths  
1 data/davisWiki/Economics.f 1 - It does point to economics which is an application of maths but does not really related to the topic of graduation in maths  
1 data/davisWiki/Biological_Systems_Engineering.f 1 - Simliar to the above but biology instead of economics  
1 data/davisWiki/UC_Davis_English_Department.f 0 - Mathematics is mentioned but it is about experiences of students who has studied in this apartment which does not relate to maths.  
1 data/davisWiki/Computer_Science.f 1 - It does point to computer science which is an application of maths but does not really related to the topic of graduation in maths  
1 data/davisWiki/What_I_Wish_I_Knew...Before_Coming_to_UC_Davis_Entomology.f 2 - Even though it does not discuss much about maths but a lot about the topic of graduating program  
1 data/davisWiki/Evelyn_Silvia.f 1 - About a person who graduated mathematics  
1 data/davisWiki/Fiber_and_Polymer_Science.f 1 -  
1 data/davisWiki/UCD_Honors_and_Prizes.f  
1 data/davisWiki/document_translated.f  
1 data/davisWiki/Statistics.f  
1 data/davisWiki/University_Departments.f  
1 data/davisWiki/ECE_Course_Reviews.f  
1 data/davisWiki/Hydrology.f  
2 data/davisWiki/MattHh.f  
2 data/davisWiki/Candidate_Statements.f  
2 data/davisWiki/Wildlife%2C_Fish%2C_and_Conservation_Biology.f  
2 data/davisWiki/Mathematics.f

#### Precision: the fraction of retrieved documents that are relevant

precision = good/retrieved = (relevant and retrieved) / retrieved = 16/22 = 0.7273


#### Recall: the fraction of relevant documents that are retrieved

recall = good / relevant = (relevant and retrieved) / relevant = 16/100 = 0.16

## Task 3.3

!!! Remember to uncomment printing for "ve" and "th he" in Engine

## Task 3.4

#### How would you interpret the meaning of the query "historic* humo*r"?

Find all docs containing any word beginning “historic”, and any word beginning with “humo”
ending in “r”

#### Why could the word "revenge" be returned by a bigram index in return to a query "re*ve"?

Although it does not end with "ve" but it contains both kGrams "^r" and "e$" if 2-gram index is to be used.

#### How could this problem of false positives be solved?

By introducing a postfiltering step, in which the terms enumerated by Boolean query on the 
kGram index are checked individually against the original query. (Before the standard searching)

## Task 2.3

#### idfs 

redirect: 0.9622652645232597

davis: 0.22885210283533228

food: 0.9770906223501915

coop: 1.5118336819028761

residence: 1.769859508252053

hall: 1.1601896567084935

movein: 2.416541154868439

recycling: 1.9329857901433665

drive: 1.0983531838072746

## Task 3.1

#### What happens to the two documents that you selected?
The documents that are selected are often pushed to higher position in the ranking result list.

#### What are the characteristics of the other documents in the new top ten list - what are they about? Are there any new ones that were not among the top ten before?
There are some documents that were not there before. The new docs in the new top ten list are those
which contains words with high term frequency in the chosen relevant docs.

#### How is the relevance feedback process affected by α and β?
If β is significantly bigger than α, the weights of words in the old query do not weight much any more.
Instead, other words with big term frequency in the chosen relevant docs have significantly greater weights. 

#### Why is the search after feedback slower?
Because the engine has to calculate weights for a much greater number of query terms than in the old query.
Then, it has to process a much larger query and thus process a much larger number of matching documents. 

#### Why is the number of returned documents larger?
Because more terms are added into the new query. Therefore there will be more documents that contains those terms. 

#### Why is relevance feedback called a local query refining method? 
Global methods are techniques for expanding or reformulating query terms independent of the query and results returned from it.
Local methods adjust a query relative to the documents that initially appear to match the query. 


#### What are other alternatives to relevance feedback for query refining?
Pseudo relevance feedback, also known as Blind relevance feedback
Indirect relevance feedback
Other global methods


## Task 3.2

Before relevant feedback: NDCG= 0.4734962383055826

After relevant feedback: NDCG= 0.6671560112340305

#### Why do we want to omit the feedback-document?
Because it is enough with just one document with high relevant score in the
very top positions to make big change in ndcg. Mathematics.f after feedback
was placed in the 9th place which would boost the ndcg significantly if counted. 
In that case we cannot see the effect of feedback on the rest of the documents. 



