package miniparser;
import java_cup.runtime.ComplexSymbolFactory;

import java.io.*;
import minijava.*;
public class Main {


 /**
   * Runs the parser on an input file.
   *
   *
   * @param argv   the command line, argv[1] is the filename to run
   *               the parser on, argv[0] determines the compiler mode
   */
  public static void main(String argv[]) 
    throws java.io.IOException, java.lang.Exception
 {
    Lexer scanner = null;
    ComplexSymbolFactory csf = new ComplexSymbolFactory();
    try {
      scanner = new Lexer( new java.io.FileReader(argv[1]),csf );
    }
    catch (java.io.FileNotFoundException e) {
      System.err.println("File not found : \""+argv[1]+"\"");
      System.exit(1);
    }
    catch (ArrayIndexOutOfBoundsException e) {
      System.err.println("Usage : java Main <MODE> <inputfile>");
      System.err.println("  with MODE ::= -jvm | -syntax | -cfg");
      System.exit(1);
    }

    try {
      Parser p = new Parser(scanner,csf);
      Program result = (Program)p.parse().value;
      if (argv[0].equals("-jvm")) {
          MiniJVMGenerator jvm = new MiniJVMGenerator(argv[1]);
          result.accept(jvm);
          jvm.flush();
      }
      if (argv[0].equals("-syntax")) {
          SyntaxTreeGenerator syntax = new SyntaxTreeGenerator(argv[1]);
          result.accept(syntax);
          syntax.flush();
      }
      if (argv[0].equals("-cfg")) {
          CFGGenerator syntax = new CFGGenerator(argv[1]);
          result.accept(syntax);
          syntax.flush();
      }
    }
    catch (java.io.IOException e) {
      System.err.println("An I/O error occured while parsing : \n"+e);
      System.exit(1);
    }
  }
}




