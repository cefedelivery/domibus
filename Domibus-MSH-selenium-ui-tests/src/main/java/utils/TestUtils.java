package utils;

import java.text.Collator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TestUtils {

	/* Checks if List provided is sorted desc*/
	public static boolean isStringListSorted(List<String> strings){
		
		Collator collator = Collator.getInstance(Locale.ENGLISH);
		collator.setStrength(Collator.PRIMARY);
		
		for (int i = 0; i < strings.size()-1; i++) {
			if(collator.compare(strings.get(i), strings.get(i+1))>0){return false;}
		}
		return true;
	}

	/* Checks if List provided is sorted desc*/
	public static boolean isDateListSorted(List<Date> dates){
		for (int i = 0; i < dates.size()-1; i++) {
			if(dates.get(i).compareTo(dates.get(i+1))<0){return false;}
		}
		return true;
	}

	public static boolean areListsEqual(List<Object> flist, List<Object> slist){
		if(flist.size() != slist.size()){ return false;}
		flist.removeAll(slist);
		return flist.size() == 0;
	}

}
