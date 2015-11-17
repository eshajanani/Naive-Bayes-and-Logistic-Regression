import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;


public class LogisticRegression {

	static String TrainHamPath=null; // holds training data ham path folder name
	static String TrainSpamPath=null; // holds training data spam path folder name
	static String TestHamPath=null;  // holds test data ham path folder name
	static String TestSpamPath=null;// holds test data spam path folder name
	static String StopWordsPath=null; // hold stopwords foldername
	NaiveBayes nb1 = new NaiveBayes();
	static LinkedList<String> stop_list1 = new LinkedList<String>();
	 static LinkedList<String> spl_char1 =new LinkedList<String>();
	 @SuppressWarnings("rawtypes")
	static List reg_lam = new LinkedList();
	static double LR =  0.001;
	static String[][] word_list =null;
	static String[][] weight_list=null;	
	static int train_tot_docs =0;
	static int train_tot_ham =0;
	static int train_tot_spam =0 ;
	static int w0 =1;
	static LinkedHashMap<String, Double> weight_Map=new LinkedHashMap<String, Double>();
	
	public LogisticRegression(String path1,String path2,String path3,String  path4,String path5)
	{
		this.TrainHamPath=path1;
		this.TrainSpamPath=path2;
		this.TestHamPath=path3;
	    this.TestSpamPath=path4;	
	    this.StopWordsPath=path5;
		
	}
	
		//method o perform logistic regression
	public static void do_Log_Regression(LinkedHashMap<String,Integer> word_map) throws FileNotFoundException
	{

		
		train_tot_ham=total_docs_folder(TrainHamPath);
		train_tot_spam=total_docs_folder(TrainSpamPath);
		train_tot_docs = train_tot_ham + train_tot_spam;
		
		store_stopwords(StopWordsPath);
		 create_special_char_list();
		 create_reg_lambda_lis();
		int  tot_dis_words=word_map.size();
		 word_list= new String [train_tot_docs+1][tot_dis_words+1];
		 create_word_list(word_map,train_tot_docs,tot_dis_words,TrainHamPath,TrainSpamPath);
		 weight_list=new String[2][tot_dis_words];
		 //create inital weight list
		 create_weight_list(word_map,tot_dis_words);
		 double[] doc_prob_list = new double[train_tot_docs];
		 //update weight list

		 System.out.println("updating weight list ");
		 update_weight_list(word_map,doc_prob_list);
	
		 System.out.print("\n LR is : "+LR);
		 System.out.println("Performing the accuracy test on test Data");
		 System.out.println("Checking accuracy for Logistic Regression without Stopwords");
		 cal_accuracy(TestHamPath,weight_Map,1, "no"); // for test ham data folder
		 cal_accuracy(TrainSpamPath,weight_Map,2,"no"); // for test spam data folder
		 System.out.println("Checking accuracy for Logistic Regression with Stopwords");
		 cal_accuracy(TestHamPath,weight_Map,1, "yes"); // for test ham data folder
		 cal_accuracy(TrainSpamPath,weight_Map,2,"yes"); // for test spam data folder
		
	}// end of method logistic regression
	
	// method to calculate the accuracy of the test data
	public static void cal_accuracy(String file,LinkedHashMap<String, Double> weight_Map, int type, String stop ) throws FileNotFoundException
	{
		
		int ham_count=0,spam_count=0;
		File folder = new File(file);
		File[] files = folder.listFiles();
		
		for(int i=0;i<files.length;i++)
		{
			double HamProb=0,SpamProb=0,sum=0;
			LinkedHashMap<String,Integer> temp_map = new LinkedHashMap<String,Integer>(); 
		
			File cur_file = files[i];
		
		//for every doc create a temp_map with words and its count
			temp_map=count_doc_words(cur_file.toString(),temp_map,stop);
	
			// calculate summation of WI and Xi
	Set set= temp_map.entrySet();
	Iterator it = set.iterator();
	while(it.hasNext())
	{
		Map.Entry me = (Map.Entry)it.next();
		String word= me.getKey().toString();	
		Double xI=Double.parseDouble(me.getValue().toString());
		double wI=0;
		if(weight_Map.containsKey(word))
		{
			wI= weight_Map.get(word);
			sum=sum+Math.log(wI)+Math.log(xI);
			
		}
		
		
	}// end of while for every word in the doc
	
	sum=w0+sum;
	HamProb = Math.exp(sum)/(1+Math.exp(sum));
	SpamProb = 1/(1+Math.exp(sum));
	double RHS =Math.log(HamProb)-Math.log(SpamProb);
	double LHS =Math.log(1);
		

			if(LHS < RHS)
			{
				ham_count++;
			
				
			}
			else
			{
				spam_count++;
			
				
			}
			
	
		}//end of for i 
		if(type==1)
		{
		//	System.out.println("Ham count : "+ ham_count +"Files length :"+files.length);
		float ham_percent= (ham_count*100)/files.length;
		System.out.println(" The percentage of occurence in ham folder is : " +ham_percent);
		}
		else
		{
			float spam_percent= (spam_count*100)/files.length;
			System.out.println(" The percentage of occurence in spam folder is : " +spam_percent);
		}

		
	}
	
	// method to count the words in a single doc
	public static LinkedHashMap<String,Integer> count_doc_words(String path,LinkedHashMap<String,Integer> map,String stop) throws FileNotFoundException
	{
		
			File file =new File(path);
			Scanner scan = new Scanner(file);
			while(scan.hasNext())
			{
				String word =scan.next();
				
				if(spl_char1.contains(word)){
					 continue;
				 }//end of if
				if(stop.equalsIgnoreCase("yes") && stop_list1.contains(word))
				{
					continue;
				}
				if(map.containsKey(word))
				 {
				 Integer count = (int)map.get(word);
				 count= count+1;
				 map.remove(word);
				 map.put(word, count);
					 
				 }//end of else if
				
				 else
					 map.put(word, 1);
			}// end of while
		return map;
	}// end of method count_words
	
	// method to update the weight list with new values 
	public static void update_weight_list(LinkedHashMap<String,Integer> word_map, double[] doc_prob_list )
	{
		
		System.out.print("\n Inside update weight list \n");

		 float reg_val=(float) 1.8;
			System.out.println("lambda value is:" +reg_val);
			//for each hardlimit values
			int lmax=100;
			System.out.println("\n The limit val is :"+ lmax);
			for(int limt=0;limt<lmax;limt++)
		{
				System.out.println("Iteration :  "+limt );
				// calculate document probability
				cal_doc_prior_prob(word_map,doc_prob_list);
				// for each distinct word xI
				for(int j=0;j<word_map.size();j++)
				{
					double sum=0;
					
					
					// get the weight of that word from weight list
					double wI=Double.parseDouble(weight_list[1][j]);
					
					// for each document do
					
					for(int k=1;k<train_tot_docs;k++)
					{
						double y=0, doc_prob=0, xI=0;
						// get the y value by checking if it is ham or spam (ham =1 and spam =0)
						if(word_list[k][word_map.size()].equalsIgnoreCase("ham")){
							 y=1;
						}
						else
							{
							y=0;
							}
						// get the doc prob
						doc_prob=doc_prob_list[k];
						//get the word occurence from word_list
						xI=Double.parseDouble(word_list[k][j]);
						//calculate the summation of xi ( yI-doc_prob) for a word in all the documents
						sum=sum+xI*(y-doc_prob);
							
					}// end of for each doc
					
					// now calculate the wi for every word  and update in the weight list
					wI=wI+(LR*sum)-(LR*reg_val*wI);
					//update the weight list with new wi for that word
					weight_list[1][j]=String.valueOf(wI);
					
				} // end of for each word
				
				
				
		}// end of hardlimit
		 
		 
	// }// end of i (lambda values)
			
			for(int i=0;i<word_map.size();i++){
				String word=weight_list[0][i];
				Double wI=Double.parseDouble(weight_list[1][i]);
				weight_Map.put(word, wI);
			}
			System.out.print("\n out side  update weight list \n");
			
		//	map_display(weight_Map);
			
			if(weight_Map.size()==word_map.size())
			{
				System.out.print("They are equal \n");
			}
			
		
	}// end of weight list
	
	
	// method to calculate document prior probability
	
	public static void cal_doc_prior_prob(LinkedHashMap<String,Integer> word_map, double[] doc_prob_list)	{
		// for every document
		for(int i=1;i<train_tot_docs;i++)
		{
			//for every word in the document in for I
			double sum=0;
			for(int j=0;j<word_map.size();j++)
			{
			// get the weight of the word from weight_list
				double temp_wI=0;
				double wI =0;
				if(Double.parseDouble(weight_list[1][j])<0)
				{
					temp_wI=Math.abs(Double.parseDouble(weight_list[1][j]));
				}
				else
				{
					temp_wI=Double.parseDouble(weight_list[1][j]);
				}
				wI=(double)Math.log(temp_wI);
				
				// get the count of the word in the document in the word_list
				double xI=0;
				if(!word_list[i][j].equals("0"))
				{
					xI= Double.parseDouble(word_list[i][j]);
					
				}
				//take the log of both weight and word count
				
				xI=Math.log(xI);
				// add the count and weight of the document 
				sum=sum+wI+xI;

			}//end of for j - for every word in the document
			double prob=0;
			sum=w0+sum;
			if(word_list[i][word_map.size()].equalsIgnoreCase("ham"))
			{
			
				prob=Math.exp(sum)/(1+Math.exp(sum));
				
			}
			else 
				{
				prob=1/(1+Math.exp(sum));
				}
			
			
			doc_prob_list[i]=prob;
		}// end of for i - for every document
		
	}
	
	
	//method to calculate total no of documents in a folder
		public static int total_docs_folder(String path)
		{
			return new File(path).listFiles().length;
		}// end of total_docs_folder method

		//method to create weight list
		
		public static void create_weight_list(LinkedHashMap<String,Integer> map,int col)
		{
			Set set = map.entrySet();
			Iterator it=set.iterator();
			int i=0;
			while(it.hasNext())
			{
				Map.Entry me = (Map.Entry)it.next();
				weight_list[0][i]= me.getKey().toString();
				i++;
				
			}
			
			for(int j=0;j<col;j++)
			{
				String wI=random_number_generator(0,3);
				weight_list[1][j]=wI;
			}
			

			
		}// end of method create_weight_list
		
		
		
		// method to generate random number
		public static String random_number_generator(float min, float max)
		{
			Random r= new Random();
			float r_no = (r.nextFloat()*(max-min))+min;
		//	System.out.print("\n the random is: "+r_no);
			
			return String.valueOf(r_no);
		}
		
		
		// method to create word list
		public static void  create_word_list(LinkedHashMap<String,Integer> map,int row, int colm, String HamPath,String SpamPath) throws FileNotFoundException
		{
			int doc_no=1; // row no of word_list matrix
			Set set = map.entrySet();
			Iterator it = set.iterator();
			int col=0;
			while(it.hasNext())
			{
				Map.Entry me = (Map.Entry)it.next();
				word_list[0][col]=me.getKey().toString();
				col++;
			}
			word_list[0][colm]= "class_name";
			for(int i=1;i<word_list.length;i++)
			{
				for(int j=0;j<word_list[i].length-1;j++)
					{
					word_list[i][j]="0";
					}
			}
			
			
			
			// Intialize the word_list with ham words occurence

			File folder1 = new File(HamPath);
			File[] files = folder1.listFiles();
			for(int x=0;x<files.length;x++)
			{
				LinkedHashMap<String,Integer> temp_map = new LinkedHashMap<String,Integer>(); 
				File cur_file= files[x];
				String word=null;
				Scanner scan = new Scanner(cur_file);
				while(scan.hasNext())
				{
					word=scan.next();
					if(spl_char1.contains(word)){
						 continue;
					 }//end of if
					 else if(temp_map.containsKey(word))
					 {
					 Integer count = (int)temp_map.get(word);
					 count= count+1;
					 temp_map.remove(word);
					 temp_map.put(word, count);
						 
					 }//end of else if
					 else
					 temp_map.put(word, 1);
					
			}// end of while
				String value=null;
				String key=null;
				Set set1 = temp_map.entrySet();
				Iterator it1 = set1.iterator();
				while(it1.hasNext())
				{
					Map.Entry me = (Map.Entry)it1.next();
					key=me.getKey().toString();
					value= me.getValue().toString();
					for	(int i=0;i <colm ;i++)		
					{
						if(word_list[0][i].equalsIgnoreCase(key))
						{
							
							word_list[doc_no][i]=value;
						//	continue;
						}// end of if
					} // end of for
					
					
				}// end of while
				word_list[doc_no][colm]="ham";
				doc_no++;
			}// end of for x
		
			// Intialize the word_list with spam words occurence

			File folder2 = new File(SpamPath);
			File[] files1 = folder2.listFiles();
			for(int y=0;y<files1.length;y++)
			{
				LinkedHashMap<String,Integer> temp_map1 = new LinkedHashMap<String,Integer>(); 
				File cur_file1= files1[y];
				String word1=null;
				Scanner scan1 = new Scanner(cur_file1);
				while(scan1.hasNext())
				{
					word1=scan1.next();
					if(spl_char1.contains(word1)){
						 continue;
					 }//end of if
					 else if(temp_map1.containsKey(word1))
					 {
					 Integer count1 = (int)temp_map1.get(word1);
					 count1= count1+1;
					 temp_map1.remove(word1);
					 temp_map1.put(word1, count1);
						 
					 }//end of else if
					 else
					 temp_map1.put(word1, 1);
					
			}// end of while
				String value1=null;
				String key1=null;
				Set set1 = temp_map1.entrySet();
				Iterator it1 = set1.iterator();
				while(it1.hasNext())
				{
					Map.Entry me1 = (Map.Entry)it1.next();
					key1=me1.getKey().toString();
					for	(int i=0;i <colm ;i++)		
					{
						if(word_list[0][i].equalsIgnoreCase(key1))
						{
							value1= me1.getValue().toString();
							word_list[doc_no][i]=value1;
						//	continue;
						}// end of if
					} // end of for
					
				}// end of while
				word_list[doc_no][colm]="spam";
				doc_no++;
			}// end of for y
			

		
		}//end of method create_word_list
		
		
		
		
	//method to store stopwords into array list
	public static void store_stopwords(String file) throws FileNotFoundException
	{
			String word=null;
			File cur_file= new File(file);
			 Scanner scan=new Scanner(cur_file);
			while(scan.hasNext())
			{
				word=scan.next();
				stop_list1.add(word);
				
				
			}// end of while
			
		
	}// end of method store_stopwords
	
	//method to create reg lambda values list
	
	public static void create_reg_lambda_lis()
	{
		reg_lam.add(0.2);
		reg_lam.add(0.4);
		reg_lam.add(0.6);
		reg_lam.add(0.7);
		reg_lam.add(0.8);
		
	}
	
	  //method to create special characters list
    
    public static void create_special_char_list()
    {
        spl_char1.add(".");
        spl_char1.add("-");
        spl_char1.add(":");
        spl_char1.add("/");
        spl_char1.add("@");
        spl_char1.add(",");
        spl_char1.add(" ");
        spl_char1.add("%");
        spl_char1.add("!");
        spl_char1.add(">");
        spl_char1.add("<");
        spl_char1.add("=");
        spl_char1.add("?");
        spl_char1.add("[");
        spl_char1.add("]");
        spl_char1.add("'");
        spl_char1.add("{");
        spl_char1.add("}");
        spl_char1.add("");
        spl_char1.add("_");
        spl_char1.add("$");
        spl_char1.add("(");
        spl_char1.add(")");
        spl_char1.add("|");
        spl_char1.add("*");
        spl_char1.add("~");
        spl_char1.add("`");
        spl_char1.add(".");
        spl_char1.add("#");
        spl_char1.add("\"");
        spl_char1.add("+");
        
    }
    
    
 // Method to display any map
 	public static void map_display(LinkedHashMap map)
 	{
 		Set set = map.entrySet();
 		Iterator it = set.iterator();
 		while(it.hasNext())
 		{
 			 Map.Entry me = (Map.Entry)it.next();
 	         System.out.print(me.getKey() + ": ");
 	         System.out.println(me.getValue());
 		}
 	}// end of method map_display
    
}