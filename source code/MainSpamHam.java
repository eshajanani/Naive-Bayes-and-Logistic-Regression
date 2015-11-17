import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class MainSpamHam {
	


	public static void main(String[] args) throws FileNotFoundException 
{
	LinkedHashMap<String,Integer> dis_word_map = new LinkedHashMap<String,Integer>();
	
		
	    
	    NaiveBayes nb = new NaiveBayes(args[0],args[1],args[2],args[3],args[4]);
	    nb.do_naive_bayes();
		
		dis_word_map= nb.Dis_Words_Map;
		LogisticRegression lr = new LogisticRegression(args[0],args[1],args[2],args[3],args[4]);
		lr.do_Log_Regression(dis_word_map);
		
}// end of main method
	
	
	
	

	
}// end of class
