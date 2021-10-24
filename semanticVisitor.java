import syntaxtree.*;
import visitor.*;



import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.WrongMethodTypeException;
import java.io.*;
import java.util.*;
import java.lang.reflect.Array;


class semanticVisitor extends symbolTable{ //extends so we can use the symbol Table
    
    String current_className = null; //Thsi will keep the name of the class we are in (We are always within a class body! This is Java :] !)

    //This HashMap will be used only when we are checking MessageSend
    //it will keep the ExpressionList arguments and types so we can compare with the ones when the method was declared
    LinkedHashMap<String,String> currentArgumentList = new LinkedHashMap<String,String>(); 

    
    //We will use Pair<string,string> mode variable from the super class
    //to keep track of which method we are in in each occasion
    //If we are not into a method mode is <null,null>



    int flag=0; //check ExpressionTail to understand why we need this

    semanticVisitor(){
        System.out.println( "PHRA TO:   " + mydecls.table );
    }
    
    
    
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
    
    public String visit(MainClass n, String argu) throws Exception {
        String classname = n.f1.accept(this, null);

        current_className = classname;


        n.f11.accept(this, classname);
        n.f14.accept(this, classname);
        n.f15.accept(this, classname);

        current_className = null; //clear className before we move to read the next class
        System.out.println();

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
    
    public String visit(ClassDeclaration n, String argu) throws Exception {
        String classname = n.f1.accept(this, null);

        current_className = classname;

        n.f3.accept(this, classname);
        n.f4.accept(this, classname);

        current_className = null; //clear className before we move to read the next class
        
        System.out.println();

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
    
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception {
        String classname = n.f1.accept(this, null);
        System.out.println("Class: " + classname);

        current_className = classname;

        n.f3.accept(this, classname);
        n.f5.accept(this, classname);
        n.f6.accept(this, classname);
        
        current_className = null; //clear className before we move to read the next class

        System.out.println();

        return null;
    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    public String visit(VarDeclaration n, String argu) throws Exception {
        
        String myType = n.f0.accept(this, null);
        String myName = n.f1.accept(this, null);


        //get the type! We gotta check whether it is int, int[],
        //boolean, or a name of a class in the symbol table
        
        if( !myType.equals( "int") && !myType.equals("boolean") && !myType.equals("int[]") && !mydecls.table.containsKey(myType)){
            System.out.println("\n\nUnknown type on declaration of variable " + myName + "!\n\n");
            throw new Exception();
        }

        if( mydecls.table.containsKey(myName) ){ //if the name of the new variable is a variable type
            System.out.println("\n\n" + myName + " is a variable type and cannot be used as a variable name!\n\n");
            throw new Exception();
        }

        
        n.f2.accept(this, null);
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
    
    public String visit(MethodDeclaration n, String argu) throws Exception {
        String argumentList = n.f4.present() ? n.f4.accept(this, null) : "";

        String myType = n.f1.accept(this, null);
        String myName = n.f2.accept(this, null);

        // before we move to anything 
        // change Pair mode content 
        //so we knwo that we are within a method

        mode.setElements(myName, myType); //check class' variable to see how this works :)

        //we need to test argument types!
        if(argumentList != ""){

            // split the argument list
            String[] argumentArray = argumentList.split("\\s*,\\s*");

            // System.out.println(Arrays.toString(argumentArray));
            for(int i = 0; i < argumentArray.length; i++){
                
                //int i , int j , int z
                String[] splitArray = argumentArray[i].split(" ");

                // splitArray[0] == int | splitArray[1] == i
                String argType = (String)Array.get(splitArray, 0);
                String argName = (String)Array.get(splitArray, 1);


                if( !argType.equals("int") && !argType.equals("boolean") && !argType.equals("int[]") && !mydecls.table.containsKey(argType)){
                    System.out.println("\n\n" + argName + " is unknown type of argument (" + argType + ")! \n\n");
                    throw new Exception();
                }


                // System.out.println(argType);
               
            }      
        }


        
        n.f7.accept(this,null);
        n.f8.accept(this,null);
        



        //just gotta check if expression,that maybe is a varType variable or a varType expr or a varType method, is valid
        //n.f10.accept(this, "type") == null if its not expr or variable
        //n.f10.accept(this, "methodtype") == null if its not a method (message send)
        
        
        //check return types
        String rettype = n.f10.accept(this, null );

        String returnType1 = n.f10.accept(this,"type");
        String returnType2 = n.f10.accept(this,"methodtype");



        if(returnType1 == null && returnType2==null){
            // System.out.println("\n\nUnknown method return type! \n\n");
            System.out.println("\n\nInvalid method return type! \n\n");
            throw new Exception();
        }else if(returnType1 == null){
            if(!returnType2.equals(myType)){
                System.out.println("\n\nInvalid method return type! \n\n");
                throw new Exception();
            }
        }else if(returnType2 == null){
            if(!returnType1.equals(myType)){
                System.out.println("\n\nInvalid method return type! \n\n");
                throw new Exception();
            }
        }

        mode.setElements(null, null);

        // System.out.println(myType + " " + myName + " -- " + argumentList);
        return null;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    
    public String visit(FormalParameterList n, String argu) throws Exception {
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
    public String visit(FormalParameterTerm n, String argu) throws Exception {
        return n.f1.accept(this, argu);
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    
    public String visit(FormalParameterTail n, String argu) throws Exception {
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
    
    public String visit(FormalParameter n, String argu) throws Exception{
        String type = n.f0.accept(this, null);
        String name = n.f1.accept(this, null);
        return type + " " + name;
    }



    /**
    * f0 -> PrimaryExpression()
    * f1 -> "&&"
    * f2 -> PrimaryExpression()
    */
    public String visit(AndExpression n, String argu) throws Exception {
        
        String varType = n.f0.accept(this, "type");
            
        
        if(varType == null){ //check above function description on function declaration for return values
            
            System.out.println("\n\nVariable undeclared!\n\n");
            throw new Exception();
        
        //only int type is accepted for PlusExpressions
        }else if( !varType.equals("boolean") ){
            System.out.println("\n\nOnly boolean are allowed in && operations.\n\n");
            throw new Exception();

        }

        varType = n.f2.accept(this, "type");

        
        if(varType == null){ //check above function description on function declaration for return values
            
            System.out.println("\n\nVariable undeclared!\n\n");
            throw new Exception();
        
        //only int type is accepted for PlusExpressions
        }else if( !varType.equals("boolean") ){
            System.out.println("\n\nOnly boolean are allowed in && operations.\n\n");
            throw new Exception();
        }

        // System.out.println("logic &&  ");

        
        return "boolean";
    }



    /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
    public String visit(PlusExpression n, String argu) throws Exception {
        
        String varType = n.f0.accept(this, "type");
            
        
        if(varType == null){ //check above function description on function declaration for return values
            
            System.out.println("\n\nVariable undeclared!\n\n");
            throw new Exception();
        
        //only int type is accepted for PlusExpressions
        }else if( !varType.equals("int") ){
            System.out.println("\n\nOnly integers are allowed in addition operations.\n\n");
            throw new Exception();

        }

        varType = n.f2.accept(this, "type");

        
        if(varType == null){ //check above function description on function declaration for return values
            
            System.out.println("\n\nVariable undeclared!\n\n");
            throw new Exception();
        
        //only int type is accepted for PlusExpressions
        }else if( !varType.equals("int") ){
            System.out.println("\n\nOnly integers are allowed in addition operations.\n\n");
            throw new Exception();
        }

        // System.out.println("prosthesi  ");

        return "int";
    }


    /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
    public String visit(MinusExpression n, String argu) throws Exception {
        
        String varType = n.f0.accept(this, "type");
            
        
        if(varType == null){ //check above function description on function declaration for return values
            
            System.out.println("\n\nVariable undeclared!\n\n");
            throw new Exception();
        
        //only int type is accepted for PlusExpressions
        }else if( !varType.equals("int") ){
            System.out.println("\n\nOnly integers are allowed in subtract operations.\n\n");
            throw new Exception();

        }

        varType = n.f2.accept(this, "type");

        
        if(varType == null){ //check above function description on function declaration for return values
            
            System.out.println("\n\nVariable undeclared!\n\n");
            throw new Exception();
        
        //only int type is accepted for PlusExpressions
        }else if( !varType.equals("int") ){
            System.out.println("\n\nOnly integers are allowed in subtract operations.\n\n");
            throw new Exception();
        }

        // System.out.println("afairesh  ");

        return "int";
    }
    
    public String visit(CompareExpression n, String argu) throws Exception {
        
        String varType = n.f0.accept(this, "type");
            
        
        if(varType == null){ //check above function description on function declaration for return values
            
            System.out.println("\n\nVariable undeclared!\n\n");
            throw new Exception();
        
        //only int type is accepted for PlusExpressions
        }else if( !varType.equals("int") ){
            System.out.println("\n\nOnly integers are allowed in comparison operations.\n\n");
            throw new Exception();

        }

        varType = n.f2.accept(this, "type");

        
        if(varType == null){ //check above function description on function declaration for return values
            
            System.out.println("\n\nVariable undeclared!\n\n");
            throw new Exception();
        
        //only int type is accepted for PlusExpressions
        }else if( !varType.equals("int") ){
            System.out.println("\n\nOnly integers are allowed in comparison operations.\n\n");
            throw new Exception();
        }

        // System.out.println("sygkrish  ");

        return "boolean";
    }

    public String visit(TimesExpression n, String argu) throws Exception {
        String varType = n.f0.accept(this, "type");
            
        
        if(varType == null){ //check above function description on function declaration for return values
            
            System.out.println("\n\nVariable undeclared!\n\n");
            throw new Exception();
        
        //only int type is accepted for PlusExpressions
        }else if( !varType.equals("int") ){
            System.out.println("\n\nOnly integers are allowed in multiplication operations.\n\n");
            throw new Exception();

        }

        varType = n.f2.accept(this, "type");

        
        if(varType == null){ //check above function description on function declaration for return values
            
            System.out.println("\n\nVariable undeclared!\n\n");
            throw new Exception();
        
        //only int type is accepted for PlusExpressions
        }else if( !varType.equals("int") ){
            System.out.println("\n\nOnly integers are allowed in multiplication operations.\n\n");
            throw new Exception();
        }

        // System.out.println("pollaplasiasmos  ");

        return "int";
    }

    public String visit(ArrayLookup n, String argu) throws Exception {
        String varType = n.f0.accept(this, "type");
            
        
        if(varType == null){ //check above function description on function declaration for return values
            
            System.out.println("\n\nVariable undeclared!\n\n");
            throw new Exception();
        
        //only int[] type is accepted for PlusExpressions
        }else if( !varType.equals("int[]") ){
            System.out.println("\n\nVariable is not an array!\n\n");
            throw new Exception();

        }

        varType = n.f2.accept(this, "type");

        
        if(varType == null){ //check above function description on function declaration for return values
            
            System.out.println("\n\nVariable undeclared!\n\n");
            throw new Exception();
        
        //only int type is accepted for PlusExpressions
        }else if( !varType.equals("int") ){
            System.out.println("\n\nOnly integers are allowed to be used as array indexes.\n\n");
            throw new Exception();
        }

        // System.out.println("koitaw pinaka  ");

        return "int";
    }


    /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
    public String visit(ArrayLength n, String argu) throws Exception {
        String varType = n.f0.accept(this, "type");
            
        
        if(varType == null){ //check above function description on function declaration for return values
            
            System.out.println("\n\nVariable undeclared!\n\n");
            throw new Exception();
        
        //only int type is accepted for PlusExpressions
        }else if( !varType.equals("int[]") ){
            System.out.println("\n\nVariable is not an array!\nYou should use an array variable to get its length!\n\n");
            throw new Exception();

        }

        // System.out.println("tsekara length  ");
        

        return "int";
    }
    

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
    */
    public String visit(MessageSend n, String argu) throws Exception {
        
        String keep_current_className = current_className;

        //get class that function comes from ( could be a "this" as well )
        String myClass = n.f0.accept(this, "type");
        // System.out.println("EDWWW  "+myClass);

        

        if( myClass == null ){
            System.out.println("\n\nClass undeclared!\n\n");
            throw new Exception();
        }else if( myClass.equals("int") || myClass.equals("boolean") || myClass.equals("int[]") ){ // if tis not a class
            System.out.println("\n\nObject that function is called at is not a Class Object!\n\n");
            throw new Exception();
        }


        //get the function that we are calling
        //check whether it exists in this class we just read , or its extends
        current_className = myClass;
        
        String methodName = n.f2.accept(this, null);
        String methodType = n.f2.accept(this, "methodtype"); //thsi is what we will return
        
        if( methodType == null ){
            System.out.println("\n\nMethod " + methodName + " undeclared!\n\n");
            throw new Exception();
        }

        //restore current class name
        current_className = keep_current_className;

        Pair<String,String> methodInfo = Pair.createPair(methodName, methodType);
        
        

        
        //fill the currentArgumentList HashMap
        n.f4.accept(this, null);

        //If there is a problem with the argument types of the declaration and the call, an exception is thrown within the function and a error message is printed

        if(!mydecls.checkMethodExistence(myClass, methodInfo, currentArgumentList)){ // the function returns false if no method with such name was found
            System.out.println("\n\nNo method named " + methodName + " was declared!\n\n");
            
        }

        //clear the currentArgumentList HashMap
        currentArgumentList.clear();

        

        //everything went well, return method type
        return methodType;
    }


    /**
    * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
    public String visit(ExpressionList n, String argu) throws Exception {
        
        String myName = n.f0.accept(this, null); 
        String myType = n.f0.accept(this, "type");

        //Get first argument type
        if( myType == null ){ //if it was not found, we wil lcheck whether the type is ok later on ;)
            System.out.println("\n\nUnknown argument type!\n\n");
            throw new Exception();
        }

        currentArgumentList.put(myName, myType);

        n.f1.accept(this, argu);
        
        return null;

    }

    /**
    * f0 -> ( ExpressionTerm() )*
    */
    public String visit(ExpressionTail n, String argu) throws Exception {
        return n.f0.accept(this, null);
    }

    /**
    * f0 -> ","
    * f1 -> Expression()
    */
    public String visit(ExpressionTerm n, String argu) throws Exception {
        
        
        String myName = n.f1.accept(this, null);
        String myType = n.f1.accept(this, "type");

        //THERE IS NO WAY WE HAVE VARABLES WITH THE NAME INT/BOOLEAN/INT[]/CLASSNAME! READ NEXT COMMENT TO SEE WHY WE NEED THIS
        if( myName.equals(myType) ){ //this happens when <INT_LITERAL> is given,so if we get 2 int_literals the currentArgumentsList will keep the instance of only one (because of duplicate key -> <ΙΝΤ,ΙΝΤ> <ΙΝΤ,ΙΝΤ>)
            myName = String.valueOf(flag++);
        }


        //Get first argument type
        if( myType == null ){ //if it was not found, we wil lcheck whether the type is ok later on ;)
            System.out.println("\n\nUnknown argument type!\n\n");
            throw new Exception();
        }

        currentArgumentList.put(myName, myType);

        return myType;
    }


    /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
    public String visit(AssignmentStatement n, String argu) throws Exception {
        String varName = n.f0.accept(this, null);
        String varType = n.f0.accept(this, "type");

        // System.out.println(varType + " eimai " +  varName);

        if(varType == null){
            System.out.println("Variable "+ varName + " is undeclared!");
            throw new Exception();
        }

        //just gotta check if expression,that maybe is a varType variable or a varType expr or a varType method, is valid
        //n.f2.accept(this, "type") == null if its not expr or variable
        //n.f2.accept(this, "methodtype") == null if its not a method (message send)

        String exprType1 = n.f2.accept(this, "type");
        String exprType2 = n.f2.accept(this, "methodtype");


        // System.out.println("dsadsa " +varType+"  dsadas "+ exprType1 + " " + exprType2);
        //if it all went well
        if( (exprType1 != null && exprType1.equals(varType)) || (exprType2 != null && exprType2.equals(varType)) ){
            // System.out.println("assign");
            
            return varType;
        
        }else if( exprType1 != null && mydecls.check_extend(exprType1) ){ // there is a chance varType is type A class , and exprType is type B class. If B extends A this is ok

            
            // A a = new B();
            //so if B extends
            
            //and if it extends A  (A = A)
            if( mydecls.get_extendClass(exprType1).equals( varType) ){
                //it all went fine
                System.out.println("class assignment!");

                return varType;
            }else{
                System.out.println("\n\nIncompatitable types!\nVariable " + varName + " cannot be converted to " + exprType1 + "!\n\n");
                throw new Exception();
            }


        }else if( exprType2 != null && mydecls.check_extend(exprType2) ){ // same as above, but it may be a method

            // A a = new B();
            //so if B extends
            
            //and if it extends A  (A = A)
            if( mydecls.get_extendClass(exprType2).equals(varType) ){
                //it all went fine
                System.out.println("class assignment!");

                return varType;
            }else{
                System.out.println("\n\nIncompatitable types!\nVariable " + varName + " cannot be converted to " + exprType2 + "!\n\n");
                throw new Exception();
            }


        }else{//it did not go well
            System.out.println("\n\nWrong type of assignment!\nVariable " + varName + " is type " + varType + "!\n\n");
            throw new Exception();
        }


        
    }

    /**
    * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Expression()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Expression()
    * f6 -> ";"
    */
    public String visit(ArrayAssignmentStatement n, String argu) throws Exception {
        
        // System.out.println("array assign");

        String varName = n.f0.accept(this, null);
        String varType = n.f0.accept(this, "type");

        if(varType == null){
            System.out.println("Variable " + varName + " is undeclared!");
            throw new Exception();
        }

        //just gotta check if expression,that maybe is a int variable or a int expr or a int method, is valid
        //n.f2.accept(this, "type") == null if its not expr or variable
        //n.f2.accept(this, "methodtype") == null if its not a method (message send)

        String exprType1 = n.f2.accept(this, "type");
        String exprType2 = n.f2.accept(this, "methodtype");

        //if it all went well
        if( (exprType1 != null && exprType1.equals("int")) || (exprType2 != null && exprType2.equals("int") ) ){
            // System.out.println("assign");
            
            //just gotta check if expression,that maybe is a varType variable or a varType expr or a varType method, is valid
            //n.f5.accept(this, "type") == null if its not expr or variable
            //n.f5.accept(this, "methodtype") == null if its not a method (message send)

            String assignmentType1 = n.f5.accept(this, "type");
            String assignmentType2 = n.f5.accept(this, "methodtype");

            //if it all went well
            if( (assignmentType1 != null && assignmentType1.equals("int")) || (assignmentType2 != null && assignmentType2.equals("int") ) ){
                // System.out.println("assign");
                
                return varType;
            
            }else{//it did not go well
                System.out.println("\n\nWrong type of assignment!\nArray " + varName + " is type " + varType + "!\n\n");
                throw new Exception();
            }

            
        
        }else{//it did not go well
            System.out.println("\n\nArray indexes should be type of int!\n\n");
            throw new Exception();
        }



    }

    /**
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
    */
    public String visit(IfStatement n, String argu) throws Exception {
        
        //just gotta check if expression,that maybe is a boolean variable or a boolean expr or a boolean method, is valid
        //n.f2.accept(this, "type") == null if its not expr or variable
        //n.f2.accept(this, "methodtype") == null if its not a method (message send)

        String exprType1 = n.f2.accept(this, "type");
        String exprType2 = n.f2.accept(this, "methodtype");

        //if it all went well
        if( (exprType1 != null && exprType1.equals("boolean")) || (exprType2 != null && exprType2.equals("boolean")) ){
            
            n.f4.accept(this, argu);
            n.f6.accept(this, argu);
        
        }else{//it did not go well
            System.out.println("\n\nWhile expression is not a logical expression (boolean type)!\n\n");
            throw new Exception();
        }

        // System.out.println("ifelse");

        return null;
    }



    /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
    public String visit(WhileStatement n, String argu) throws Exception {
        
        //just gotta check if expression,that maybe is a boolean variable or a boolean expr or a boolean method, is valid
        //n.f2.accept(this, "type") == null if its not expr or variable
        //n.f2.accept(this, "methodtype") == null if its not a method (message send)

        String exprType1 = n.f2.accept(this, "type");
        String exprType2 = n.f2.accept(this, "methodtype");

        //if it all went well
        if( (exprType1 != null && exprType1.equals("boolean") ) || (exprType2 != null && exprType2.equals("boolean") ) ){
            
            n.f4.accept(this, argu);
        
        }else{//it did not go well
            System.out.println("\n\nWhile expression is not a logical expression (boolean type)!\n\n");
            throw new Exception();
        }
        
        // System.out.println("While...");

        return null;
    
        
    }


    /**
    * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
    public String visit(PrintStatement n, String argu) throws Exception {
        
        //AS IN JAVA WE ACCEPT ANY TYPE OF DECLARED OBJECT (int,boolean,int[],className)
        
        String myType1 = n.f2.accept(this, "type"); // check whetehr it is a variable or expression
        if(myType1 == null){//if it is not

            //maybe it is a function
            String myMethodType = n.f2.accept(this, "methodtype");

            if(myMethodType == null){ // if it's nto a method too 
                System.out.println("\n\nUnknown print argument!\n\n");
                throw new Exception();
            }
            //if we got here it was a function,return its type
            return myMethodType;
        }

        // System.out.println("ektypwnw");

        //if we got here everything went ok 
        return myType1;
    }



    /**
    * f0 -> "!"
    * f1 -> PrimaryExpression()
    */
    public String visit(NotExpression n, String argu) throws Exception {
        
        
        String varType = n.f1.accept(this, "type");
            
        
        if(varType == null){ //check above function description on function declaration for return values
            
            System.out.println("\n\nVariable undeclared!\n\n");
            throw new Exception();
        
        //only int type is accepted for PlusExpressions
        }else if( !varType.equals("boolean") ){
            System.out.println("\n\nVariable is not boolean!\nYou should use a boolean variable to use the \"not\" operator!\n\n");
            throw new Exception();

        }

        // System.out.println("notExpression  ");
        

        return "boolean";
    }

    public String visit(ArrayType n, String argu) {
        return "int[]";
    }

    public String visit(BooleanType n, String argu) {
        return "boolean";
    }

    public String visit(IntegerType n, String argu) {
        return "int";
    }

    public String visit(IntegerLiteral n, String argu) throws Exception {
        return "int";
    }

    public String visit(TrueLiteral n, String argu) throws Exception {
        String myValue = n.f0.accept(this, argu);

        return "boolean";
    }

    /**
    * f0 -> "false"
    */
    public String visit(FalseLiteral n, String argu) throws Exception {
        String myValue = n.f0.accept(this, argu);

        return "boolean";

    }

    /**
    * f0 -> "this"
    */
    public String visit(ThisExpression n, String argu) throws Exception {
        return current_className;
    }

    /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
    public String visit(AllocationExpression n, String argu) throws Exception {
        
        String myClassName = n.f1.accept(this, null);
        
        //We just gotta check if such class exists
        if( !(mydecls.table.containsKey(myClassName)) ){
            System.out.println("\n\nClass undeclared!\n\n");
            throw new Exception();
        }

        // System.out.println("newClassAlloc  ");


        return myClassName; // return class
    }

    /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
    public String visit(ArrayAllocationExpression n, String argu) throws Exception {
        
        String myType = n.f3.accept(this, "type");
        
        if(myType == null){ //check above function description on function declaration for return values
            
            System.out.println("\n\nVariable undeclared!\n\n");
            throw new Exception();
        
        //only int type is accepted for PlusExpressions
        }else if( !myType.equals("int") ){
            System.out.println("\n\nSize should be an Integer when allocation a new Array!\n\n");
            throw new Exception();

        }

        // System.out.println("newArrayAlloc  ");

        return "int[]";
    }

    @Override
    public String visit(Identifier n, String argu) { //if I want to return Identifier type then argu == "type"
        
        String myName = n.f0.toString();  



        if(argu == "type"){ //if I want to return Identifier type then argu == "type"
            return mydecls.find_identifierType(current_className, myName, mode); //return variable type
        }
        if(argu == "methodtype"){ //if I want to return methodType type then argu == "methodtype"
            return mydecls.find_methodType(current_className, myName); //return method type
        }


        return myName; //else just return the name
   
    }

    /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
    public String visit(BracketExpression n, String argu) throws Exception {
        String myType = n.f1.accept(this, null);
        

        if(argu=="type") myType = n.f1.accept(this, "type");
        if(argu=="methodtype") myType = n.f1.accept(this, "methodtype");

        return myType;
    }
    
}