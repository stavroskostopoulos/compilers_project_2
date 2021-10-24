
import syntaxtree.*;
import visitor.*;



import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.*;
import java.util.*;




public class Main {
    public static void main(String[] args) throws Exception {
        // if(args.length != 1){
        //     System.err.println("Usage: java Main <inputFile>");
        //     System.exit(1);
        // }

        if(args.length < 1){
            System.err.println("Please fix command line arguments!");
            System.exit(1);
        }


        for(int i= args.length-1; i>=0; i--){


            FileInputStream fis = null;

            try{
                fis = new FileInputStream(args[i]);
                MiniJavaParser parser = new MiniJavaParser(fis);

                Goal root = parser.Goal();

                System.err.println("Program parsed successfully.");

                symbolTable eval = new symbolTable();
                root.accept(eval, null);

                semanticVisitor eval2 = new semanticVisitor();
                root.accept(eval2, null);

                myOffsets eval3 = new myOffsets();

                eval.mydecls.table.clear();
            }
            catch(ParseException ex){
                System.out.println(ex.getMessage());
            }
            catch(FileNotFoundException ex){
                System.err.println(ex.getMessage());
            }
            finally{
                try{
                    if(fis != null) fis.close();
                }
                catch(IOException ex){
                    System.err.println(ex.getMessage());
                }
            }

        }
    }
}

class classDeclarationTable{

    Map<String,Vector> table = new HashMap<String,Vector>();

    classDeclarationTable(){     
        System.out.println("\nCreated symbolTable!\n");
    }

    //a function to add a new class that was just declared to the symbolTable
    public void add_newClass(String name, String extend){
        //In this Hashmap each cell contains a class name and a vector 
        //which contains a HashMap for the variables of the class and a HashMap for the methods

        HashMap varMap = new HashMap<String, String>();
        //(check read me) the method map consists of the method name and a vector containing
        //a HashMap for the method's arguments and a HashMap forthe method's variables declarations
        HashMap methodMap = new HashMap< Pair<String,String>, Vector>();

        
        


        //create the vector
        Vector classContent = new Vector();

        //add the variable map vector[0]
        classContent.add(varMap);
        //add the method map vector[1]
        classContent.add(methodMap);

        //if this new class extends a new one,then add one more index to the contains Vector (vector[2])
        if(extend != null)    classContent.add(extend);

        this.table.put(name, classContent);

    }
    
    public Vector create_methodVector(){
        //(check read me) the method map consists of the method name and a vector containing
        //a HashMap for the method's arguments and a HashMap forthe method's variables declarations


        //method's vector
        Vector methodContent = new Vector();

        LinkedHashMap methodArguments = new LinkedHashMap<String,String>(); // <type, name>
        HashMap methodVariables = new HashMap<String,String>(); // <type, name>

        //add the above to the method's contains vector
        methodContent.add(methodArguments); //vector[0]
        methodContent.add(methodVariables); //vector[1]

        return methodContent;
    }

    public HashMap<String,String> getClassVariables(String className){
        
        return (HashMap<String,String>) this.table.get(className).get(0);    

    }

    public HashMap<Pair<String,String>,Vector> getClassMethods(String className){
        
        return (HashMap<Pair<String,String>,Vector>) this.table.get(className).get(1);    

    }

    public HashMap<String,String> getMethodVariables(String className, Pair<String, String> methodName){ //Function name speaks by itself :P

        //Get the HashMap of methods for this class
        HashMap<Pair<String,String>,Vector> class_methodMap = getClassMethods(className);
        
        //get contains of this specific method

        //check existence
        if(class_methodMap.containsKey(methodName)){
            Vector temp = class_methodMap.get(methodName);
            
            
            //return method variable hashmap
            return (HashMap<String,String>) temp.get(1);    

        }    
        

        return null;
    }

    public Vector constructArgumentTypeVector( LinkedHashMap<String, String> argumentTable){ // Return something like: "int,int,boolean,int"
        
        Vector myVector = new Vector();

        for(String type : argumentTable.values()) {
            myVector.add(type);
        }

        
        return myVector;
    }


    public boolean vectorEqual(Vector a1, Vector a2){
        if(a1.size() != a2.size()) return false;
        
        for(int i=0 ; i < a1.size() ; i++){
            if(!a1.get(i).equals(a2.get(i))) return false;
        }

        return true;
    }

    public boolean vectorEqualwithExtends(Vector decl, Vector calls){
        if(decl.size() != calls.size()) return false;
        
        for(int i=0 ; i < decl.size() ; i++){

            if(!decl.get(i).equals(calls.get(i))){
                //if decl[i] != calls[i]
                //lets check if there is an extended class

                if(  !(decl.get(i).toString()).equals(this.get_extendClass(calls.get(i).toString()))   ) return false;
            }
        }

        return true;
    }


    public boolean checkMethodExistence(String className, Pair method, LinkedHashMap<String,String> callArguments) throws Exception{// returns false if no such method was found

        boolean result = false;

        //check existence

        //First check whether there is a class this class extends
        if(check_extend(className)){//If there is, we gotta check here first
            
            String extendsName = get_extendClass(className);

            result = checkMethodExistence(extendsName, method, callArguments);
        }

        //Let's check whether there actually is a method with such name and type in our class
        HashMap<Pair<String,String>,Vector> tempMethMap = getClassMethods(className);

        //if there is not
        if(!tempMethMap.containsKey(method)){
            //if no method was found in extend as well
            if( result == false) return false;
            else return true; // so if it wsa found in extend class and not here reutn true ( if result == true ecerything was fine when we checked the extend class)
        }

        //if there is we like that
        //let's get it

        Vector methodContains = tempMethMap.get(method);

        //Now last thing we gotta check is if the arguments fit

        //lets get a string list with the argument types of the method. (IN CORRECT ORDER)
        Vector declareArgs = constructArgumentTypeVector((LinkedHashMap<String,String>)methodContains.get(0));
        Vector callArgs = constructArgumentTypeVector(callArguments);
        

        // System.out.println("DECLARE  " + declareArgs);
        // System.out.println("CALL  " + callArgs);


        if(!this.vectorEqual(declareArgs, callArgs)){ // if declaration variable types don't match the call variable types
            //maybe we are getting an extended type,taht we should accept..Let's check this first!
            if(!vectorEqualwithExtends(declareArgs, callArgs)){
                System.out.println("\n\nWrong argument types used for method named " + method.getElement0() + "!\n\n");
                throw new Exception();
            } 
            
        }


        //if we are here,everythign went great
        return true;

    }

    public LinkedHashMap<String,String> getMethodArguments(String className, Pair<String, String> methodName){ //Function name speaks by itself :P

        //Get the HashMap of methods for this class
        HashMap<Pair<String,String>,Vector> class_methodMap = getClassMethods(className);
        
        //get contains of this specific method

        //check existence
        if(class_methodMap.containsKey(methodName)){
            Vector temp = class_methodMap.get(methodName);
            
            //return method variable hashmap
            return (LinkedHashMap<String,String>) temp.get(0);    

        }    
        

        return null;
    }
    
    public boolean check_methodDuplicates(String className, Pair methodName){ //Function name speaks by itself :P

        return getClassMethods(className).containsKey(methodName);
    }

    public boolean check_extend(String className){ //check whether this class (className) extends another function (check add_newClass() to see how this works)
        if(!this.table.containsKey(className)) return false;
        
        return this.table.get(className).size() == 3 ? true : false;
    }

    public String get_extendClass(String className){ // Get the name of the class that the class we are in extends (if we are in B and B extends A this will return "A")
        //Only use this function if we are sure that the class className extends another class! This is YOUR responsibility!
        return (String) this.table.get(className).get(2);
    }

    public String find_identifierType(String className, String variableName, Pair methodName){ //A function that returns a variable's type | returns NULL if variable was not found
        
        //Let's have a talk about this..
        //Let's say we have a variableName "number". 
        //This variable may have been declared in the class scope (outside of a function).So at first we check there.
        //After this, it may has ALSO been declared within a method in the same class.So we MUST also check method with methodName
        //the method may have a new declaration using this variableName 

        HashMap<String,String> classVariables = this.getClassVariables(className);

        String varType = null;


        //First of all, maybe the variable belongs to the superclass
        //if the class we are in extends another class

        //let's check if it extends
        if(check_extend(className)){
            //System.out.println(get_extendClass(className));
            String extendsName = get_extendClass(className);
            
            //we don't want to check the superclass' method though (that may have the same name with the method we are in right now)
            //so let's create a new methodName Pair object and set it to null
            Pair<String,String> no_method = Pair.createPair(null, null);

            //This may return something, may return null
            //If we find something else later on it will be overriden
            varType = find_identifierType( extendsName, variableName, no_method);
            
        }



        //check existence and get save the variableType in varType (else it remains NULL)
        if(classVariables.containsKey(variableName))    varType = classVariables.get(variableName);


        
        if(methodName.getElement0() != null){//if we came from a method then we have to look at the methods variable as well
            
            HashMap<String,String> methodVariables = getMethodVariables(className, methodName);

            //check if there is such variable in this method
            if(methodVariables.containsKey(variableName)){

                //if there is, this is the variable we are looking for!
                return methodVariables.get(variableName);
  
            }
            
            //if we are here we didn't find such variable in the method's variable hashmap
            //so let's try to find it in its arguments

            LinkedHashMap<String,String> methodArguments = getMethodArguments(className, methodName);

            //check if there is such variable in this method
            if(methodArguments.containsKey(variableName)){

                //if there is, this is the variable we are looking for!
                return methodArguments.get(variableName);
  
            }
   
        }

        


        //If we got here then there was not such variable neither in method arguments or variables
        //just return the varType we found in class, or if there was not such variable found return null
        return varType;

    }

    public String find_methodType(String className, String methodName){
        
        String varType = null;

        //First we check to the extended class (if there is one)
        //let's check if it extends
        if(check_extend(className)){
            //System.out.println(get_extendClass(className));
            String extendsName = get_extendClass(className);
            
            //we don't want to check the superclass' method though (that may have the same name with the method we are in right now)
            //so let's create a new methodName Pair object and set it to null
            Pair<String,String> no_method = Pair.createPair(null, null);

            //This may return something, may return null
            //If we find something else later on it will be overriden
            varType = find_methodType( extendsName, methodName);
            
        }


        //Lets check for className now
        HashMap<Pair<String,String>,Vector> classMethods = getClassMethods(className);



        //check method existence
        //iterate through keyset
        for(Pair<String,String> temp : classMethods.keySet()){
            if(temp.getElement0() == methodName) return temp.getElement1(); //return type if you find the methodname

        }


        return varType;
        
    }

}



