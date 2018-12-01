/**
 * java_cup.anttask.CUPTask.java
 *
 * @author Michael Petter, 2003
 *
 * Ant-Task for CUP Parser Generator for Java
 * -- tested with Ant 1.5.1;
 * -- compiles with javac -classpath .:${ANT_HOME}/lib/ant.jar java_cup.anttask.CUPTask.java
 * -- overrides org.apache.tools.ant.taskdefs.Java
 * -- providing cool interface to CUP
 * -- mapping all existing parameters to attributes
 * -- trys to add new useful features to CUP, like 
 *     - automatic package discovery
 *     - re-generate .java only when necessary
 *     - possibility to generate into a dest-directory
 *
 * my code is not perfect (in some cases it is pretty
 * ugly :-) ), but i didn't encounter any major error
 * until now
 */

package java_cup.anttask;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;

import java_cup.version;

public class CUPTask extends Task 
{
    private String srcfile=null;
    private String parser=null;
    private String _package=null;
    private String symbols=null;
    private String destdir=null;
    private boolean _interface=false;
    private boolean nonterms=false;
    private String expect=null;
    private boolean compact_red=false;
    private boolean nowarn=false;
    private boolean nosummary=false;
    private boolean progress=false;
    private boolean dump_grammar=false;
    private boolean dump_states=false;
    private boolean dump_tables=false;
    private boolean dump=false;
    private boolean time=false;
    private boolean debug=false;
    private boolean debugsymbols=false;
    private boolean nopositions=false;
    private boolean xmlactions=false;
    private boolean genericlabels=false;
    private boolean locations=true;
    private boolean noscanner=false;
    private boolean force=false;
    private boolean quiet=false;
  
    /**
     * executes the task
     * parses all attributes and validates options...
     *
     */

    public void execute() throws BuildException 
    {
        List sc = new ArrayList();  // sc = simulated commandline
	// here, we parse our elements
	if (parser!=null)  { sc.add("-parser"); sc.add(parser);}
        else parser="parser"; // set the default name to check actuality
	if (_package!=null){ sc.add("-package"); sc.add(_package); }
	if (symbols!=null) { sc.add("-symbols"); sc.add(symbols); }
        else symbols="sym";
	if (expect!=null)  {  sc.add("-expect"); sc.add(expect); }
	if (_interface)    {  sc.add("-interface"); }
	if (nonterms)      {  sc.add("-nonterms"); }
	if (compact_red)   {  sc.add("-compact_red"); }
	if (nowarn)        {  sc.add("-nowarn"); }
	if (nosummary)     {  sc.add("-nosummary");}
	if (progress)      {  sc.add("-progress"); }
	if (dump_grammar)  {  sc.add("-dump_grammar"); }
	if (dump_states)   {  sc.add("-dump_states"); }
	if (dump_tables)   {  sc.add("-dump_tables"); }
	if (dump)          {  sc.add("-dump"); }
	if (time)          {  sc.add("-time"); }
	if (debug)         {  sc.add("-debug"); }
	if (debugsymbols)  {  sc.add("-debugsymbols"); }
	if (nopositions)   {  sc.add("-nopositions"); }
	if (locations)     {  sc.add("-locations"); }
	if (genericlabels) {  sc.add("-genericlabels"); }
	if (xmlactions)    {  sc.add("-xmlactions"); }
	if (noscanner)     {  sc.add("-noscanner"); }
	if (!quiet) log ("This is "+version.title_str);
        if (!quiet) log ("Authors : "+version.author_str);
	if (!quiet) log ("Bugreports to petter@cs.tum.edu");

	// look for package name and add to destdir
	String packagename = inspect(srcfile);
	

	// now, that's sweet:
	if (destdir==null) {
	    destdir=System.getProperty("user.dir");
	    if (!quiet) log("No destination directory specified; using working directory: "+destdir);
	}
	File dest = new File(destdir+packagename);
	if (!(dest).exists()) {
	    if (!quiet) log("Destination directory didn't exist; creating new one: "+destdir+packagename);
	    dest.mkdirs();
	    force=true;
	}
	else {
	    if (force&& !quiet) { log("anyway, this generation will be processed because of option force set to \"true\""); }
	    else { if (!quiet) log("checking, whether this run is necessary"); }
	    // let's check, whether there exists any Parser fragment...
	    File parserfile = new File(destdir+packagename,parser+".java");
	    File symfile    = new File(destdir+packagename,symbols+".java");
	    File cupfile    = new File(srcfile);
	    
	    if (!parserfile.exists() || !symfile.exists()) {
		if (!quiet) log("Either Parserfile or Symbolfile didn't exist");
		force=true;	    
	    }else { if (!quiet) log("Parserfile and symbolfile are existing"); }
	    
	    
	    if (parserfile.lastModified()<=cupfile.lastModified()) {
		if (!quiet) log("Parserfile "+parserfile+" isn't actual");
		force=true;
	    }else { if (!quiet) log("Parserfile "+parserfile+" is actual"); }
	    
	    if (symfile.lastModified()<=cupfile.lastModified()) {
		if (!quiet) log("Symbolfile "+symfile+" isn't actual");
		force=true;
	    }else { if (!quiet) log("Symbolfile"+symfile+" is actual"); }
	    
	    
	    if (!force) {
		if (!quiet) log("skipping generation of "+srcfile);
		if (!quiet) log("use option force=\"true\" to override");
		return;
	    }
	}
        
	sc.add("-destdir");
        sc.add(dest.getAbsolutePath());
        
        // also catch the not existing input file
	if (srcfile==null) throw new BuildException("Input file needed: Specify <cup srcfile=\"myfile.cup\"> ");
	if (!(new File(srcfile)).exists()) throw new BuildException("Input file not found: srcfile=\""+srcfile+"\" ");
	
        sc.add(srcfile);
	String[] args = new String[sc.size()];
        for (int i=0;i<args.length;i++) args[i]=(String)sc.get(i);
        

	try {
            java_cup.Main.main(args);
        }catch(Exception e){
            log("CUP error occured int CUP task: "+e);
        }
	
    }

    /**
     * Let's search for package name
     *
     * @param cupfile where we have to search for the package name
     *
     * @return the package folder structure
     */
    protected String inspect(String cupfile){
	try{
	BufferedReader br = new BufferedReader(new FileReader(cupfile));
	while (br.ready()){
	    String line = br.readLine();
	    if ((line.startsWith("package"))&&(line.indexOf(";")!=-1))
		{
		    String result = line.substring(8,line.indexOf(";"));
		    result = result.replace('.',System.getProperty("file.separator").charAt(0));
		    return System.getProperty("file.separator") + result;
		}
	    
	}
	}catch (IOException ioe){
	}
	return "";
    }

    /**
     * Gets the value of quiet
     *
     * @return the value of quiet
     */
    public boolean getQuiet()  {
	return this.quiet;
    }

    /**
     * Sets the value of quiet
     *
     * @param arg_quiet Value to assign to this.quiet
     */
    public void setQuiet(boolean argquiet) {
	this.quiet = argquiet;
    }
    /**
     * Gets the value of force
     *
     * @return the value of force
     */
    public boolean getForce()  {
	return this.force;
    }

    /**
     * Sets the value of force
     *
     * @param arg_package Value to assign to this.force
     */
    public void setForce(boolean argforce) {
	this.force = argforce;
    }
    /**
     * Gets the value of _package
     *
     * @return the value of _package
     */
    public String getPackage()  {
	return this._package;
    }

    /**
     * Sets the value of _package
     *
     * @param arg_package Value to assign to this._package
     */
    public void setPackage(String arg_package) {
	this._package = arg_package;
    }

    /**
     * Gets the value of destdir
     *
     * @return the value of destdir
     */
    public String getDestdir()  {
	return this.destdir;
    }

    /**
     * Sets the value of destdir
     *
     * @param arg_package Value to assign to this.destdir
     */
    public void setDestdir(String destdir) {
	this.destdir = destdir;
    }

    /**
     * Gets the value of _interface
     *
     * @return the value of _interface
     */
    public boolean isInterface()  {
	return this._interface;
    }

    /**
     * Sets the value of _interface
     *
     * @param arg_interface Value to assign to this._interface
     */
    public void setInterface(boolean arg_interface) {
	this._interface = arg_interface;
    }

    /**
     * Get the Srcfile value.
     * @return the Srcfile value.
     */
    public String getSrcfile() {
	return srcfile;
    }

    /**
     * Set the Srcfile value.
     * @param newSrcfile The new Srcfile value.
     */
    public void setSrcfile(String newSrcfile) {
	this.srcfile = newSrcfile;
    }

  

    /**
     * Gets the value of parser
     *
     * @return the value of parser
     */
    public String getParser() {
	return this.parser;
    }

    /**
     * Sets the value of parser
     *
     * @param argParser Value to assign to this.parser
     */
    public void setParser(String argParser){
	this.parser = argParser;
    }

    /**
     * Gets the value of symbols
     *
     * @return the value of symbols
     */
    public String getSymbols() {
	return this.symbols;
    }

    /**
     * Sets the value of symbols
     *
     * @param argSymbols Value to assign to this.symbols
     */
    public void setSymbols(String argSymbols){
	this.symbols = argSymbols;
    }

    /**
     * Gets the value of nonterms
     *
     * @return the value of nonterms
     */
    public boolean isNonterms() {
	return this.nonterms;
    }

    /**
     * Sets the value of nonterms
     *
     * @param argNonterms Value to assign to this.nonterms
     */
    public void setNonterms(boolean argNonterms){
	this.nonterms = argNonterms;
    }

    /**
     * Gets the value of expect
     *
     * @return the value of expect
     */
    public String getExpect() {
	return this.expect;
    }

    /**
     * Sets the value of expect
     *
     * @param argExpect Value to assign to this.expect
     */
    public void setExpect(String argExpect){
	this.expect = argExpect;
    }

    /**
     * Gets the value of compact_red
     *
     * @return the value of compact_red
     */
    public boolean isCompact_red() {
	return this.compact_red;
    }

    /**
     * Sets the value of compact_red
     *
     * @param argCompact_red Value to assign to this.compact_red
     */
    public void setCompact_red(boolean argCompact_red){
	this.compact_red = argCompact_red;
    }

    /**
     * Gets the value of nowarn
     *
     * @return the value of nowarn
     */
    public boolean isNowarn() {
	return this.nowarn;
    }

    /**
     * Sets the value of nowarn
     *
     * @param argNowarn Value to assign to this.nowarn
     */
    public void setNowarn(boolean argNowarn){
	this.nowarn = argNowarn;
    }

    /**
     * Gets the value of nosummary
     *
     * @return the value of nosummary
     */
    public boolean isNosummary() {
	return this.nosummary;
    }

    /**
     * Sets the value of nosummary
     *
     * @param argNosummary Value to assign to this.nosummary
     */
    public void setNosummary(boolean argNosummary){
	this.nosummary = argNosummary;
    }

    /**
     * Gets the value of progress
     *
     * @return the value of progress
     */
    public boolean isProgress() {
	return this.progress;
    }

    /**
     * Sets the value of progress
     *
     * @param argProgress Value to assign to this.progress
     */
    public void setProgress(boolean argProgress){
	this.progress = argProgress;
    }

    /**
     * Gets the value of dump_grammar
     *
     * @return the value of dump_grammar
     */
    public boolean isDump_grammar() {
	return this.dump_grammar;
    }

    /**
     * Sets the value of dump_grammar
     *
     * @param argDump_grammar Value to assign to this.dump_grammar
     */
    public void setDump_grammar(boolean argDump_grammar){
	this.dump_grammar = argDump_grammar;
    }

    /**
     * Gets the value of dump_states
     *
     * @return the value of dump_states
     */
    public boolean isDump_states() {
	return this.dump_states;
    }

    /**
     * Sets the value of dump_states
     *
     * @param argDump_states Value to assign to this.dump_states
     */
    public void setDump_states(boolean argDump_states){
	this.dump_states = argDump_states;
    }

    /**
     * Gets the value of dump_tables
     *
     * @return the value of dump_tables
     */
    public boolean isDump_tables() {
	return this.dump_tables;
    }

    /**
     * Sets the value of dump_tables
     *
     * @param argDump_tables Value to assign to this.dump_tables
     */
    public void setDump_tables(boolean argDump_tables){
	this.dump_tables = argDump_tables;
    }

    /**
     * Gets the value of dump
     *
     * @return the value of dump
     */
    public boolean isDump() {
	return this.dump;
    }

    /**
     * Sets the value of dump
     *
     * @param argDump Value to assign to this.dump
     */
    public void setDump(boolean argDump){
	this.dump = argDump;
    }

    /**
     * Gets the value of time
     *
     * @return the value of time
     */
    public boolean isTime() {
	return this.time;
    }

    /**
     * Sets the value of time
     *
     * @param argTime Value to assign to this.time
     */
    public void setTime(boolean argTime){
	this.time = argTime;
    }

    /**
     * Gets the value of debug
     *
     * @return the value of debug
     */
    public boolean isDebug() {
	return this.debug;
    }

    /**
     * Sets the value of debug
     *
     * @param argDebug Value to assign to this.debug
     */
    public void setDebug(boolean argDebug){
	this.debug = argDebug;
    }
    
    /**
      * Gets the value of debug
     *
      * @return the value of debug
      */
    public boolean isDebugSymbols() {
 	return this.debugsymbols;
    }
 
     /**
      * Sets the value of debug
      *
      * @param argDebug Value to assign to this.debug
      */
    public void setDebugSymbols(boolean argDebug){
 	this.debugsymbols = argDebug;
    }
 
     /**
     * Gets the value of nopositions
     *
     * @return the value of nopositions
     */
    public boolean isNopositions() {
	return this.nopositions;
    }

    /**
     * Sets the value of nopositions
     *
     * @param argNopositions Value to assign to this.nopositions
     */
    public void setNopositions(boolean argNopositions){
	this.nopositions = argNopositions;
    }
    /**
     * Gets the value of locations
     *
     * @return the value of locations
     */
    public boolean isLocations() {
	return this.locations;
    }

    /**
     * Sets the value of locations
     *
     * @param argLocations Value to assign to this.locations
     */
    public void setLocations(boolean argLocations){
	this.locations = argLocations;
    }

    /**
     * Gets the value of noscanner
     *
     * @return the value of noscanner
     */
    public boolean isNoscanner() {
	return this.noscanner;
    }

    /**
     * Sets the value of noscanner
     *
     * @param argNoscanner Value to assign to this.noscanner
     */
    public void setNoscanner(boolean argNoscanner){
	this.noscanner = argNoscanner;
    }

	public boolean isXmlactions() {
		return xmlactions;
	}

	public void setXmlactions(boolean xmlactions) {
		this.xmlactions = xmlactions;
	}

	public boolean isGenericlabels() {
		return genericlabels;
	}

	public void setGenericlabels(boolean genericlabels) {
		this.genericlabels = genericlabels;
	}


}

  
  
