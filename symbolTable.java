import syntaxtree.*;
import visitor.*;



import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.*;
import java.util.*;


class symbolTable extends GJDepthFirst<String, String>{ 

    static classDeclarationTable mydecls = new classDeclarationTable();
    
    Pair<String, String> mode = Pair.createPair(null, null); //helps us know whether we are currently within a method (<null, null> when we are not | <name, type> otherwise)


    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    @Override
    public String visit(MainClass n, String class_name) throws Exception { // 

        //Visit identifier to get class Name
        String classname = n.f1.accept(this, null);

        //add new class to the symbol table (HashMap)
        mydecls.add_newClass(classname, null);

        n.f1.accept(this, classname);
        n.f11.accept(this, classname);
        n.f14.accept(this, classname);
        n.f15.accept(this, classname);



        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    @Override
    public String visit(ClassDeclaration n, String class_name) throws Exception {

        //Visit identifier to get class Name
        String classname = n.f1.accept(this, null);

        //check if a class with this name already exists
        if(mydecls.table.containsKey(classname)){
            System.out.println("\n\nA class named: " + classname + " already exists!\n\n");
            throw new Exception();
        }

        //add new class to the symbol table (HashMap)       
        mydecls.add_newClass(classname, null);

        n.f3.accept(this, classname);
        n.f4.accept(this, classname);



        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    @Override
    public String visit(ClassExtendsDeclaration n, String class_name) throws Exception {

        //Visit identifier to get class Name
        String classname = n.f1.accept(this, null);

        //check if a class with this name already exists
        if(mydecls.table.containsKey(classname)){
            System.out.println("\n\nA class named: \"" + classname + "\" already exists!\n\n");
            throw new Exception();
        }


        

        
        //But when we have “class B extends A”, A must be defined before B
        String extendsName = n.f3.accept(this, null);

        if(!mydecls.table.containsKey(extendsName)){ //If there is no class with such key
            System.out.println("\n\nClass \"" + classname + "\" extends a class that has not been declared before " + classname + "!\n\n");
            throw new Exception();
        }


        //add new class to the symbol table (HashMap) and its extend
        mydecls.add_newClass(classname, extendsName);

        n.f5.accept(this, classname);
        n.f6.accept(this, classname);



        return null;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    @Override
    public String visit(MethodDeclaration n, String class_name) throws Exception {
        //Get method argument list
        String argumentList = n.f4.present() ? n.f4.accept(this, null) : "";


        //Get method type and name
        String myType = n.f1.accept(this, null);
        
        
        String myName = n.f2.accept(this, null);

        //Check if a method with this name already exists!

    
        mode.setElements(myName, myType); //set current method (will change to null when we are done)
        if(mydecls.check_methodDuplicates(class_name, mode)){
            System.out.println("\n\nA method named: \"" + myName + "\" already exists in class " + class_name + "!\n\n");
            throw new Exception();
        }

        //(check read me) the method map consists of the method name and a vector containing
        //a HashMap for the method's arguments and a HashMap forthe method's variables declarations
        Vector methodContains = mydecls.create_methodVector();

        //This is the HashMap of this method's arguments
        HashMap<String,String> temp_argMap = (HashMap<String,String>) methodContains.get(0);

        

        //Now let's break the argumentList which is like "int i, int j"
        //to words so we can get the type and name of each variable
        //and add it to the method's vector
        String[] splitString = argumentList.split(", ");


        for (String s : splitString) {
            // s is like: int i
            String argType = ""; // this will hold "int"
            String argName = ""; // this will hold "i"

            
            //split using whitespace
            String[] words = s.split(" ");
            
            // tok is either a type or a name
            int i=0;
            for (String tok : words){

                if(tok == ""){// to avoid empty argument list
                    continue;
                }

                if(i==0){//tok is a type(int/boolean etc.)

                    argType = tok;

                    i++; //increment i

                    
                }else if(i==1){//tok is a variable name
                    
                    argName = tok;

                    i=0; //reset


                }

                
                
            }
            
            //check for duplicate
            if(temp_argMap.containsKey(argName)){
                System.out.println("\n\nA variable named: \"" + argName + "\" already exists in " + myName + " arguments!\n\n");
                throw new Exception();
            }

            //put this argument to the method's argument HashMap
            if(argumentList != "") temp_argMap.put(argName, argType);

        }

        

        //Get class Contains from symbol table 
        Vector temp = mydecls.table.get(class_name);


        //To update its contains hashmaps ( variable, method hashmaps)
        HashMap<Pair<String,String>,Vector> tempmap = (HashMap<Pair<String,String>,Vector>) temp.get(1);

        //create a pair of 2 strings (name and type) to store in the method HashMap as a key to the method's vector
        Pair<String, String> info = Pair.createPair(myName, myType);
        

        //Store new method name and type in class contains vector
        tempmap.put(info, methodContains);


        //Now it's time to accept the varDeclarations of the method ( mode != null )
        
        n.f7.accept(this, class_name);
        mode.setElements(null, null);

        //System.out.println("Class einai " + class_name);
        //System.out.println(myType + " " + myName + " -- " + argumentList);
        //System.out.println(argumentList);

        return null;
    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    public String visit(VarDeclaration n, String class_name) throws Exception { //mode = null -> class variable declarations | mode = methodName -> method variable declarations
        

        String myType = n.f0.accept(this, null);
        String myName = n.f1.accept(this, null);

        //Get class Contains from symbol table 
        Vector temp = mydecls.table.get(class_name);



        if(mode.getElement0() == null && mode.getElement1() == null){//mode = <null,null> -> class variable declarations


            //To update its contains hashmaps ( variable, method hashmaps)
            HashMap<String,String> tempmap = (HashMap<String,String>) temp.get(0);

            //check for duplicate
            if(tempmap.containsKey(myName)){
                System.out.println("\n\nA variable named: \"" + myName + "\" already exists in class " + class_name + "!\n\n");
                throw new Exception();
            }

            //Store new variable name and type in class contains vector
            tempmap.put(myName, myType);

        }else{ //mode = methodName -> method variable declarations

            //get method HashMaps to get Vector
            HashMap<Pair<String,String>,Vector> tempmap = (HashMap<Pair<String,String>,Vector>) temp.get(1);
            Vector temp_methodVector = tempmap.get(mode);
            // System.out.println("EKEI " + temp_methodVector + myType + " " + myName + "KENA   " + mode);
            
            
            HashMap<String,String> temp_methodMap = (HashMap<String,String>) temp_methodVector.get(0);

            
            //check for duplicate in method's arguments
            if(temp_methodMap.containsKey(myName)){
                System.out.println("\n\nA variable named: \"" + myName + "\" already exists in method " + mode.getElement0() + " arguments!\n\n");
                throw new Exception();
            }




            temp_methodMap = (HashMap<String,String>) temp_methodVector.get(1);

            
            //check for duplicate in method's variables
            if(temp_methodMap.containsKey(myName)){
                System.out.println("\n\nA variable named: \"" + myName + "\" already exists in method " + mode.getElement0() + "!\n\n");
                throw new Exception();
            }

            //Store new variable name and type in class contains vector
            temp_methodMap.put(myName, myType);

        }
        

        

        //System.out.println("variables" + myType + " " + myName + " -- ");
        

        return null;
    }



    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterList n, String class_name) throws Exception {
        String ret = n.f0.accept(this, null);

        if (n.f1 != null) {
            ret += n.f1.accept(this, null);
        }

        return ret;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    public String visit(FormalParameterTerm n, String class_name) throws Exception {
        return n.f1.accept(this, class_name);
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    @Override
    public String visit(FormalParameterTail n, String class_name) throws Exception {
        String ret = "";
        for ( Node node: n.f0.nodes) {
            ret += ", " + node.accept(this, null);
        }

        return ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n, String class_name) throws Exception{
        String type = n.f0.accept(this, null);
        String name = n.f1.accept(this, null);
        return type + " " + name;
    }


    /**
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    */
    public String visit(Type n, String argument) throws Exception {
        String myType = n.f0.accept(this, null);

        // if(argument == "extends"){

        //     if( myType != "int" && myType != "boolean" && myType != "int[]" && !(mydecls.table.containsKey(myType))){
        //         System.out.println("\n\nType " + myType + " is not a valid type!\n\n");
        //         throw new Exception();
        //     }
    
        // }
        
        return myType;
    }

    @Override
    public String visit(ArrayType n, String class_name) {
        return "int[]";
    }

    public String visit(BooleanType n, String class_name) {
        return "boolean";
    }

    public String visit(IntegerType n, String class_name) {
        return "int";
    }

    @Override
    public String visit(Identifier n, String class_name) {
        return n.f0.toString();
    }
}
