package com.ravello.utils.logs;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;

/**
 * A pattern layout class for logback, this one will throw away all the frames that were not requested by the <include>
 * directive at logback.xml
 * 
 * @author Ilan Goldenstein
 *
 */
public class ExceptionFilteredPatternLayout extends PatternLayout
{

	/**
     * Holds the list of included frames.
     */
    private Set<String> includedFrames = new HashSet<String>();
    
    /**
     * Should i include all frames up to the first "include" anyway?
     */
    private boolean includeAllFramesUpToFirstExplicitInclusion = true ;
    
	/**
     * This will remove all elements that are not in the "inclusionPatterns" in a logging event that contains an exception 
     */
    @Override
	public String doLayout(ILoggingEvent event) {
    	IThrowableProxy throwableProxy = event.getThrowableProxy() ;
    	if (throwableProxy == null || includedFrames.size() == 0) {
    		//This is a regular message, no exception.
    		//Either that or the user didn't request any filter inclusions, go back
    		return super.doLayout(event) ;
    	}
    	
    	//Otherwise we have to deal with them...
    	
    	//The list of the stacktrace elements that eventually will be included
    	List<StackTraceElementProxy> steInclusionList = new LinkedList<StackTraceElementProxy>() ;
    	StackTraceElementProxy[] steOriginalList = throwableProxy.getStackTraceElementProxyArray() ;
    	
    	//Variable to use when we want to show all frames up to the first explicit inclusion
    	boolean firstExclusiveInclusionAlreadyIncluded = false ;
    	if (!includeAllFramesUpToFirstExplicitInclusion) {
    		firstExclusiveInclusionAlreadyIncluded = true ;
    	}
    	
    	//Variable to keep where the last explicit inclusion took place
    	int lastExplicitInclusion = 0;
    	
    	for (int i = 0; i < steOriginalList.length; i++) {
    		if (startsWithAnIncludedPattern(steOriginalList[i].getSTEAsString())) { 
    			steInclusionList.add(steOriginalList[i]) ;
    			firstExclusiveInclusionAlreadyIncluded = true ;
    			lastExplicitInclusion = i ;
    	
    		} else if (!firstExclusiveInclusionAlreadyIncluded) {
    			//Or if we still haven't found the first explicit included frame
    			steInclusionList.add(steOriginalList[i]) ;
    		} 
    	}
    	
    	//And add the last frames..
    	for (int i = lastExplicitInclusion + 1 ; i < steOriginalList.length; i++) {
    		steInclusionList.add(steOriginalList[i]) ;
    	}
    	
    	//If everything has been included anyway, we can skip the reflection part
    	//This is just for "performance", we could do the reflection part anyway (without the following "if")
    	if (steInclusionList.size() == steOriginalList.length) {
    		return super.doLayout(event) ;
    	}
    	IThrowableProxy tProxy = event.getThrowableProxy() ;
    	try {
    		//Set the new stack element array, since this is a private field on the original Object, we need to do this reflection thingy
			Field field = tProxy.getClass().getDeclaredField("stackTraceElementProxyArray") ;
			field.setAccessible(true) ;
			field.set(tProxy, steInclusionList.toArray(new StackTraceElementProxy[0])) ;
		} catch (Exception ex) {
			ex.printStackTrace() ;
		}
		return super.doLayout(event);
	}

    /**
     * Returns whether the parameter starts with one of the inclusion pattern.
     * 
     * @param stackTraceElementString
     * @return
     */
    private boolean startsWithAnIncludedPattern(String stackTraceElementString)
    {
        Iterator<String> iterator = includedFrames.iterator();
        while (iterator.hasNext())
        {
            if (stackTraceElementString.trim().startsWith(iterator.next()))
            {
                return true;
            }
        }
        return false;
    }
    
    public void setInclude(String include) {
    	includedFrames.add("at " + include) ;
    }
    
    public void setIncludeAllFramesUpToFirstExplicitInclusion(
			boolean includeAllFramesUpToFirstExplicitInclusion) {
		this.includeAllFramesUpToFirstExplicitInclusion = includeAllFramesUpToFirstExplicitInclusion;
	}

}