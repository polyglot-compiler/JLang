// Technische Universitaet Muenchen 
// Fakultaet fuer Informatik 
// Riitta Hoellerer
//
// Praktikum des Uebersetzerbaus 
// SS 2001
//
// interface containing constants (for Lexer, IdentificationVisitor and 
// EmitCodeVisitor)
//
package minijava;
public interface Constants {

  public static final int INTTYPE  = 1;
  public static final int BOOLTYPE = 2;

  public static final int TRUE  = 1;
  public static final int FALSE = 0;
 
  public static final int PLUS  = 1;
  public static final int MINUS = 2;
  public static final int MULT  = 3;
  public static final int DIV   = 4;
  public static final int MOD   = 5;

  public static final int LEQ    = 1;
  public static final int LE     = 2;
  public static final int GT     = 3;
  public static final int GTQ    = 4;
  public static final int EQ     = 5;
  public static final int NEQ    = 6;
  public static final int AND    = 7;
  public static final int OR     = 8;


}
