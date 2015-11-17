import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;


@SuppressWarnings("rawtypes")
public class NaiveBayes {

	
	static String TrainHamPath=null; // holds training data ham path folder name
	static String TrainSpamPath=null; // holds training data spam path folder name
	static String TestHamPath=null;  // holds test data ham path folder name
	static String TestSpamPath=null;// holds test data spam path folder name
	static String StopWordsPath=null; // hold stopwords foldername
	static LinkedHashMap<String,Integer> HamMap = new LinkedHashMap<String, Integer>(); // holds the word and occurence of it in ham folder files
	static LinkedHashMap<String,Integer> SpamMap = new LinkedHashMap<String,Integer>(); // holds the word and occurence of it in spam folder files
	static LinkedHashMap<String,Integer> Dis_Words_Map = new LinkedHashMap<String,Integer>(); // holds the distict words both in ham and spam folder files
	static LinkedList<String> stop_list = new LinkedList<String>();
        static LinkedList<String> spl_char =new LinkedList<String>();
	
	public NaiveBayes()
	{
		
	}
	
	public NaiveBayes(String path1,String path2,String path3,String  path4,String path5)
	{
		this.TrainHamPath=path1;
		this.TrainSpamPath=path2;
		this.TestHamPath=path3;
	    this.TestSpamPath=path4;	
	    this.StopWordsPath=path5;
		
	}
	
	public static void do_naive_bayes() throws FileNotFoundException
	{
		int train_tot_docs =0;
		int train_tot_ham =0;
		int train_tot_spam =0 ;
		int test_tot_docs=0;
		int test_tot_ham=0;
		int test_tot_spam=0;
		float[] priors_val= new float[2];
	
	train_tot_ham=total_docs_folder(TrainHamPath);
	train_tot_spam=total_docs_folder(TrainSpamPath);

	
	train_tot_docs = train_tot_ham + train_tot_spam;

	
	priors_val[0] = (float)train_tot_ham /(float)train_tot_docs; // holds ham's prior val
	priors_val[1]=(float)train_tot_spam /(float)train_tot_docs; // holds spam's prior val
	

    create_special_char_list();
	count_words(TrainHamPath, HamMap);// count words in Ham train folder and store in the HamMap
	count_words(TrainSpamPath, SpamMap);// count words in Ham train folder and store in the HamMap
	store_stopwords(StopWordsPath);
	int ham_tot_words = count_tot_words(HamMap);
	int spam_tot_words = count_tot_words(SpamMap);
	int dis_total_words =count_tot_dis_words(HamMap,SpamMap);

	System.out.println("Checking accuracy for Naive Bayes without Stopwords");
cal_accuracy(TestHamPath,priors_val[0],priors_val[1],dis_total_words,ham_tot_words,spam_tot_words,1, "no"); // for test ham data folder
cal_accuracy(TrainSpamPath,priors_val[0],priors_val[1],dis_total_words,ham_tot_words,spam_tot_words,2,"no"); // for test spam data folder
System.out.println("Checking accuracy for Naive Bayes with Stopwords");
cal_accuracy(TestHamPath,priors_val[0],priors_val[1],dis_total_words,ham_tot_words,spam_tot_words,1, "yes"); // for test ham data folder
cal_accuracy(TrainSpamPath,priors_val[0],priors_val[1],dis_total_words,ham_tot_words,spam_tot_words,2,"yes"); // for test spam data folder
	}// end of method do_naive_bayes
	
	

	//method to store stopwords into array list
	public static void store_stopwords(String file) throws FileNotFoundException
	{
		String word=null;
		File cur_file= new File(file);
		 Scanner scan=new Scanner(cur_file);
		while(scan.hasNext())
		{
			word=scan.next();
			stop_list.add(word);
			
			
		}// end of while
	}// end of method store_stopwords
	
	
// method to calculate accuracy of naive bayes on test data
	
	public static void cal_accuracy(String file,float priorH, float priorS, int dis_total_words,int ham_tot, int spam_tot,int type,String stop) throws FileNotFoundException
	{
		
		
		int ham_count=0,spam_count=0;
		File folder = new File(file);
		File[] files = folder.listFiles();
		
		for(int i=0;i<files.length;i++)
		{
			
			LinkedHashMap<String,Integer> temp_map = new LinkedHashMap<String,Integer>(); 
		
			File cur_file = files[i];
			String word=null;
		
		
		float HamProb =(float) Math.log(priorH)/(float)Math.log(2);

		float SpamProb = (float)Math.log(priorS)/(float)Math.log(2);
	
		
		
		
		 Scanner scan=new Scanner(cur_file);
		while(scan.hasNext())
		{

			word =scan.next();
	
		
			
	if(spl_char.contains(word)){
		 continue;
	 }
	if(stop.equalsIgnoreCase("yes") && stop_list.contains(word))
	{
		continue;
	}
			float temp1=0, temp2=0;
			if(HamMap.containsKey(word))
			{
	
			Integer word_cnt =HamMap.get(word);
			
	
	
			temp1 = (float)(word_cnt+1)/(float)(ham_tot + dis_total_words);
			temp1= (float)Math.log(temp1)/(float)Math.log(2);
	
			HamProb=HamProb+temp1;
			
			}// end of if
			else
			{
		
				Integer word_cnt=1;
				temp1 = (float)(word_cnt)/(float)(ham_tot + dis_total_words);
	
				temp1= (float)Math.log(temp1)/(float)Math.log(2);
	
				HamProb=HamProb+temp1;
				
			}// end of else
			
			
			if(SpamMap.containsKey(word))
			{
		
				Integer word_cnt =SpamMap.get(word);
		
			
			temp2 = (float)(word_cnt+1)/(float)(spam_tot + dis_total_words);
		
		
		temp2= (float)Math.log(temp2)/(float)Math.log(2);
			
			SpamProb=SpamProb+temp2;
				
			}// end of if
			else
			{
			
				Integer word_cnt=1;
			temp2 = (float)(word_cnt)/(float)(spam_tot + dis_total_words);
		
				temp2= (float)Math.log(temp2)/(float)Math.log(2);
	
				SpamProb=SpamProb+temp2;
				
			}// end of else
	 	
		}// end of while

			if(HamProb >= SpamProb)
			{
				ham_count++;
			}
			else
			{
				spam_count++;
			}
			
	
		}//end of for
		if(type==1)
		{
		
			float ham_percent= (ham_count*100)/files.length;
		System.out.println(" The percentage of occurence in ham folder is : " +ham_percent);
		}
		else
		{
			float spam_percent= (spam_count*100)/files.length;
			System.out.println(" The percentage of occurence in spam folder is : " +spam_percent);
		}
		
	}// end of method cal_accuracy
	
	// method to calculate total no of distinct words in a both spam and ham  folder 
	
	public static int count_tot_dis_words(LinkedHashMap Hmap, LinkedHashMap Smap)
	{
		int count=0;
		Integer temp=1;
		Set set1 = Hmap.entrySet();
		Iterator it1 = set1.iterator();
		while(it1.hasNext())
		{
			 Map.Entry me1 = (Map.Entry)it1.next();
	     
	         Dis_Words_Map.put(me1.getKey().toString(), 1);
		}
		
		
		Set set2 = Smap.entrySet();
		Iterator it2 = set2.iterator();
		while(it2.hasNext())
		{
			 Map.Entry me2 = (Map.Entry)it2.next();
	      
			 if(Dis_Words_Map.containsKey(me2.getKey()))
				 continue;
			 else
	         Dis_Words_Map.put(me2.getKey().toString(), 1);
		}
		
		count =Dis_Words_Map.size();
		
	return count;
		
	}
	
	// method to calculate total no of words in a one folder 
	public static int count_tot_words(LinkedHashMap map)
	{
		int count =0;
		Set set = map.entrySet();
		Iterator it = set.iterator();
		while(it.hasNext())
		{
			 Map.Entry me = (Map.Entry)it.next();
	   
	         count = count + (int)me.getValue();
		}
		
		return count;
		
	}
	
	
	//method to calculate total no of documents in a folder
	public static int total_docs_folder(String path)
	{
		return new File(path).listFiles().length;
	}// end of total_docs_folder method

        //method to create special characters list
         
        public static void create_special_char_list()
        {
            spl_char.add(".");
            spl_char.add("-");
            spl_char.add(":");
            spl_char.add("/");
            spl_char.add("@");
            spl_char.add(",");
            spl_char.add(" ");
            spl_char.add("%");
            spl_char.add("!");
            spl_char.add(">");
            spl_char.add("<");
            spl_char.add("=");
            spl_char.add("?");
            spl_char.add("[");
            spl_char.add("]");
            spl_char.add("'");
            spl_char.add("{");
            spl_char.add("}");
            spl_char.add("");
            spl_char.add("_");
            spl_char.add("$");
            spl_char.add("(");
            spl_char.add(")");
            spl_char.add("|");
            spl_char.add("*");
            spl_char.add("~");
            spl_char.add("`");
            spl_char.add(".");
            spl_char.add("#");
            spl_char.add("\"");
            spl_char.add("+");
            
        }
        
	// method to count the words in the documents in pathargument  folder
	
	public static void count_words(String path,LinkedHashMap map) throws FileNotFoundException
	{
		File folder = new File(path);
		File[] files = folder.listFiles();
		
		for(int i=0;i<files.length;i++)
		{
			File file = files[i];
			Scanner scan = new Scanner(file);
			while(scan.hasNext())
			{
				String word =scan.next();
				
				 if(spl_char.contains(word)){
					 continue;
				 }//end of if
				 else if(map.containsKey(word))
				 {
				 Integer count = (int)map.get(word);
				 count= count+1;
				 map.remove(word);
				 map.put(word, count);
					 
				 }//end of else if
				 else
					 map.put(word, 1);
			}// end of while
		}// end of for
	}// end of method count_words
	
	
	//method to build test documents map
	
	public static void count_doc_words(String path,LinkedHashMap map) throws FileNotFoundException
	{
		
			File file =new File(path);
			Scanner scan = new Scanner(file);
			while(scan.hasNext())
			{
				String word =scan.next();
				
				if(spl_char.contains(word)){
					 continue;
				 }//end of if
				 else if(map.containsKey(word))
				 {
				 Integer count = (int)map.get(word);
				 count= count+1;
				 map.remove(word);
				 map.put(word, count);
					 
				 }//end of else if
				 else
					 map.put(word, 1);
			}// end of while
		
	}// end of method count_words
	
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
       
        //method to return distinctwords map
	public static LinkedHashMap<String,Integer> get_distinct_words_map()
	{
		return Dis_Words_Map;
	}
	
	
	
	
	
	
}// end of class Naive Bayes
