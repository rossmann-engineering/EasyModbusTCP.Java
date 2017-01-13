package de.re.easymodbus.modbusclient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
/**
*
* @author Stefan Rossmann
*/
public class DateTime 
{
    /**
    * Returns the current DateTime in Ticks (one ms = 10000ticks)
    * @return Current Date and Time in Ticks
    */    
	protected static long getDateTimeTicks()
	{
		long TICKS_AT_EPOCH = 621355968000000000L; 
		long tick = System.currentTimeMillis()*10000 + TICKS_AT_EPOCH;
		return tick;
	}
	
    /**
    * Returns the current DateTme in String Format yyyy/MM/dd HH:mm:ss
    * @return current DateTme in String Format yyyy/MM/dd HH:mm:ss
    */    
	protected static String getDateTimeString()
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		return(dateFormat.format(cal.getTime()));
	}
}
