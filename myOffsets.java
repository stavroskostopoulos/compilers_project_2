import syntaxtree.*;
import visitor.*;



import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.WrongMethodTypeException;
import java.io.*;
import java.util.*;


public class myOffsets extends symbolTable { //extend symbol table to have access to it

    HashMap<String,Pair<String,String>> saveOffsets = new HashMap<String,Pair<String,String>>();

    myOffsets(){

        for( String className : mydecls.table.keySet() ){

            printMyOffset(className);
                      
        }

    }

    public void printMyOffset(String className){

        Integer myVarOffs = 0;
        Integer myMethOffs = 0;

        if(mydecls.check_extend(className)){ //check whether we extend 
            //if we do
            //get extended class name
            String extendsName = mydecls.get_extendClass(className);

            //get extended class last offset
            Pair<String,String> myOffs = saveOffsets.get(extendsName);

            myVarOffs = Integer.parseInt(myOffs.getElement0());
            myMethOffs = Integer.parseInt(myOffs.getElement1());


        }

        // System.out.println(className + "." + varName + ": " + myOffs);

        //Print variables

        // Get the variables of the class
        HashMap<String,String> myVars =  mydecls.getClassVariables(className);

        for( Map.Entry<String, String> entry : myVars.entrySet() ){

            String myType = (String) entry.getValue();

            System.out.println(className + "." + entry.getKey() + ": " + myVarOffs);
            
            // if((entry.getValue()).equals("int")) myVarOffs += 4; System.out.println("MPHKAAAAAAA  ");

            if(myType.equals("int") ){
                myVarOffs += 4; 
            }else if(myType.equals("boolean") ){
                myVarOffs += 1; 
            }else if(myType.equals("int[]") ){
                myVarOffs += 8; 
            }else{//its a class variable
                myVarOffs += 8; 

            }



            
            
        }


        //Print methods
        HashMap<Pair<String,String>,Vector> myMeth =  mydecls.getClassMethods(className);

        for( Pair<String,String> myMethod : myMeth.keySet() ){


            System.out.println(className + "." + myMethod.getElement0() + ": " + myMethOffs);
            
            myMethOffs += 8;
            
        }

        Pair<String,String> newOffs = Pair.createPair(myVarOffs.toString(), myMethOffs.toString());

        saveOffsets.put(className, newOffs);


    }


}